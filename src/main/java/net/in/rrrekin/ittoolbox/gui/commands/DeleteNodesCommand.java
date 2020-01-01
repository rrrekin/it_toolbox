package net.in.rrrekin.ittoolbox.gui.commands;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;

/** @author michal.rudewicz@gmail.com */
public class DeleteNodesCommand extends OperationCommand {

  private final @NotNull TreeItem<NetworkNode> root;
  private final @NotNull List<ItemInfo> toDelete;

  public DeleteNodesCommand(
      final @NotNull IntegerProperty stateSerialNumber,
      final @NotNull ObservableList<TreeItem<NetworkNode>> selection) {
    super(stateSerialNumber);
    requireNonNull(selection, "selection must not be null");
    checkArgument(!selection.isEmpty(), "selectoin cannot be empty");
    selection.forEach(
        item -> checkArgument(item != null, "selection cannot contain null elements"));
    root = findRoot(selection.get(0));
    final List<TreeItem<NetworkNode>> toDelete = new ArrayList<>(selection);
    removeChildren(toDelete);
    this.toDelete =
        toDelete.stream()
            .map(item -> new ItemInfo(getItemLocation(item), item))
            .sorted()
            .collect(Collectors.toList());
  }

  private static void removeChildren(final List<TreeItem<NetworkNode>> list) {
    final List<TreeItem<NetworkNode>> toRemove = new ArrayList<>();
    for (final TreeItem<NetworkNode> item : list) {
      for (TreeItem<NetworkNode> ancestor = item.getParent();
          ancestor != null;
          ancestor = ancestor.getParent()) {
        if (list.contains(ancestor)) {
          toRemove.add(item);
          break;
        }
      }
    }
    list.removeAll(toRemove);
  }

  @Override
  public String getDescription() {
    return localMessage("OP_DELETE");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("toDelete", toDelete).toString();
  }

  @Override
  public TreeItem<NetworkNode> execute() {
    super.execute();
    toDelete.forEach(item -> item.getItem().getParent().getChildren().remove(item.getItem()));
    return null;
  }

  @Override
  public void revoke() {
    super.revoke();
    toDelete.forEach(
        item -> {
          final @NotNull int[] itemLocation = item.getLocation();
          final int lastIndex = itemLocation.length - 1;
          final @NotNull TreeItem<NetworkNode> parent =
              locateItem(Arrays.copyOfRange(itemLocation, 0, lastIndex), root);
          parent.getChildren().add(itemLocation[lastIndex], item.getItem());
        });
  }

  static class ItemInfo implements Comparable<ItemInfo> {

    @NotNull private final int[] location;
    @NotNull private final TreeItem<NetworkNode> item;

    ItemInfo(@NotNull final int[] location, @NotNull final TreeItem<NetworkNode> item) {
      this.location = location;
      this.item = item;
    }

    public @NotNull int[] getLocation() {
      return location;
    }

    public @NotNull TreeItem<NetworkNode> getItem() {
      return item;
    }

    @Override
    public String toString() {
      return printablePath(location);
    }

    @Override
    public int compareTo(@NotNull final ItemInfo other) {
      if (other == null) {
        return 1;
      }
      if (other == this) {
        return 0;
      }
      if (location.length < other.location.length) {
        return -1;
      } else if (location.length > other.location.length) {
        return 1;
      }
      // both items have the same location length
      for (int i = 0; i < location.length; i++) {
        if (location[i] < other.location[i]) {
          return -1;
        } else if (location[i] > other.location[i]) {
          return 1;
        }
      }

      return 0;
    }
  }
}
