package net.in.rrrekin.ittoolbox.events;

import lombok.NonNull;
import lombok.Value;

/**
 * Event sent when configuration file read or save attempt is finished.
 * @author michal.rudewicz@gmail.com
 */
@Value
public class ConfigurationFileReadEvent {
  private final @NonNull ConfigurationFileReadEvent.Code code;
  private final @NonNull String message;

  public enum Code {
    OK, MISSING, FAILED;
  }
}
