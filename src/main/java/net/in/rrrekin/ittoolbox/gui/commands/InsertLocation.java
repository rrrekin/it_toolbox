package net.in.rrrekin.ittoolbox.gui.commands;

import static java.util.Objects.requireNonNull;

import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents insertion point location.
 *
 * @author michal.rudewicz @gmail.com
 */
public class InsertLocation {
  private final @NotNull TreeItem<NetworkNode> parent;
  private final int index;

  /**
   * Instantiates a new Insert location.
   *
   * @param parent the parent tree item
   * @param index the index on children list. -1 for last position
   */
  public InsertLocation(
      final @NotNull TreeItem<NetworkNode> parent, final int index) {
    this.parent = requireNonNull(parent);
    this.index = index;
  }

  /**
   * Gets parent tree item.
   *
   * @return the parent
   */
  @NotNull
  public TreeItem<NetworkNode> getParent() {
    return parent;
  }

  /**
   * Gets index on children list. -1 for last position.
   *
   * @return the index
   */
  @Nullable
  public int getIndex() {
    return index;
  }
}
