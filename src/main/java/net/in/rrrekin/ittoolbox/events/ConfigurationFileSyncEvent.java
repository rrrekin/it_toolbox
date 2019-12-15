package net.in.rrrekin.ittoolbox.events;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Event sent when configuration file read or save attempt is finished.
 * @author michal.rudewicz@gmail.com
 */
public final class ConfigurationFileSyncEvent {
  private final @NotNull ConfigurationFileSyncEvent.Code code;
  private final @NotNull String message;

  public ConfigurationFileSyncEvent(
    @NotNull ConfigurationFileSyncEvent.Code code,
    @NotNull String message) {
    this.code = requireNonNull(code,"Code must not be null");
    this.message = requireNonNull(message, "Message must not be null");
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
    final ConfigurationFileSyncEvent that = (ConfigurationFileSyncEvent) o;
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
    OK, MISSING, FAILED, NEW, SAVED;
  }
}
