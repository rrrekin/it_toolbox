package net.in.rrrekin.ittoolbox.utilities;

import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.enMessage;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import org.jetbrains.annotations.PropertyKey;

/**
 * Base exception for all exceptions that carry english and localized message based on application
 * resource bundle.
 *
 * @author michal.rudewicz @gmail.com
 */
public class LocalizedException extends Exception {
  private final String localizedMessage;

  @Override
  public String getLocalizedMessage() {
    return localizedMessage;
  }

  /**
   * Instantiates a new Localized exception.
   *
   * @param code the message code
   */
  public LocalizedException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code) {
    super(enMessage(code));
    localizedMessage = localMessage(code);
  }

  /**
   * Instantiates a new Localized exception.
   *
   * @param code the message code
   * @param args the message args {@link java.text.MessageFormat}
   */
  public LocalizedException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Object... args) {
    super(enMessage(code, args));
    localizedMessage = localMessage(code, args);
  }

  /**
   * Instantiates a new Localized exception.
   *
   * @param code the message code
   * @param cause the cause
   */
  public LocalizedException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause) {
    super(enMessage(code), cause);
    localizedMessage = localMessage(code);
  }

  /**
   * Instantiates a new Localized exception.
   *
   * @param code the message code
   * @param cause the cause
   * @param args the message args {@link java.text.MessageFormat}
   */
  public LocalizedException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause,
      final Object... args) {
    super(enMessage(code, args), cause);
    localizedMessage = localMessage(code, args);
  }
}
