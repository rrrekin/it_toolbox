package net.in.rrrekin.ittoolbox.gui.commands;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Collection;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.gui.model.NodeForest;
import net.in.rrrekin.ittoolbox.gui.model.NodeForest.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author michal.rudewicz@gmail.com */
public class InsertForestCommand extends OperationCommand {

  private final @NotNull TreeItem<NetworkNode> root;
  private final @NotNull int[] parentLocation;
  private final @NotNull NodeForest nodeForest;
  private final @NotNull String description;
  private final int insertionIndex;

  public InsertForestCommand(
      final IntegerProperty stateSerialNumber,
      final InsertLocation insertionPoint,
      final NodeForest nodeForest,
      final @NotNull String description) {
    super(stateSerialNumber);
    this.parentLocation =
        getItemLocation(requireNonNull(insertionPoint, "item must not be null").getParent());
    this.nodeForest = requireNonNull(nodeForest, "nodeForest must not be null");
    this.description = requireNonNull(description);
    this.root = findRoot(insertionPoint.getParent());
    this.insertionIndex = insertionPoint.getIndex();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public @Nullable TreeItem<NetworkNode> execute() {
    super.execute();
    final TreeItem<NetworkNode> parent = locateItem(parentLocation, root);
    addChildrenTo(nodeForest.getRoots(), parent, insertionIndex);
    return parent;
  }

  private static void addChildrenTo(
      final @NotNull Collection<Node> children,
      final @NotNull TreeItem<NetworkNode> parent,
      final int insertionIndex) {
    parent.setExpanded(true);
    int index = insertionIndex;
    for (final Node node : children) {
      final NetworkNode networkNode = node.getValue();
      final TreeItem<NetworkNode> newItem =
          new TreeItem<>(networkNode, networkNode.getIconDescriptor().getIcon());
      if (node.getChildren() != null) {
        addChildrenTo(node.getChildren(), newItem, -1);
      }
      if (index > 0 && index < parent.getChildren().size()) {
        parent.getChildren().add(index, newItem);
      } else {
        parent.getChildren().add(newItem);
      }
      index++;
    }
  }

  @Override
  public void revoke() {
    super.revoke();
    final TreeItem<NetworkNode> parent = locateItem(parentLocation, root);
    final ObservableList<TreeItem<NetworkNode>> children = parent.getChildren();
    if (insertionIndex < 0) {
      children.remove(children.size() - nodeForest.getRoots().size(), children.size());

    } else if (insertionIndex + nodeForest.getRoots().size() < children.size()) {
      children.remove(insertionIndex, insertionIndex + nodeForest.getRoots().size());
    } else {
      throw new IllegalStateException(
          "Undo of insert several items impossible as parent entry do not contain enough items");
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("parentLocation", printablePath(parentLocation))
        .add("content", nodeForest)
        .toString();
  }
}
