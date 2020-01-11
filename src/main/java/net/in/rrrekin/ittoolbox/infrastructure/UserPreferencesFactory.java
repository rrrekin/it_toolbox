package net.in.rrrekin.ittoolbox.infrastructure;

import com.google.inject.Singleton;
import java.nio.file.Path;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class of {@link UserPreferences} objects.
 *
 * @author michal.rudewicz @gmail.com
 */
@Singleton
public class UserPreferencesFactory {

  @NonNls
  private static final @NotNull Logger log = LoggerFactory.getLogger(UserPreferencesFactory.class);

  /** Instantiates a new User preferences factory. */
  public UserPreferencesFactory() {
    log.debug("Creating UserPreferencesFactory instance.");
  }

  /**
   * Creates UserPreferences instance for a class instance connected with a file.
   *
   * @param clazz the class
   * @param file the file
   * @return the user preferences instance
   */
  public @NotNull UserPreferences create(final @NotNull Class<?> clazz, final @NotNull Path file) {
    log.debug("Creating UserPreferences for {} {}", clazz, file);
    return new UserPreferences(clazz, file);
  }

  /**
   * Creates UserPreferences instance for class common for whole application.
   *
   * @param clazz the class
   * @return the user preferences instance
   */
  public @NotNull UserPreferences create(final @NotNull Class<?> clazz) {
    log.debug("Creating UserPreferences for {}", clazz);
    return new UserPreferences(clazz);
  }
}
