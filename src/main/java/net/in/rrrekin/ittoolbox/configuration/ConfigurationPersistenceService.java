package net.in.rrrekin.ittoolbox.configuration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
  private final @NonNull DumperOptions yamlOptions;
  private final @NonNull ServiceRegistry serviceRegistry;
  private final @NonNull NodeFactory nodeFactory;

  /**
   * Instantiates a new Configuration persistence service.
   *
   * @param serviceRegistry the service registry
   * @param nodeFactory the node factory
   */
  public ConfigurationPersistenceService(
      final @NonNull ServiceRegistry serviceRegistry, final @NonNull NodeFactory nodeFactory) {
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
  }

  // TODO: Add user notifications on minor configuration read errors during initial load.
  // TODO: Introduce exceptions to reject reload of invalid configuration.

  /**
   * Creates a new Configuration object based on a configuration YAML file.
   *
   * @param configFile the config file
   * @param initialLoad the initial load flag. When true minor errors will be ignored.
   * @return the configuration
   * @throws InvalidConfigurationException when configuration cannot be properly read
   * @throws MissingConfigurationException when configuration file is missing
   */
  public Configuration load(final @NonNull File configFile, final boolean initialLoad)
      throws InvalidConfigurationException, MissingConfigurationException {

    final Yaml yaml = new Yaml(yamlOptions);
    final Map<String, ?> configurationDto;
    try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(configFile))) {
      configurationDto = yaml.load(inputStream);
    } catch (final FileNotFoundException e) {
      log.info("Configuration file ({}) not present.", configFile);
      throw new MissingConfigurationException("MISSING_CFG_FILE", e, configFile);
    } catch (final IOException | ClassCastException e) {
      log.info("Failed to read configuration file ({}): {}", configFile, e.getLocalizedMessage());
      throw new InvalidConfigurationException("UNREADABLE_CFG_FILE", e, configFile);
    }

    if (configurationDto == null) {
      throw new InvalidConfigurationException("UNREADABLE_CFG_FILE", configFile);
    }

    if (configurationDto.get(LOCALE_PROPERTY) != null) {
      LocaleUtil.setLocale(
          Locale.forLanguageTag(String.valueOf(configurationDto.get(LOCALE_PROPERTY))));
    }

    final Object version = configurationDto.get(VERSION_PROPERTY);
    if (VERSION.equals(version)) {
      // Read Network nodes configuration
      final List<NetworkNode> networkNodes;
      final Object serversDto = configurationDto.get(SERVERS_PROPERTY);
      if (serversDto instanceof List) {
        networkNodes = nodeFactory.createFrom((List<?>) serversDto);
      } else {
        log.warn("Failed to read server list.");
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
          }
        }

      } else {
        log.warn("Failed to read application modules configuration.");
      }

      // Read service configuration
      final Object servicesDto = configurationDto.get(SERVICES_PROPERTY);
      if (servicesDto instanceof Map) {
        for (final Map.Entry<?, ?> entry : ((Map<?, ?>) servicesDto).entrySet()) {
          final String serviceId = StringUtils.toStringOrEmpty(entry.getKey());
          final String serviceOptions = StringUtils.toStringOrEmpty(entry.getValue());
          serviceRegistry.configureService(serviceId, serviceOptions);
        }
      } else {
        log.warn("Failed to read services list.");
      }

      return new Configuration(networkNodes, modules);
    } else {
      throw new InvalidConfigurationException("UNKNOWN_VERSION", configFile, version);
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
      log.info("Failed to write configuration file ({}): {}", configFile, e.getLocalizedMessage());
      throw new FailedConfigurationSaveException(
          "CONFIG_SAVE_ERROR", e, configFile, e.getLocalizedMessage());
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
