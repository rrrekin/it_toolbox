package net.in.rrrekin.ittoolbox.utilities;

import lombok.Getter;
import lombok.Setter;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;

/**
 * Error codes returned by application on failures.
 *
 * @author michal.rudewicz@gmail.com
 */
public enum ErrorCode {
  INITIALIZATION_ERROR(1),
  RUNTIME_ERROR(2),
  INTERRUPTED(3),
  CANNOT_CREATE_APP_DIRECTORY(1000),
  CANNOT_CREATE_LOGS_DIRECTORY(1001),
  CANNOT_CREATE_LOGS_CONFIG(1002),
  SAVE_ERROR(2000),
  LOAD_ERROR(2001);

  @Getter @Setter private static SystemWrapper system = new SystemWrapper();

  @Getter private final int status;

  ErrorCode(final int status) {
    this.status = status;
  }

  /** Terminates application with this error code. */
  public void exit() {
    system.exit(status);
  }
}
