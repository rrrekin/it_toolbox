package net.in.rrrekin.ittoolbox.events;

import lombok.NonNull;
import lombok.Value;
import net.in.rrrekin.ittoolbox.utilities.ErrorCode;

/**
 * Event used to notify main window to display blocking message window with possibility to continue
 * or terminate application.
 *
 * @author michal.rudewicz@gmail.com
 */
@Value
public class BlockingApplicationErrorEvent {
  /** Error code to use if user select to terminate application. */
  private final @NonNull ErrorCode errorCode;
  private final @NonNull String title;
  private final @NonNull String message;
  private final boolean fatal;
}
