package net.in.rrrekin.ittoolbox.configuration.nodes;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter of {@link NetworkNode} object to/from generic Map DTO.
 *
 * @author michal.rudewicz @gmail.com
 */
@SuppressWarnings("MethodMayBeStatic")
public class NodeConverter {

  /** The constant TYPE key in configuration DTO. */
  public static final String TYPE_PROPERTY = "type";
  /** The constant NAME key in configuration DTO. */
  public static final String NAME_PROPERTY = "name";
  /** The constant ICON key in configuration DTO. */
  public static final String ICON_PROPERTY = "icon";
  /** The constant ADDRESS key in configuration DTO. */
  public static final String ADDRESS_PROEPRTY = "address";
  /** The constant DESCRIPTION key in configuration DTO. */
  public static final String DESCRIPTION_PROPERTY = "description";
  /** The constant CHILD_NODES key in configuration DTO. */
  public static final String CHILD_NODES_PROPERTY = "children";
  /** The constant SERVICES_PROPERTY key in configuration DTO. */
  public static final String SERVICES_PROPERTY = "services";
  /** The constant PROPERTIES_PREFIX for custom properties in configuration DTO. */
  @NonNls public static final String PROPERTIES_PREFIX = "_";

  private static final int MAX_DESCRIPTION_WIDTH = 40;
  private static final Logger log = LoggerFactory.getLogger(NodeConverter.class);

  private final @NotNull AppPreferences appPreferences;

  @Inject
  public NodeConverter(final AppPreferences appPreferences) {
    this.appPreferences = requireNonNull(appPreferences, "AppPreferences must not be null");
  }

  /**
   * Create network node from Map DTO.
   *
   * @param dto the DTO
   * @return the network node
   */
  public @NotNull NetworkNode convertTo(final @NotNull Map<?, ?> dto)
      throws InvalidConfigurationException {
    final String typeName = String.valueOf(dto.get(TYPE_PROPERTY));
    final NodeType type = NodeType.of(typeName);
    if (type == null) {
      throw new InvalidConfigurationException(
          "EX_CANNOT_CREATE",
          StringUtils.isBlank(typeName) ? localMessage("CFG_UNKNOWN_TYPE") : typeName);
    }
    switch (type) {
      case SERVER:
        return convertToServer(dto);
      case GENERIC_NODE:
        return convertToGenericNode(dto);
      case GROUP:
        return convertToGroupingNode(dto);
      default:
        throw new InvalidConfigurationException(
            "EX_CANNOT_CREATE",
            StringUtils.isBlank(typeName) ? localMessage("CFG_UNKNOWN_TYPE") : typeName);
    }
  }

  public @NotNull ImmutableMap<String, Object> convertFrom(final @Nullable NetworkNode node) {
    final ImmutableMap.Builder<String, Object> response = ImmutableMap.builder();
    response.put(TYPE_PROPERTY, node.getType().getTypeName());
    response.put(NAME_PROPERTY, node.getName());
    if (node instanceof Server) {
      response.put(ADDRESS_PROEPRTY, ((Server) node).getAddress());
    }
    response.put(ICON_PROPERTY, node.getIconDescriptor().toString());
    response.put(DESCRIPTION_PROPERTY, node.getDescription());
    if (!node.getServiceDescriptors().isEmpty()) {
      response.put(SERVICES_PROPERTY, node.getServiceDescriptors());
    }
    node.getProperties()
        .forEach((property, value) -> response.put(PROPERTIES_PREFIX + property, value));
    return response.build();
  }

  private @NotNull Server convertToServer(final @NotNull Map<?, ?> dto) {
    final String name = String.valueOf(dto.get(NAME_PROPERTY));
    final String address = String.valueOf(dto.get(ADDRESS_PROEPRTY));
    final String description = String.valueOf(dto.get(DESCRIPTION_PROPERTY));
    final IconDescriptor iconDescriptor =
        convertToIconDescriptor(String.valueOf(dto.get(ICON_PROPERTY)));
    final HashMap<String, String> properties = Maps.newHashMap();
    final List<String> serviceDescriptors = newArrayList();
    if (dto.get(SERVICES_PROPERTY) instanceof List) {
      ((List<?>) dto.get(SERVICES_PROPERTY))
          .forEach(
              it -> {
                final String service = toStringOrEmpty(it);
                if (!service.trim().isEmpty()) {
                  serviceDescriptors.add(service);
                }
              });
    }
    for (final Map.Entry<?, ?> entry : dto.entrySet()) {
      final String property = String.valueOf(entry.getKey());
      final Object value = entry.getValue();
      if (property != null && property.startsWith(PROPERTIES_PREFIX)) {
        final String propertyName = property.substring(PROPERTIES_PREFIX.length());
        properties.put(propertyName, toStringOrEmpty(value));
      }
    }

    return new Server(name, address, description, iconDescriptor, properties, serviceDescriptors);
  }

  private @NotNull GenericNode convertToGenericNode(final @NotNull Map<?, ?> dto) {
    final String name = String.valueOf(dto.get(NAME_PROPERTY));
    final String description = String.valueOf(dto.get(DESCRIPTION_PROPERTY));
    final IconDescriptor iconDescriptor =
        convertToIconDescriptor(String.valueOf(dto.get(ICON_PROPERTY)));
    final HashMap<String, String> properties = Maps.newHashMap();
    final List<String> serviceDescriptors = newArrayList();
    if (dto.get(SERVICES_PROPERTY) instanceof List) {
      ((List<?>) dto.get(SERVICES_PROPERTY))
          .forEach(
              it -> {
                final String service = toStringOrEmpty(it);
                if (!service.trim().isEmpty()) {
                  serviceDescriptors.add(service);
                }
              });
    }
    for (final Map.Entry<?, ?> entry : dto.entrySet()) {
      final String property = String.valueOf(entry.getKey());
      final Object value = entry.getValue();
      if (property != null && property.startsWith(PROPERTIES_PREFIX)) {
        final String propertyName = property.substring(PROPERTIES_PREFIX.length());
        properties.put(propertyName, toStringOrEmpty(value));
      }
    }

    return new GenericNode(name, description, iconDescriptor, properties, serviceDescriptors);
  }

  private @NotNull GroupingNode convertToGroupingNode(final @NotNull Map<?, ?> dto) {
    final String name = String.valueOf(dto.get(NAME_PROPERTY));
    final String description = String.valueOf(dto.get(DESCRIPTION_PROPERTY));
    final IconDescriptor iconDescriptor =
        convertToIconDescriptor(String.valueOf(dto.get(ICON_PROPERTY)));
    final List<String> serviceDescriptors = newArrayList();
    if (dto.get(SERVICES_PROPERTY) instanceof List) {
      ((List<?>) dto.get(SERVICES_PROPERTY))
          .forEach(
              it -> {
                final String service = toStringOrEmpty(it);
                if (!service.trim().isEmpty()) {
                  serviceDescriptors.add(service);
                }
              });
    }
    return new GroupingNode(name, description, iconDescriptor, serviceDescriptors);
  }

  private @Nullable IconDescriptor convertToIconDescriptor(final @Nullable String description) {
    return description == null ? null : IconDescriptor.of(description);
  }

  public @NotNull Collection<Node> toGui(final @Nullable TreeItem<NetworkNode> item) {
    final List<Node> response = newArrayList();
    if (item != null && item.getValue() != null) {
      final Font fontHeader =
          Font.font(
              appPreferences.getFontFamily(), FontWeight.BOLD, appPreferences.getFontSize() * 2.5);
      final Font fontSubHeader =
          Font.font(
              appPreferences.getFontFamily(), FontWeight.BOLD, appPreferences.getFontSize() * 1.5);
      final Font fontNote =
          Font.font(
              appPreferences.getFontFamily(), FontPosture.ITALIC, appPreferences.getFontSize());
      final Font fontLabel =
          Font.font(
              appPreferences.getFontFamily(), FontWeight.BOLD, appPreferences.getFontSize() * 1.2);
      final Font fontValue =
          Font.font(appPreferences.getFontFamily(), appPreferences.getFontSize() * 1.2);

      final @NotNull NetworkNode node = item.getValue();
      response.add(node.getIconDescriptor().getIcon().size(appPreferences.getFontSize() * 5.0));
      response.add(createText(fontHeader, "\n", " ", node.getName(), "\n"));
      response.add(createText(fontNote, localMessage("NODE_SERVER"), "\n"));
      if (node instanceof Server) {
        response.add(createText(fontLabel, "\n", localMessage("NODE_LABEL_ADDRESS"), " "));
        response.add(createText(fontValue, ((Server) node).getAddress(), "\n"));
      }
      response.add(createText(fontSubHeader, "\n", localMessage("NODE_LABEL_DESCRIPTION"), "\n"));
      response.add(createText(fontValue, node.getDescription(), "\n"));
      if (!node.getProperties().isEmpty()) {
        response.add(createText(fontSubHeader, "\n", localMessage("NODE_LABEL_PROPERTIES"), "\n"));
        node.getProperties().keySet().stream()
            .sorted()
            .forEach(
                property -> {
                  response.add(createText(fontLabel, property, " "));
                  response.add(createText(fontValue, node.getProperties().get(property), "\n"));
                });
      }
      if (!node.getServiceDescriptors().isEmpty()) {
        response.add(createText(fontSubHeader, "\n", localMessage("NODE_LABEL_SERVICES"), "\n"));
        node.getServiceDescriptors().stream()
            .sorted()
            .forEach(
                service -> {
                  response.add(createText(fontValue, service, "\n"));
                });
      }
    }

    return response;
  }

  private Text createText(final Font font, final String... strings) {
    final Text text = new Text(String.join("", Arrays.asList(strings)));
    text.setFont(font);
    return text;
  }
}
