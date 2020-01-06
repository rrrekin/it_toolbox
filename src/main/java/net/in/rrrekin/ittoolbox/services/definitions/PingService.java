package net.in.rrrekin.ittoolbox.services.definitions;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.os.OsServices;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.ServiceType;
import net.in.rrrekin.ittoolbox.services.executors.PingExecutor;
import org.jetbrains.annotations.NotNull;

/** @author michal.rudewicz@gmail.com */
public class PingService implements ServiceDefinition {

  private final @NotNull OsServices osServices;
  private final @NotNull UserPreferencesFactory userPreferencesFactory;

  public PingService(
      final @NotNull OsServices osServices,
      final @NotNull UserPreferencesFactory userPreferencesFactory) {
    this.osServices = requireNonNull(osServices, "osServices must be not null");
    this.userPreferencesFactory =
        requireNonNull(userPreferencesFactory, "userPreferencesFactory must be not null");
  }

  @NotNull
  @Override
  public String getName(final @NotNull ServiceDescriptor descriptor) {
    return localMessage("SERVICE_PING");
  }

  @Override
  public @NotNull ServiceType getType() {
    return ServiceType.PING;
  }

  @Override
  public @NotNull ServiceDescriptor getDefaultDescriptor() {
    return new ServiceDescriptor(ServiceType.PING, null);
  }

  @NotNull
  @Override
  public ServiceExecutor getExecutor(
      @NotNull final ServiceDescriptor descriptor, final @NotNull Configuration configuration) {
    return new PingExecutor(osServices, userPreferencesFactory);
  }

  @Override
  public boolean hasEditor() {
    return false;
  }

  @NotNull
  @Override
  public ServiceEditor getEditor() {
    throw new IllegalStateException("Editor not available for PingService.");
  }

  @Override
  public boolean isApplicableTo(@NotNull final NodeType type) {
    return type == NodeType.SERVER;
  }

  @Override
  public boolean isDefaultFor(@NotNull final NodeType type) {
    return type == NodeType.SERVER;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
