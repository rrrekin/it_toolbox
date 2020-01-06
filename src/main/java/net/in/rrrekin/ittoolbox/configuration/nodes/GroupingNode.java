package net.in.rrrekin.ittoolbox.configuration.nodes;

import static java.util.Objects.requireNonNullElse;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.paint.Color;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import org.controlsfx.glyphfont.FontAwesome.Glyph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a group of other network nodes.
 *
 * @author michal.rudewicz @gmail.com
 */
public class GroupingNode implements NetworkNode {

  private static final @NotNull IconDescriptor DEFAULT_ICON_DESCRIPTOR =
      new IconDescriptor(Glyph.FOLDER_OPEN, Color.BLUE, true);

  private final @NotNull String name;
  private final @NotNull String description;
  private final @NotNull IconDescriptor iconDescriptor;
  private final @NotNull ImmutableList<ServiceDescriptor> serviceDescriptors;

  public GroupingNode(
      final @Nullable String name,
      final @Nullable String description,
      final @Nullable IconDescriptor iconDescriptor,
      final @Nullable List<ServiceDescriptor> serviceDescriptors) {
    this.name = requireNonNullElse(name, localMessage("NODE_GROUPING_NODE_DEFAULT_NAME"));
    this.description = requireNonNullElse(description, this.name);
    this.iconDescriptor = requireNonNullElse(iconDescriptor, DEFAULT_ICON_DESCRIPTOR);
    this.serviceDescriptors =
        ImmutableList.copyOf(requireNonNullElse(serviceDescriptors, ImmutableList.of()));
  }

  /**
   * Instantiates a new Grouping node.
   *
   * @param name the name
   */
  public GroupingNode(final @Nullable String name) {
    this.name = requireNonNullElse(name, localMessage("NODE_GROUPING_NODE_DEFAULT_NAME"));
    this.description = this.name;
    serviceDescriptors = ImmutableList.of();
    iconDescriptor = DEFAULT_ICON_DESCRIPTOR;
  }

  public GroupingNode() {
    this(null);
  }

  @NotNull
  @Override
  public NodeType getType() {
    return NodeType.GROUP;
  }

  @Override
  public String getLocalNodeTypeName() {
    return localMessage("NODE_GROUP");

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
  public @NotNull ImmutableList<ServiceDescriptor> getServiceDescriptors() {
    return serviceDescriptors;
  }

  @Override
  public @NotNull ImmutableMap<String, String> getEnv() {
    final Builder<String, String> builder = ImmutableMap.builder();
    builder.put(ENV_VAR_NAME, name);
    builder.put(ENV_VAR_DESCRIPTION, description);
    return builder.build();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("description", description)
      .add("iconDescriptor", iconDescriptor)
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
    final GroupingNode that = (GroupingNode) o;
    return name.equals(that.name) &&
      description.equals(that.description) &&
      iconDescriptor.equals(that.iconDescriptor) &&
      serviceDescriptors.equals(that.serviceDescriptors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, iconDescriptor, serviceDescriptors);
  }
}
