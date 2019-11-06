package net.in.rrrekin.ittoolbox.utilities;

import lombok.Getter;

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
  CANNOT_CREATE_LOGS_CONFIG(1002);

  @Getter private final int value;

  ErrorCode(final int value) {
    this.value = value;
  }
}
