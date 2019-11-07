package net.in.rrrekin.ittoolbox.events;

import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.Value;
import org.jetbrains.annotations.NonNls;

/**
 * Event emitted when configuration cannot be properly read.
 *
 * @author michal.rudewicz@gmail.com
 */
@Value
public class ConfigurationErrorEvent {
  private static final Pattern NEWLINES_PATTERN = Pattern.compile("([\r\n])+");
  private final @NonNull ConfigurationErrorEvent.Code code;
  private final @NonNull String message;

  @NonNls
  public String singleLineError() {
    return (NEWLINES_PATTERN.matcher(message).replaceAll("; ")).trim();
  }

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
