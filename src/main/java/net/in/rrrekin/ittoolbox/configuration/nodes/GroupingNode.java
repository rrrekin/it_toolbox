package net.in.rrrekin.ittoolbox.configuration.nodes;

import static com.google.common.base.Preconditions.checkArgument;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.enMessage;
import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a group of other network nodes.
 *
 * @author michal.rudewicz @gmail.com
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class GroupingNode implements NetworkNode {

  /** Separator used to join parent hierarchy of network nodes. */
  private static final char LOCATION_PATH_SEPARATOR = '/';

  @Getter @Setter private @NonNull String name;
  @Getter @Setter private @NonNull String description;
  @Getter private final @NotNull List<NetworkNode> childNodes;
  @Getter private final @NonNull List<String> serviceDescriptors;

  /**
   * Instantiates a new Grouping node.
   *
   * @param name the name
   */
  public GroupingNode(final @NonNull String name) {
    this.name = name;
    description = name;
    childNodes = Lists.newArrayList();
    serviceDescriptors = Lists.newArrayList();
  }

  /**
   * Instantiates a new Grouping node.
   *
   * @param dto the dto
   * @param factory the factory
   */
  public GroupingNode(
      final @NonNull Map<String, Object> dto,
      final @NonNull NodeFactory factory,
      @NonNls final @NonNull String parentInfo) {
    final String type = toStringOrEmpty(dto.get(TYPE_PROPERTY));
    checkArgument(
        NodeType.GROUP.getTypeName().equalsIgnoreCase(type),
        enMessage("NODE_CONSTRUCTOR_TYPE_MISMATCH"),
        type);
    name = toStringOrEmpty(dto.get(NAME_PROPERTY));
    description = toStringOrEmpty(dto.get(DESCRIPTION_PROPERTY));
    childNodes = Lists.newArrayList();
    serviceDescriptors = Lists.newArrayList();
    // TODO: implement notifications on errors in service and child lists (also for other node types)
    if (dto.get(SERVICES_PROPERTY) instanceof List) {
      ((List<?>) dto.get(SERVICES_PROPERTY))
          .forEach(it -> serviceDescriptors.add(toStringOrEmpty(it)));
    }
    final Object childNodesDtos = dto.get(CHILD_NODES_PROPERTY);
    if (childNodesDtos instanceof List) {
      childNodes.addAll(
          factory.createNodeList(
              (List<?>) childNodesDtos, parentInfo + LOCATION_PATH_SEPARATOR + name));
    }
  }

  @Override
  public @NotNull ImageIcon getIcon() {
    return new ImageIcon();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public @NotNull Map<String, Object> getDtoProperties() {
    final Map<String, Object> response = Maps.newLinkedHashMap();
    response.put(TYPE_PROPERTY, NodeType.GROUP.getTypeName());
    response.put(NAME_PROPERTY, name);
    response.put(DESCRIPTION_PROPERTY, description);
    response.put(
        CHILD_NODES_PROPERTY,
        childNodes.stream().map(NetworkNode::getDtoProperties).collect(Collectors.toList()));
    response.put(SERVICES_PROPERTY, serviceDescriptors);
    return response;
  }
}
