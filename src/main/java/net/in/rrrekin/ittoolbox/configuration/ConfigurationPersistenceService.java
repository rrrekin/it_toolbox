package net.in.rrrekin.ittoolbox.configuration;

import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_MODULE_LIST;
import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_MODULE_OPTIONS;
import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_SERVICES_SECTION;
import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_SERVICE_CONFIGURATION;
import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.SERVER_LIST_UNREADABLE;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeFactory;
import net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceRegistry;
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Service responsible for saving and loading application configuration.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class ConfigurationPersistenceService {

  private static final String VERSION = "1.0";
  private static final String VERSION_PROPERTY = "version";
  private static final String SERVERS_PROPERTY = "servers";
  private static final String MODULES_PROPERTY = "modules";
  private static final String SERVICES_PROPERTY = "services";
  private static final String LOCALE_PROPERTY = "locale";
  private static final int YAML_LINE_WIDTH = 130;
  public static final int MAX_OBJECT_DESCRIPTION_WIDTH = 40;
  private final @NonNull DumperOptions yamlOptions;
  private final @NonNull ServiceRegistry serviceRegistry;
  private final @NonNull NodeFactory nodeFactory;
  private final @NonNull EventBus eventBus;

  /**
   * Instantiates a new Configuration persistence service.
   *
   * @param serviceRegistry the service registry
   * @param nodeFactory the node factory
   */
  @Inject
  public ConfigurationPersistenceService(
      final @NonNull ServiceRegistry serviceRegistry,
      final @NonNull NodeFactory nodeFactory,
      final @NonNull EventBus eventBus) {
    log.debug("Initializing ConfigurationPersistenceService");
    yamlOptions = new DumperOptions();
    yamlOptions.setIndent(4);
    yamlOptions.setAllowUnicode(true);
    yamlOptions.setIndicatorIndent(2);
    yamlOptions.setNonPrintableStyle(DumperOptions.NonPrintableStyle.ESCAPE);
    yamlOptions.setWidth(YAML_LINE_WIDTH);
    yamlOptions.setSplitLines(true);
    yamlOptions.setVersion(DumperOptions.Version.V1_1);
    yamlOptions.setPrettyFlow(true);
    yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    this.serviceRegistry = serviceRegistry;
    this.nodeFactory = nodeFactory;
    this.eventBus = eventBus;
  }

  // TODO: Add user notifications on minor configuration read errors during initial load.
  // TODO: Introduce exceptions to reject reload of invalid configuration.

  /**
   * Creates a new Configuration object based on a configuration YAML file.
   *
   * @param configFile the config file
   * @return the configuration
   * @throws InvalidConfigurationException when configuration cannot be properly read
   * @throws MissingConfigurationException when configuration file is missing
   */
  public Configuration load(final @NonNull File configFile)
      throws InvalidConfigurationException, MissingConfigurationException {

    log.info("Loading configuration from '{}'", configFile);
    final Yaml yaml = new Yaml(yamlOptions);
    final Map<String, ?> configurationDto;
    try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(configFile))) {
      configurationDto = yaml.load(inputStream);
    } catch (final FileNotFoundException e) {
      log.warn("Configuration file ({}) not present.", configFile);
      throw new MissingConfigurationException("EX_MISSING_CFG_FILE", e, configFile);
    } catch (final IOException | ClassCastException e) {
      log.warn("Failed to read configuration file ({}): {}", configFile, e.getLocalizedMessage());
      throw new InvalidConfigurationException("EX_UNREADABLE_CFG_FILE", e, configFile);
    }

    if (configurationDto == null) {
      throw new InvalidConfigurationException("EX_UNREADABLE_CFG_FILE", configFile);
    }

    if (configurationDto.get(LOCALE_PROPERTY) != null) {
      LocaleUtil.setLocale(
          Locale.forLanguageTag(String.valueOf(configurationDto.get(LOCALE_PROPERTY))));
    }

    // TODO: simplify
    final Object version = configurationDto.get(VERSION_PROPERTY);
    if (VERSION.equals(version)) {
      // Read Network nodes configuration
      final List<NetworkNode> networkNodes;
      final Object serversDto = configurationDto.get(SERVERS_PROPERTY);
      if (serversDto instanceof List) {
        networkNodes = nodeFactory.createFrom((List<?>) serversDto, SERVERS_PROPERTY);
      } else {
        log.warn("Failed to read server list.");
        eventBus.post(
            new ConfigurationErrorEvent(
                SERVER_LIST_UNREADABLE, LocaleUtil.localMessage("CFG_SERVER_LIST_UNREADABLE")));
        networkNodes = Lists.newArrayList();
      }

      // Read application modules configuration
      final Map<String, Map<String, String>> modules = Maps.newHashMap();
      final Object modulesDto = configurationDto.get(MODULES_PROPERTY);
      if (modulesDto instanceof Map) {
        for (final Map.Entry<?, ?> entry : ((Map<?, ?>) modulesDto).entrySet()) {
          final String moduleId = StringUtils.toStringOrEmpty(entry.getKey());
          final Object optionsDto = entry.getValue();
          final Map<String, String> moduleConfig = Maps.newHashMap();
          modules.put(moduleId, moduleConfig);
          if (optionsDto instanceof Map) {
            for (final Map.Entry<?, ?> optionEntry : ((Map<?, ?>) optionsDto).entrySet()) {
              final String optionId = StringUtils.toStringOrEmpty(optionEntry.getKey());
              final String optionValue = StringUtils.toStringOrEmpty(optionEntry.getValue());
              moduleConfig.put(optionId, optionValue);
            }
          } else {
            log.warn("Failed to read options for module {}", moduleId);
            eventBus.post(
                new ConfigurationErrorEvent(
                    INVALID_MODULE_OPTIONS,
                    LocaleUtil.localMessage(
                        "CFG_INVALID_MODULE_OPTIONS",
                        moduleId,
                        abbreviate(String.valueOf(optionsDto), MAX_OBJECT_DESCRIPTION_WIDTH))));
          }
        }
      } else {
        log.warn("Failed to read application modules configuration.");
        eventBus.post(
            new ConfigurationErrorEvent(
                INVALID_MODULE_LIST, LocaleUtil.localMessage("CFG_INVALID_MODULE_LIST")));
      }

      // Read service configuration
      final Object servicesDto = configurationDto.get(SERVICES_PROPERTY);
      if (servicesDto instanceof Map) {
        for (final Map.Entry<?, ?> entry : ((Map<?, ?>) servicesDto).entrySet()) {
          final String serviceId = StringUtils.toStringOrEmpty(entry.getKey());
          final String serviceOptions = StringUtils.toStringOrEmpty(entry.getValue());
          try {
            serviceRegistry.configureService(serviceId, serviceOptions);
          } catch (final Exception e) {
            log.warn("Failed to configure service {}.", serviceId);
            eventBus.post(
                new ConfigurationErrorEvent(
                    INVALID_SERVICE_CONFIGURATION,
                    LocaleUtil.localMessage(
                        "CFG_INVALID_SERVICE_CONFIGURATION",
                        serviceId,
                        abbreviate(String.valueOf(serviceOptions), MAX_OBJECT_DESCRIPTION_WIDTH))));
          }
        }
      } else {
        log.warn("Failed to read services list.");
        eventBus.post(
            new ConfigurationErrorEvent(
                INVALID_SERVICES_SECTION, LocaleUtil.localMessage("CFG_INVALID_SERVICES_SECTION")));
      }

      return new Configuration(networkNodes, modules);

    } else {
      throw new InvalidConfigurationException("EX_UNKNOWN_VERSION", configFile, version);
    }
  }

  /**
   * Save configuration to file.
   *
   * @param configFile the config file
   * @param config the config
   * @throws FailedConfigurationSaveException when unable to save the configuration to the file
   */
  public void save(final @NonNull File configFile, final @NonNull Configuration config)
      throws FailedConfigurationSaveException {
    final Yaml yaml = new Yaml(yamlOptions);
    try (final BufferedWriter output =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
      yaml.dump(getDto(config), output);
    } catch (final IOException e) {
      log.warn("Failed to write configuration file ({}): {}", configFile, e.getLocalizedMessage());
      throw new FailedConfigurationSaveException(
          "EX_CONFIG_SAVE_ERROR", e, configFile, e.getLocalizedMessage());
    }
  }

  private @NotNull Map<String, ?> getDto(final @NotNull Configuration config) {
    final List<Map<String, ?>> serversDto =
        config.getNetworkNodes().stream()
            .map(NetworkNode::getDtoProperties)
            .collect(Collectors.toList());
    final Map<String, String> services =
        serviceRegistry.stream()
            .collect(
                Collectors.toMap(ServiceDefinition::getId, ServiceDefinition::getConfiguration));

    final ImmutableMap.Builder<String, Object> responseBuilder = ImmutableMap.builder();
    responseBuilder.put(VERSION_PROPERTY, VERSION);
    if (LocaleUtil.getLocaleCode() != null) {
      responseBuilder.put(LOCALE_PROPERTY, LocaleUtil.getLocaleCode());
    }
    responseBuilder.put(SERVERS_PROPERTY, serversDto);
    responseBuilder.put(SERVICES_PROPERTY, services);
    responseBuilder.put(MODULES_PROPERTY, config.getModules());

    return responseBuilder.build();
  }
}
