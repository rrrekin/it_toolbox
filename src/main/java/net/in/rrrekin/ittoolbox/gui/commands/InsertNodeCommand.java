package net.in.rrrekin.ittoolbox.gui.commands;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Inserts single node into a given tree item.
 *
 * @author michal.rudewicz@gmail.com
 */
public class InsertNodeCommand extends OperationCommand {

  private final @NotNull TreeItem<NetworkNode> root;
  private final @NotNull int[] parentLocation;
  private @Nullable int position;
  private final @NotNull NetworkNode node;

  public InsertNodeCommand(
      final @NotNull IntegerProperty stateSerialNumber,
      final @NotNull InsertLocation insertionPoint,
      final @NotNull NetworkNode node) {
    super(stateSerialNumber);
    this.parentLocation =
        getItemLocation(
            requireNonNull(insertionPoint, "insertionPoint must not be null").getParent());
    this.position = insertionPoint.getIndex();
    this.node = requireNonNull(node, "node must not be null");
    this.root = findRoot(insertionPoint.getParent());
  }

  @Override
  public String getDescription() {
    return StringUtils.abbreviate(node.getName(), MAX_DESCRIPTION_LEN);
  }

  @Override
  public @Nullable TreeItem<NetworkNode> execute() {
    super.execute();
    final TreeItem<NetworkNode> parent = locateItem(parentLocation, root);
    final ObservableList<TreeItem<NetworkNode>> children = parent.getChildren();
    parent.setExpanded(true);
    final TreeItem<NetworkNode> newTreeItem =
        new TreeItem<>(node, node.getIconDescriptor().getIcon());
    if (position >= children.size()) {
      position = -1;
    }
    if (position < 0) {
      children.add(newTreeItem);
    } else {
      children.add(position, newTreeItem);
    }
    return newTreeItem;
  }

  @Override
  public void revoke() {
    super.revoke();
    final TreeItem<NetworkNode> parent = locateItem(parentLocation, root);
    final ObservableList<TreeItem<NetworkNode>> children = parent.getChildren();
    if (position < 0) {
      children.remove(children.size() - 1);
    } else {
      checkState(
          position >= 0 && position < children.size(),
          "Invalid position of insert command to undo.");
      children.remove(position);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("parentPath", printablePath(parentLocation))
        .add("position", position < 0 ? position : "end")
        .add("node", node)
        .toString();
  }
}
