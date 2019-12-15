package net.in.rrrekin.ittoolbox.utilities;

import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;

/**
 * Error codes returned by application on failures.
 *
 * @author michal.rudewicz@gmail.com
 */
@Deprecated
// TODO: remove
public enum ErrorCode {
  INITIALIZATION_ERROR(1),
  RUNTIME_ERROR(2),
  INTERRUPTED(3),
  CANNOT_CREATE_APP_DIRECTORY(1000),
  CANNOT_CREATE_LOGS_DIRECTORY(1001),
  CANNOT_CREATE_LOGS_CONFIG(1002),
  SAVE_ERROR(2000),
  LOAD_ERROR(2001);

  private static SystemWrapper system = new SystemWrapper();

  private final int status;

  ErrorCode(final int status) {
    this.status = status;
  }

  public static SystemWrapper getSystem() {
    return ErrorCode.system;
  }

  public static void setSystem(SystemWrapper system) {
    ErrorCode.system = system;
  }

  /** Terminates application with this error code. */
  public void exit() {
    system.exit(status);
  }

  public int getStatus() {
    return this.status;
  }
}
