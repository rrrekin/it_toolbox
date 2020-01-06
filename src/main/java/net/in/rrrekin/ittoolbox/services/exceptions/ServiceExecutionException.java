package net.in.rrrekin.ittoolbox.services.exceptions;

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import net.in.rrrekin.ittoolbox.utilities.exceptions.LocalizedException;
import org.jetbrains.annotations.PropertyKey;

/** @author michal.rudewicz@gmail.com */
public class ServiceExecutionException extends LocalizedException {

  public ServiceExecutionException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code) {
    super(code);
  }

  public ServiceExecutionException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Object... args) {
    super(code, args);
  }

  public ServiceExecutionException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause) {
    super(code, cause);
  }

  public ServiceExecutionException(
      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final String code,
      final Throwable cause,
      final Object... args) {
    super(code, cause, args);
  }
}
