package net.in.rrrekin.ittoolbox.utilities;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import groovy.text.SimpleTemplateEngine;
import java.util.HashMap;
import java.util.Map;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Utility class to provide common string utilities.
 *
 * @author michal.rudewicz @gmail.com
 */
public final class StringUtils {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(StringUtils.class);

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
      final @Nullable Object object, final @NotNull String defaultValue) {
    requireNonNull(defaultValue, "Default value must not be null");
    return object == null ? defaultValue : String.valueOf(object);
  }

  /**
   * Apply templating to the string. Uses groovy GString syntax {@link SimpleTemplateEngine}.
   *
   * @param template the template
   * @param variables the variables
   * @return the string
   */
  public static @NotNull String applyTemplate(
      final @NotNull String template, final @NotNull Map<String, Object> variables)
      throws TemplateException {
    requireNonNull(template, "Template must not be null");
    requireNonNull(variables, "Variables map must not be null");
    log.trace("Evaluating template '{}' with variables {}", template, variables);
    final SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
    try {
      return Strings.nullToEmpty(
          templateEngine.createTemplate(template).make(new HashMap(variables)).toString());
    } catch (final Exception e) {
      log.warn("Failed to evaluate template '{}' with variables {}", template, variables);
      throw new TemplateException(template, e);
    }
  }
}
