package net.in.rrrekin.ittoolbox.gui.commands;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import javafx.beans.property.IntegerProperty;
import org.jetbrains.annotations.NotNull;

/**
 * Interface of commands that are executed on MainWindow model.
 *
 * @author michal.rudewicz@gmail.com
 */
public abstract class OperationCommand {

  private final @NotNull IntegerProperty stateSerialNumber;
  private int revokeAllowedAt = Integer.MIN_VALUE;

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

  /** Perform operation. */
  public void execute() {
    checkState(
        stateSerialNumber.get() == Integer.MIN_VALUE,
        "Trying to execute already executed comand (executed before at %s, now is %s).",
        stateSerialNumber.get(),
        revokeAllowedAt);
    revokeAllowedAt = stateSerialNumber.getValue() + 1;
    stateSerialNumber.set(revokeAllowedAt);
  }

  /**
   * Perform operation that will restore state before the {@link #execute()} call. Should be called
   * only in the state created by the execute method.
   */
  public void revoke() {
    checkState(
        stateSerialNumber.get() == revokeAllowedAt,
        "Undo operation executed on incorrect state (%s!=%s)",
        stateSerialNumber.get(),
        revokeAllowedAt);
    revokeAllowedAt = Integer.MIN_VALUE;
  }
}
