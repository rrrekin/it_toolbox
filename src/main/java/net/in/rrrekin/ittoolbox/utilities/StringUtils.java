package net.in.rrrekin.ittoolbox.utilities;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to provide common string utilities.
 *
 * @author michal.rudewicz @gmail.com
 */
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
}
