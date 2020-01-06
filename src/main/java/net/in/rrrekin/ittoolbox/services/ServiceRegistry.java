package net.in.rrrekin.ittoolbox.services;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.os.OsServices;
import net.in.rrrekin.ittoolbox.services.definitions.ExecuteService;
import net.in.rrrekin.ittoolbox.services.definitions.NetcatService;
import net.in.rrrekin.ittoolbox.services.definitions.NmapService;
import net.in.rrrekin.ittoolbox.services.definitions.PingService;
import net.in.rrrekin.ittoolbox.services.definitions.SshService;
import net.in.rrrekin.ittoolbox.services.definitions.TelnetService;
import net.in.rrrekin.ittoolbox.services.definitions.TracerouteService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author michal.rudewicz@gmail.com */
public class ServiceRegistry {

  @NonNls private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

  private final @NotNull Map<ServiceType, ServiceDefinition> serviceMap =
      new EnumMap<>(ServiceType.class);
  private final @NotNull List<ServiceDefinition> serverDefaultServices;
  private final @NotNull List<ServiceDefinition> serverDefinedServices;
  private final @NotNull List<ServiceDefinition> genericDefaultServices;
  private final @NotNull List<ServiceDefinition> genericDefinedServices;
  private final @NotNull List<ServiceDefinition> groupDefaultServices;
  private final @NotNull List<ServiceDefinition> groupDefinedServices;

  private final @NotNull CommonResources commonResources;
  private final @NotNull OsServices osServices;
  private final @NotNull UserPreferencesFactory userPreferencesFactory;

  @Inject
  public ServiceRegistry(
      @NotNull final CommonResources commonResources,
      final @NotNull OsServices osServices,
      final @NotNull UserPreferencesFactory userPreferencesFactory) {
    log.debug("Creating ServiceRegistry.");
    this.commonResources = requireNonNull(commonResources, "commonResources must not be null");
    this.osServices = requireNonNull(osServices, "osServices must not be null");
    this.userPreferencesFactory =
        requireNonNull(userPreferencesFactory, "userPreferencesFactory must not be null");
    final List<ServiceDefinition> services =
        List.of(
            new PingService(osServices, userPreferencesFactory),
            new TracerouteService(commonResources),
            new ExecuteService(commonResources),
            new SshService(commonResources),
            new TelnetService(commonResources),
            new NetcatService(commonResources),
            new NmapService(commonResources));

    for (final ServiceDefinition service : services) {
      checkState(
          !serviceMap.containsKey(service.getType()),
          "Service type %s wanted by %s is already registered for %s",
          service.getType(),
          service,
          serviceMap.get(service.getType()));
      serviceMap.put(service.getType(), service);
    }
    serverDefaultServices =
        services.stream()
            .filter(service -> service.isDefaultFor(NodeType.SERVER))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Server default services: {}", serverDefaultServices);
    serverDefinedServices =
        services.stream()
            .filter(
                service ->
                    service.isApplicableTo(NodeType.SERVER)
                        && (!service.isDefaultFor(NodeType.SERVER) || service.hasEditor()))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Server defined services: {}", serverDefinedServices);

    genericDefaultServices =
        services.stream()
            .filter(service -> service.isDefaultFor(NodeType.GENERIC_NODE))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Generic node default services: {}", genericDefaultServices);
    genericDefinedServices =
        services.stream()
            .filter(
                service ->
                    service.isApplicableTo(NodeType.GENERIC_NODE)
                        && (!service.isDefaultFor(NodeType.GENERIC_NODE) || service.hasEditor()))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Generic node defined services: {}", genericDefinedServices);

    groupDefaultServices =
        services.stream()
            .filter(service -> service.isDefaultFor(NodeType.GROUP))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Grouping node default services: {}", groupDefaultServices);
    groupDefinedServices =
        services.stream()
            .filter(
                service ->
                    service.isApplicableTo(NodeType.GROUP)
                        && (!service.isDefaultFor(NodeType.GROUP) || service.hasEditor()))
            .collect(Collectors.toUnmodifiableList());
    log.debug("Grouping node defined services: {}", groupDefinedServices);

    log.debug("ServiceRegistry created. Registered services: {}", serviceMap.keySet());
  }

  public Collection<ServiceType> getAllServiceTypes() {
    return Collections.unmodifiableSet(serviceMap.keySet());
  }

  public Collection<ServiceDefinition> getAllServices() {
    return Collections.unmodifiableCollection(serviceMap.values());
  }

  public @NotNull List<ServiceDefinition> getDefaultServicesFor(final @NotNull NodeType nodeType) {
    switch (nodeType) {
      case SERVER:
        return serverDefaultServices;
      case GENERIC_NODE:
        return genericDefaultServices;
      case GROUP:
        return groupDefaultServices;
      default:
        throw new IllegalStateException("Unknown node type " + nodeType);
    }
  }

  public @NotNull List<ServiceDefinition> getDefinedServicesFor(final @NotNull NodeType nodeType) {
    switch (nodeType) {
      case SERVER:
        return serverDefinedServices;
      case GENERIC_NODE:
        return genericDefinedServices;
      case GROUP:
        return groupDefinedServices;
      default:
        throw new IllegalStateException("Unknown node type " + nodeType);
    }
  }

  @Deprecated
  public void configureService(final String serviceId, final String serviceOptions) {
    // Stub method. TBD if this is correct approach

  }

  @Deprecated
  public @NotNull Stream<ServiceDefinition> stream() {
    return serviceMap.values().stream();
  }

  public @NotNull String getNameFor(final @NotNull ServiceDescriptor sd) {
    final ServiceDefinition service = serviceMap.get(sd.getType());
    if (service != null) {
      return service.getName(sd);
    } else {
      log.error("Cannot find service for {}", sd);
      return sd.getType().name();
    }
  }

  public @NotNull ServiceExecutor getExecutorFor(
      @NotNull final ServiceDescriptor sd, final @NotNull Configuration configuration) {
    final ServiceDefinition service = serviceMap.get(sd.getType());
    return service.getExecutor(sd, configuration);
  }
}
