package net.in.rrrekin.ittoolbox.utilities;

import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle localization.
 *
 * @author michal.rudewicz @gmail.com
 */
public final class LocaleUtil {

  @NonNls private static final Logger log = LoggerFactory.getLogger(LocaleUtil.class);

  /** Property bundle with global messages. */
  public static final String MESSAGES_PROPERTY_BUNDLE = "Messages";

  private static @Nullable String localeCode = null;

  @NonNls
  private static final @NotNull ResourceBundle enMessages =
      requireNonNull(
          ResourceBundle.getBundle(MESSAGES_PROPERTY_BUNDLE, Locale.ENGLISH),
          "Failed to get english message bundle");

  @NonNls
  private static @NotNull ResourceBundle messages =
      requireNonNull(
          ResourceBundle.getBundle(MESSAGES_PROPERTY_BUNDLE), "Failed to get local message bundle");

  @NonNls
  private static final @NotNull Locale systemLocale =
      requireNonNull(Locale.getDefault(), "Failed to get system locale");

  private LocaleUtil() {
    // private constructor to prevent instantiation
  }

  /**
   * Sets locale for application. If null reset to default system locale.
   *
   * @param newLocale new locale for application. If null reset to default system locale.
   */
  public static void setLocale(final @Nullable Locale newLocale) {
    if (newLocale != null) {
      localeCode = newLocale.toLanguageTag();
      Locale.setDefault(newLocale);
    } else {
      localeCode = null;
      Locale.setDefault(systemLocale);
    }
    messages =
        requireNonNull(
            ResourceBundle.getBundle(MESSAGES_PROPERTY_BUNDLE),
            "Failed to get local message bundle");
  }

  /**
   * Returns localized message for given code.
   *
   * @param code the message code
   * @return the localized message for given code
   */
  public static @NotNull String localMessage(
      @PropertyKey(resourceBundle = MESSAGES_PROPERTY_BUNDLE) final @NotNull String code) {
    try {
      return messages.getString(code);
    } catch (final Exception e) {
      log.error("Missing resource bundle value for {}", code);
      return code;
    }
  }

  /**
   * Returns localized message for given code and arguments using {@link MessageFormat}.
   *
   * @param code the message code
   * @param args the message arguments
   * @return the formatted localized message for given code and arguments
   */
  public static @NotNull String localMessage(
      @PropertyKey(resourceBundle = MESSAGES_PROPERTY_BUNDLE) final @NotNull String code,
      final Object... args) {
    try {
      return new MessageFormat(messages.getString(code), Locale.getDefault()).format(args);
    } catch (final Exception e) {
      log.error("Missing resource bundle value for {}", code);
      return code;
    }
  }

  /**
   * En * Returns english message for given code. string.
   *
   * @param code the message code
   * @return the english message for given code
   */
  public static @NotNull String enMessage(
      @PropertyKey(resourceBundle = MESSAGES_PROPERTY_BUNDLE) final @NotNull String code) {
    try {
      return enMessages.getString(code);
    } catch (final Exception e) {
      log.error("Missing resource bundle value for {}", code);
      return code;
    }
  }

  /**
   * Returns english message for given code and arguments using {@link MessageFormat}.
   *
   * @param code the message code
   * @param args the message arguments
   * @return the formatted english message for given code and arguments
   */
  public static @NotNull String enMessage(
      @PropertyKey(resourceBundle = MESSAGES_PROPERTY_BUNDLE) final @NotNull String code,
      final Object... args) {
    try {
      return new MessageFormat(enMessages.getString(code), Locale.ENGLISH).format(args);
    } catch (final Exception e) {
      log.error("Missing resource bundle value for {}", code);
      return code;
    }
  }

  /**
   * Get supported array with languages codes.
   *
   * @return the string [ ]
   */
  public static String[] getSupportedLanguages() {
    return enMessages.getString("__IMPLEMENTED_LANGUAGES").split(",");
  }

  /**
   * Get single character value from property bundle (first non-blank character used). Returns null
   * if value not defined or defined as an empty string.
   *
   * @param code the property key
   * @return the character
   */
  public static @Nullable Character localCharacter(
      @PropertyKey(resourceBundle = MESSAGES_PROPERTY_BUNDLE) final @NotNull String code) {
    try {
      final String value = messages.getString(code);
      return value.isEmpty() ? null : value.charAt(0);
    } catch (final Exception e) {
      log.error("Missing resource bundle value for {}", code);
      return null;
    }
  }

  /** Gets language code of selected locale. */
  public static @Nullable String getLocaleCode() {
    return localeCode;
  }

  /** Resource bundle used to create english messages (e.g. log entries). */
  public static @NotNull ResourceBundle getEnMessages() {
    return enMessages;
  }

  /** Resource bundle used to create localized messages (e.g. error dialog boxes). */
  public static @NotNull ResourceBundle getMessages() {
    return messages;
  }
}
