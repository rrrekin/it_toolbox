package net.in.rrrekin.ittoolbox.configuration.exceptions;

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import net.in.rrrekin.ittoolbox.utilities.exceptions.LocalizedException;
import org.jetbrains.annotations.PropertyKey;

/**
 * Exception thrown when configuration file cannot be read.
 *
 * @author michal.rudewicz @gmail.com
 */
public class InvalidConfigurationException extends LocalizedException {

  /**
   * Instantiates a new InvalidConfigurationException.
   *
   * @param code the message code
   */
  public InvalidConfigurationException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code) {
    super(code);
  }

  /**
   * Instantiates a new InvalidConfigurationException.
   *
   * @param code the message code
   * @param args arguments for message {@link java.text.MessageFormat}
   */
  public InvalidConfigurationException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Object... args) {
    super(code, args);
  }

  /**
   * Instantiates a new InvalidConfigurationException.
   *
   * @param code the message code
   * @param cause the cause
   * @param args arguments for message {@link java.text.MessageFormat}
   */
  public InvalidConfigurationException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause,
      final Object... args) {
    super(code, cause, args);
  }
}
