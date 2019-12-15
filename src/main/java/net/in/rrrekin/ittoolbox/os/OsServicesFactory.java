package net.in.rrrekin.ittoolbox.os;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import java.util.Locale;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Factory of {@link OsServices} implementations based on OS name.
 *
 * @author michal.rudewicz @gmail.com
 */
public final class OsServicesFactory {

  /** The constant for determination of Linux OS. */
  public static final String LINUX = "linux";

  /** The constant for determination of Windows OS. */
  public static final String WINDOWS = "windows";

  /** The constant for determination od MacOS. */
  public static final String MAC = "mac";
  private static final Logger log = org.slf4j.LoggerFactory.getLogger(OsServicesFactory.class);

  private final @NotNull ProgramLocationService locationService;
  private final @NotNull SystemWrapper system;

  @Inject
  public OsServicesFactory(final @NotNull ProgramLocationService locationService, final @NotNull SystemWrapper system) {
    this.locationService = requireNonNull(locationService, "LocationService must not be null");
    this.system = requireNonNull(system, "System must not be null");
  }

  /**
   * Create {@link OsServices} based on OS name (from System.getProperty("os.name").
   *
   * @param osName the os name
   * @return the os services
   */
  public OsServices create(final @NotNull String osName) {
    requireNonNull(osName, "OsName must not be null");
    if (osName.toLowerCase(Locale.ENGLISH).startsWith(LINUX)) {
      return new OsServicesLinuxImpl(system, locationService);
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(WINDOWS)) {
      return new OsServicesWindowsImpl(system, locationService);
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(MAC)) {
      return new OsServicesMacOsImpl(system, locationService);
    } else {
      return new OsServicesDefaultImpl(system, locationService);
    }
  }

  /**
   * Create {@link OsServices} for actual operating system returned by
   * System.getProperty("os.name").
   *
   * @return the os services
   */
  public OsServices create() {
    return create(system.getProperty(OsServices.OS_NAME_ENV_VAR));
  }
}
