package net.in.rrrekin.ittoolbox.configuration.exceptions;

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import net.in.rrrekin.ittoolbox.utilities.LocalizedException;
import org.jetbrains.annotations.PropertyKey;

/**
 * Exception thrown when configuration file cannot be found.
 *
 * @author michal.rudewicz @gmail.com
 */
public class FailedConfigurationSaveException extends LocalizedException {
  /**
   * Instantiates a new FailedConfigurationSaveException.
   *
   * @param code the message code
   * @param cause the cause
   * @param args arguments for message {@link java.text.MessageFormat}
   */
  public FailedConfigurationSaveException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause,
      final Object... args) {
    super(code, cause, args);
  }
}
