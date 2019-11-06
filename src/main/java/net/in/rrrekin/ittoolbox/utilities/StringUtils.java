package net.in.rrrekin.ittoolbox.utilities;

import groovy.text.SimpleTemplateEngine;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to provide common string utilities.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public final class StringUtils {

  private StringUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Converts object to its string representation {@link String#valueOf(Object)} or empty string for
   * null values.
   *
   * @param object the object
   * @return the string representation
   */
  public static @NotNull String toStringOrEmpty(final @Nullable Object object) {
    return object == null ? "" : String.valueOf(object);
  }

  /**
   * Converts object to its string representation {@link String#valueOf(Object)} or default value
   * for null values.
   *
   * @param object the object
   * @param defaultValue the default value
   * @return the string representation
   */
  public static @NotNull String toStringOrDefault(
      final @Nullable Object object, final @NonNull String defaultValue) {
    return object == null ? defaultValue : String.valueOf(object);
  }

  /**
   * Apply templating to the string. Uses groovy GString syntax {@link SimpleTemplateEngine}.
   *
   * @param template the template
   * @param variables the variables
   * @return the string
   */
  public static String applyTemplate(
      final @NonNull String template, final @NonNull Map<String, Object> variables)
      throws TemplateException {
    log.trace("Evaluating template '{}' with variables {}", template, variables);
    final SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
    try {
      return templateEngine.createTemplate(template).make(variables).toString();
    } catch (final Exception e) {
      log.warn("Failed to evaluate template '{}' with variables {}", template, variables);
      throw new TemplateException(template, e);
    }
  }
}
