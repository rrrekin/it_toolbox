package net.in.rrrekin.ittoolbox.events;

import lombok.Value;

/**
 * Event emitted when configuration cannot be properly read.
 *
 * @author michal.rudewicz@gmail.com
 */
@Value
public class ConfigurationErrorEvent {
  private final Code code;
  private final String message;

  public enum Code {
    SERVER_LIST_UNREADABLE,
    CANNOT_CREATE_NETWORK_NODE,
    INVALID_OBJECT_ON_DTO_LIST,
    INVALID_MODULE_OPTIONS,
    INVALID_MODULE_LIST,
    INVALID_SERVICE_CONFIGURATION,
    INVALID_SERVICES_SECTION;
  }
}
