package net.in.rrrekin.ittoolbox.gui.commands;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.primitives.Ints;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface of commands that are executed on MainWindow model.
 *
 * @author michal.rudewicz@gmail.com
 */
public abstract class OperationCommand {

  private final @NotNull IntegerProperty stateSerialNumber;
  private int revokeAllowedAt = Integer.MIN_VALUE;
  public static final int MAX_DESCRIPTION_LEN = 32;

  protected OperationCommand(final @NotNull IntegerProperty stateSerialNumber) {
    this.stateSerialNumber =
        requireNonNull(stateSerialNumber, "stateSerialNumber must not be null");
  }

  /**
   * Get readable command description to be used in Undo/Redo description.
   *
   * @return readable command description to be used in Undo/Redo description
   */
  public abstract String getDescription();

  /**
   * Perform operation.
   *
   * @return modified item if applicable, null otherwise
   */
  public @Nullable TreeItem<NetworkNode> execute() {
    checkState(
        revokeAllowedAt == Integer.MIN_VALUE,
        "Trying to execute already executed comand (executed before at %s, now is %s).",
        revokeAllowedAt,
        stateSerialNumber.get());
    revokeAllowedAt = stateSerialNumber.getValue() + 1;
    stateSerialNumber.set(revokeAllowedAt);
    return null;
  }

  /**
   * Perform operation that will restore state before the {@link #execute()} call. Should be called
   * only in the state created by the execute method.
   *
   * @return modified item if applicable, null otherwise
   */
  public void revoke() {
    checkState(
        stateSerialNumber.get() == revokeAllowedAt,
        "Undo operation executed on incorrect state (%s!=%s)",
        stateSerialNumber.get(),
        revokeAllowedAt);
    revokeAllowedAt = Integer.MIN_VALUE;
    stateSerialNumber.set(stateSerialNumber.getValue() - 1);
  }

  protected static @NotNull int[] getItemLocation(final @NotNull TreeItem<NetworkNode> item) {
    final Deque<Integer> result = new LinkedList<>();
    TreeItem<NetworkNode> current = item;
    for (TreeItem<NetworkNode> parent = current.getParent();
        parent != null;
        current = parent, parent = parent.getParent()) {
      result.addFirst(parent.getChildren().indexOf(current));
    }
    return Ints.toArray(result);
  }

  protected static @NotNull TreeItem<NetworkNode> locateItem(
      final @NotNull int[] path, final @NotNull TreeItem<NetworkNode> root) {
    TreeItem<NetworkNode> response = root;
    for (final int i : path) {
      response = response.getChildren().get(i);
    }
    return response;
  }

  protected static @NotNull TreeItem<NetworkNode> findRoot(
      final @NotNull TreeItem<NetworkNode> item) {
    TreeItem<NetworkNode> response = item;
    while (response.getParent() != null) {
      response = response.getParent();
    }
    return response;
  }

  protected static @NotNull String printablePath(final @NotNull int[] itemLocation) {
    return Arrays.stream(itemLocation).mapToObj(String::valueOf).collect(Collectors.joining("/"));
  }

}
