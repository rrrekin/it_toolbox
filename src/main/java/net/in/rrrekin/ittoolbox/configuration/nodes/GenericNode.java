package net.in.rrrekin.ittoolbox.configuration.nodes;

import static java.util.Objects.requireNonNullElse;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.paint.Color;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import org.controlsfx.glyphfont.FontAwesome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an entity tha has no address, e.g. vlan, clustered application instance, etc.
 *
 * @author michal.rudewicz @gmail.com
 */
public class GenericNode implements NetworkNode {

  private static final @NotNull IconDescriptor DEFAULT_ICON_DESCRIPTOR =
      new IconDescriptor(FontAwesome.Glyph.CUBES, Color.BLUE, true);

  private final @NotNull String name;
  private final @NotNull String description;
  private final @NotNull IconDescriptor iconDescriptor;
  private final @NotNull ImmutableMap<String, String> properties;
  private final @NotNull ImmutableList<String> serviceDescriptors;

  /**
   * Instantiates a new Generic node.
   *
   * @param name the name
   * @param description the description
   * @param iconDescriptor the icon descriptor
   * @param properties the properties
   * @param serviceDescriptors the service descriptors
   */
  public GenericNode(
      final @Nullable String name,
      final @Nullable String description,
      final @Nullable IconDescriptor iconDescriptor,
      final @Nullable Map<String, String> properties,
      final @Nullable List<String> serviceDescriptors) {

    this.name = requireNonNullElse(name, localMessage("NODE_GENERIC_NODE_DEFAULT_NAME"));
    this.description = requireNonNullElse(description, this.name);
    this.iconDescriptor = requireNonNullElse(iconDescriptor, DEFAULT_ICON_DESCRIPTOR);
    this.properties = ImmutableMap.copyOf(requireNonNullElse(properties, ImmutableMap.of()));
    this.serviceDescriptors =
        ImmutableList.copyOf(requireNonNullElse(serviceDescriptors, ImmutableList.of()));
  }

  /**
   * Instantiates a new Generic node.
   *
   * @param name the name
   */
  public GenericNode(final @Nullable String name) {
    this.name = requireNonNullElse(name, localMessage("NODE_GENERIC_NODE_DEFAULT_NAME"));
    this.description = this.name;
    properties = ImmutableMap.of();
    serviceDescriptors = ImmutableList.of();
    iconDescriptor = DEFAULT_ICON_DESCRIPTOR;
  }

  /** Instantiates a new Generic node. */
  public GenericNode() {
    this(null);
  }

  @Override
  public NodeType getType() {
    return NodeType.GENERIC_NODE;
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull String getDescription() {
    return description;
  }

  @Override
  public @NotNull IconDescriptor getIconDescriptor() {
    return iconDescriptor;
  }

  @Override
  public @NotNull ImmutableList<String> getServiceDescriptors() {
    return serviceDescriptors;
  }

  @Override
  public @NotNull ImmutableMap<String, String> getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("description", description)
      .add("iconDescriptor", iconDescriptor)
      .add("properties", properties)
      .add("serviceDescriptors", serviceDescriptors)
      .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final GenericNode that = (GenericNode) o;
    return name.equals(that.name) &&
      description.equals(that.description) &&
      iconDescriptor.equals(that.iconDescriptor) &&
      properties.equals(that.properties) &&
      serviceDescriptors.equals(that.serviceDescriptors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, iconDescriptor, properties, serviceDescriptors);
  }
}
