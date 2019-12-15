package net.in.rrrekin.ittoolbox.events;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Event emitted when configuration cannot be properly read.
 *
 * @author michal.rudewicz@gmail.com
 */
public final class ConfigurationErrorEvent {
  private static final Pattern NEWLINES_PATTERN = Pattern.compile("([\r\n])+");
  private final @NotNull ConfigurationErrorEvent.Code code;
  private final @NotNull String message;

  public ConfigurationErrorEvent(
    @NotNull ConfigurationErrorEvent.Code code,
    @NotNull String message) {
    this.code = requireNonNull(code, "Code must not be null");
    this.message = requireNonNull(message,"Message must not be null");
  }

  @NonNls
  public @NotNull String singleLineError() {
    return (NEWLINES_PATTERN.matcher(message).replaceAll("; ")).trim();
  }

  public @NotNull Code getCode() {
    return this.code;
  }

  public @NotNull String getMessage() {
    return this.message;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ConfigurationErrorEvent that = (ConfigurationErrorEvent) o;
    return code == that.code &&
      message.equals(that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("code", code)
      .add("message", message)
      .toString();
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
