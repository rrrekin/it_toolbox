package net.in.rrrekin.ittoolbox.os;

import com.google.inject.Inject;
import java.util.Locale;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * Factory of {@link OsServices} implementations based on OS name.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public final class OsServicesFactory {

  /** The constant for determination of Linux OS. */
  public static final String LINUX = "linux";

  /** The constant for determination of Windows OS. */
  public static final String WINDOWS = "windows";

  /** The constant for determination od MacOS. */
  public static final String MAC = "mac";

  private final @NonNull ProgramLocationService locationService;
  private final @NonNull SystemWrapper system;

  @Inject
  public OsServicesFactory(final @NonNull ProgramLocationService locationService, final @NonNull SystemWrapper system) {
    this.locationService = locationService;
    this.system = system;
  }

  /**
   * Create {@link OsServices} based on OS name (from System.getProperty("os.name").
   *
   * @param osName the os name
   * @return the os services
   */
  public OsServices create(final @NonNull String osName) {
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
