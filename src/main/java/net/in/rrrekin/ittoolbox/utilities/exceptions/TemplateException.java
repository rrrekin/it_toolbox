package net.in.rrrekin.ittoolbox.utilities.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown where string templating fails.
 *
 * @author michal.rudewicz@gmail.com
 */
public class TemplateException extends LocalizedException {

  public TemplateException(final @NotNull String template, final Throwable cause) {
    super("EX_TEMPLATE_EXCEPTION", cause, template, cause.getLocalizedMessage());
  }
}
