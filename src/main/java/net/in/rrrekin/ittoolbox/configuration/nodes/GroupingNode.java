package net.in.rrrekin.ittoolbox.configuration.nodes;

import static com.google.common.base.Preconditions.checkArgument;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.enMessage;
import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.Icon;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a group of other network nodes.
 *
 * @author michal.rudewicz @gmail.com
 */
@ToString
@RequiredArgsConstructor
public class GroupingNode implements NetworkNode {

  /** Separator used to join parent hierarchy of network nodes. */
  private static final char LOCATION_PATH_SEPARATOR = '/';

  @Getter @Setter private @NonNull String name;
  @Getter @Setter private @NonNull String description;
  @ToString.Exclude @Setter private @Nullable Icon icon = null;
  @ToString.Exclude @Getter private final @NotNull List<@NotNull NetworkNode> childNodes;
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
    // TODO: implement notifications on errors in service and child lists (also for other node
    // types)
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
  public @NotNull Icon getIcon() {
    if (icon == null) {
      icon = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.FOLDER_OPEN, NetworkNode.ICON_SIZE);
    }
    return icon;
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

  @NonNls
  @Override
  public String toHtml() {
    return "<h1>"
        + escapeHtml4(name)
        + "</h1><p><i>"
        + escapeHtml4(description)
        + "</i></p>";
  }
}
