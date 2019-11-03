package net.in.rrrekin.ittoolbox.configuration.exceptions;

import java.io.File;
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import net.in.rrrekin.ittoolbox.utilities.LocalizedException;
import org.jetbrains.annotations.PropertyKey;

/**
 * Exception thrown when configuration file cannot be found.
 *
 * @author michal.rudewicz @gmail.com
 */
public class MissingConfigurationException extends LocalizedException {
  /**
   * Instantiates a new MissingConfigurationException.
   *
   * @param code the message code
   * @param cause the cause
   * @param file the file
   */
  public MissingConfigurationException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause,
      final File file) {
    super(code, cause, file);
  }
}
