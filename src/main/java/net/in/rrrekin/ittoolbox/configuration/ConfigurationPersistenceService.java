package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.configuration.nodes.NodeConverter.CHILD_NODES_PROPERTY;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeConverter;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceRegistry;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.NonPrintableStyle;
import org.yaml.snakeyaml.DumperOptions.Version;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Service responsible for saving and loading application configuration.
 *
 * @author michal.rudewicz @gmail.com
 */
@Singleton
public class ConfigurationPersistenceService {

  private static final String VERSION = "1.0";
  private static final String VERSION_PROPERTY = "version";
  private static final String SERVERS_PROPERTY = "servers";
  private static final String MODULES_PROPERTY = "modules";
  private static final String SERVICES_PROPERTY = "services";
  private static final int YAML_LINE_WIDTH = 130;
  private static final int MAX_OBJECT_DESCRIPTION_WIDTH = 40;

  @NonNls
  private static final Logger log = LoggerFactory.getLogger(ConfigurationPersistenceService.class);

  @NonNls public static final String ROOT = "root";

  private final @NotNull DumperOptions yamlOptions;
  private final @NotNull ServiceRegistry serviceRegistry;
  private final @NotNull NodeConverter nodeConverter;
  //  private final @NonNull EventBus eventBus;

  /**
   * Instantiates a new Configuration persistence service.
   *
   * @param serviceRegistry the service registry
   */
  @Inject
  public ConfigurationPersistenceService(
      final @NotNull ServiceRegistry serviceRegistry, final @NotNull NodeConverter nodeConverter) {
    log.debug("Initializing ConfigurationPersistenceService");
    yamlOptions = new DumperOptions();
    yamlOptions.setIndent(4);
    yamlOptions.setAllowUnicode(true);
    yamlOptions.setIndicatorIndent(2);
    yamlOptions.setNonPrintableStyle(NonPrintableStyle.ESCAPE);
    yamlOptions.setWidth(YAML_LINE_WIDTH);
    yamlOptions.setSplitLines(true);
    yamlOptions.setVersion(Version.V1_1);
    yamlOptions.setPrettyFlow(true);
    yamlOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
    this.serviceRegistry = requireNonNull(serviceRegistry, "ServiceRegistry must not be null");
    this.nodeConverter = requireNonNull(nodeConverter, "NodeConverter must not be null");
  }

  /**
   * Creates a new Configuration object based on a configuration YAML file.
   *
   * @param configFile the config file
   * @return the configuration
   * @throws InvalidConfigurationException when configuration cannot be properly read
   * @throws MissingConfigurationException when configuration file is missing
   */
  public @NotNull ReadResult load(final @NotNull File configFile)
      throws InvalidConfigurationException, MissingConfigurationException {

    log.info("Loading configuration from '{}'", configFile);
    requireNonNull(configFile, "ConfigFile must not be null");
    final Yaml yaml = new Yaml(yamlOptions);
    final Map<String, ?> configurationDto;
    try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(configFile))) {
      configurationDto = yaml.load(inputStream);
    } catch (final FileNotFoundException e) {
      log.warn("Configuration file ({}) not present.", configFile);
      throw new MissingConfigurationException("EX_MISSING_CFG_FILE", e, configFile);
    } catch (final YAMLException | IOException | ClassCastException e) {
      log.warn("Failed to read configuration file ({}): {}", configFile, e.getLocalizedMessage());
      throw new InvalidConfigurationException("EX_UNREADABLE_CFG_FILE", e, configFile);
    }

    if (configurationDto == null) {
      throw new InvalidConfigurationException("EX_UNREADABLE_CFG_FILE", configFile);
    }

    final Object version = configurationDto.get(VERSION_PROPERTY);
    if (VERSION.equals(version)) {
      final ReadResult result = readConfigV1(configurationDto);
      result.configuration.setFilePath(configFile.getAbsoluteFile().toPath().normalize());
      return result;
    } else {
      throw new InvalidConfigurationException("EX_UNKNOWN_VERSION", configFile, version);
    }
  }

  public @NotNull TreeItem<NetworkNode> treeFromYaml(final @NotNull String yamlData)
      throws InvalidConfigurationException {
    requireNonNull(yamlData, "yamlData must not be null");
    final Yaml yaml = new Yaml(yamlOptions);
    final Object deserialized = yaml.load(yamlData);
    log.debug("Deserialized content: {}", deserialized);
    if (deserialized instanceof Map) {
      // Attempting to interpret as a whole configuration entry
      final List<String> warnings = newArrayList();
      final TreeItem<NetworkNode> rootNode =
          readNetworkNodesV1((Map<String, ?>) deserialized, warnings);
      if (warnings.isEmpty()) {
        return rootNode;
      }
    } else if (deserialized instanceof List) {
      final List<String> warnings = newArrayList();
      final @NotNull List<TreeItem<NetworkNode>> servers =
          readServerList((List) deserialized, warnings);
      if (warnings.isEmpty()) {
        final TreeItem<NetworkNode> treeItem = new TreeItem<>(new GroupingNode("paste-root"));
        treeItem.getChildren().addAll(servers);
        return treeItem;
      }
    }
    throw new IllegalArgumentException(
        "Text cannot be correctly interpretted as a configuration YAML");
  }

  private @NotNull ReadResult readConfigV1(final @NotNull Map<String, ?> configurationDto)
      throws InvalidConfigurationException {
    final List<String> warnings = newArrayList();
    final TreeItem<NetworkNode> rootNode = readNetworkNodesV1(configurationDto, warnings);
    final Map<String, Map<String, String>> modules = readModulesV1(configurationDto, warnings);
    readServicesV1(configurationDto, warnings);

    return new ReadResult(new Configuration(rootNode, modules), warnings);
  }

  private @NotNull TreeItem<NetworkNode> readNetworkNodesV1(
      final @NotNull Map<String, ?> configurationDto, final @NotNull List<String> warnings)
      throws InvalidConfigurationException {
    final TreeItem<NetworkNode> rootNode = new TreeItem<>(new GroupingNode(ROOT));
    final Object serversDto = configurationDto.get(SERVERS_PROPERTY);
    if (serversDto != null) {
      if (serversDto instanceof List) {
        rootNode.getChildren().addAll(readServerList((List<?>) serversDto, warnings));
      } else {
        log.warn("Failed to read server list.");
        throw new InvalidConfigurationException("CFG_INVALID_NODE_LIST");
      }
    } else {
      log.warn("Missing network node list");
      warnings.add(localMessage("CFG_SERVER_LIST_MISSING"));
    }
    return rootNode;
  }

  private void readServicesV1(
      final @NotNull Map<String, ?> configurationDto, final @NotNull List<String> warnings) {
    final Object servicesDto = configurationDto.get(SERVICES_PROPERTY);
    if (servicesDto instanceof Map) {
      for (final Entry<?, ?> entry : ((Map<?, ?>) servicesDto).entrySet()) {
        final String serviceId = StringUtils.toStringOrEmpty(entry.getKey());
        final String serviceOptions = StringUtils.toStringOrEmpty(entry.getValue());
        try {
          serviceRegistry.configureService(serviceId, serviceOptions);
        } catch (final Exception e) {
          log.warn("Failed to configure service {}.", serviceId, e);
          warnings.add(
              localMessage(
                  "CFG_INVALID_SERVICE_CONFIGURATION",
                  serviceId,
                  abbreviated(serviceOptions),
                  MAX_OBJECT_DESCRIPTION_WIDTH));
        }
      }
    } else {
      log.warn("Failed to read services list.");
      warnings.add(localMessage("CFG_INVALID_SERVICES_SECTION"));
    }
  }

  private static @NotNull Map<String, Map<String, String>> readModulesV1(
      final @NotNull Map<String, ?> configurationDto, final @NotNull List<String> warnings) {
    final Map<String, Map<String, String>> modules = Maps.newHashMap();
    final Object modulesDto = configurationDto.get(MODULES_PROPERTY);
    if (modulesDto != null) {
      if (modulesDto instanceof Map) {
        for (final Entry<?, ?> entry : ((Map<?, ?>) modulesDto).entrySet()) {
          final String moduleId = StringUtils.toStringOrEmpty(entry.getKey());
          final Object optionsDto = entry.getValue();
          if (optionsDto instanceof Map) {
            final Map<String, String> moduleConfig = Maps.newHashMap();
            modules.put(moduleId, moduleConfig);
            for (final Entry<?, ?> optionEntry : ((Map<?, ?>) optionsDto).entrySet()) {
              final String optionId = StringUtils.toStringOrEmpty(optionEntry.getKey());
              final String optionValue = StringUtils.toStringOrEmpty(optionEntry.getValue());
              moduleConfig.put(optionId, optionValue);
            }
          } else {
            log.warn("Failed to read options for module {}", moduleId);
            warnings.add(
                localMessage(
                    "CFG_INVALID_MODULE_OPTIONS",
                    moduleId,
                    abbreviate(String.valueOf(optionsDto), MAX_OBJECT_DESCRIPTION_WIDTH)));
          }
        }
      } else {
        log.warn("Failed to read application modules configuration.");
        warnings.add(localMessage("CFG_INVALID_MODULE_LIST"));
      }
    }
    return modules;
  }

  private @NotNull List<TreeItem<NetworkNode>> readServerList(
      final @NotNull Iterable<?> serversDto, final @NotNull Collection<String> warnings) {
    final ImmutableList.Builder<TreeItem<NetworkNode>> response = ImmutableList.builder();
    for (final Object dto : serversDto) {
      if (dto instanceof Map) {
        try {
          response.add(readNode((Map) dto, warnings));
        } catch (final InvalidConfigurationException e) {
          warnings.add(localMessage("CFG_INVALID_OBJECT_ON_DTO_LIST", abbreviated(dto)));
        }
      } else {
        warnings.add(localMessage("CFG_INVALID_OBJECT_ON_DTO_LIST", abbreviated(dto)));
      }
    }
    return response.build();
  }

  private @NotNull TreeItem<NetworkNode> readNode(
      final @NotNull Map<?, ?> dto, final @NotNull Collection<String> warnings)
      throws InvalidConfigurationException {
    final NetworkNode node = nodeConverter.convertTo(dto);
    final TreeItem<NetworkNode> response = new TreeItem<>(node, node.getIconDescriptor().getIcon());
    if (!node.isLeaf()) {
      final Object children = dto.get(CHILD_NODES_PROPERTY);
      if (children != null) {
        if (children instanceof List) {
          response.getChildren().addAll(readServerList((List) children, warnings));
        } else {
          warnings.add(
              localMessage(
                  "CFG_INVALID_OBJECT_ON_DTO_LIST2", CHILD_NODES_PROPERTY, abbreviated(children)));
        }
      }
    }
    return response;
  }

  private static @Nullable String abbreviated(final @Nullable Object dto) {
    return abbreviate(
        StringEscapeUtils.escapeJava(String.valueOf(dto)), MAX_OBJECT_DESCRIPTION_WIDTH);
  }

  public static final class ReadResult {
    public final @NotNull Configuration configuration;
    public final @NotNull ImmutableList<String> warnings;

    ReadResult(
        final @NotNull Configuration configuration, final @NotNull Iterable<String> warnings) {
      this.configuration = configuration;
      this.warnings = ImmutableList.copyOf(warnings);
    }

    public boolean isClean() {
      return warnings.isEmpty();
    }
  }

  /**
   * Save configuration to file.
   *
   * @param configFile the config file
   * @param config the config
   * @throws FailedConfigurationSaveException when unable to save the configuration to the file
   */
  public void save(final @NotNull File configFile, final @NotNull Configuration config)
      throws FailedConfigurationSaveException {
    requireNonNull(configFile, "ConfigFile must not be null");
    requireNonNull(config, "Config must not be null");
    if (configFile.isFile()) {
      final File backupFile = new File(configFile.getParent(), "." + configFile.getName() + "~");
      log.debug("Creating backup file '{}' for '{}'", backupFile, configFile);
      backupFile.delete();
      configFile.renameTo(backupFile);
    }
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
        config.getNetworkNodes().getChildren().stream()
            .map(this::getDto)
            .collect(Collectors.toList());
    final Map<String, ?> services =
        serviceRegistry.stream()
            .collect(
                Collectors.toMap(
                    serviceDefinition -> serviceDefinition.getType().toString(),
                    ServiceDefinition::getConfiguration));

    final Builder<String, Object> responseBuilder = ImmutableMap.builder();
    responseBuilder.put(VERSION_PROPERTY, VERSION);
    responseBuilder.put(SERVERS_PROPERTY, serversDto);
    responseBuilder.put(SERVICES_PROPERTY, services);
    responseBuilder.put(MODULES_PROPERTY, config.getModules());

    return responseBuilder.build();
  }

  private @NotNull Map<String, ?> getDto(final @NotNull TreeItem<NetworkNode> item) {
    final NetworkNode node = item.getValue();
    if (node.isLeaf()) {
      return nodeConverter.convertFrom(node);
    } else {
      final Builder<String, Object> response = ImmutableMap.builder();
      response.putAll(nodeConverter.convertFrom(node));
      response.put(
          CHILD_NODES_PROPERTY,
          item.getChildren().stream().map(this::getDto).collect(Collectors.toList()));
      return response.build();
    }
  }
}
