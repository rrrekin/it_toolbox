package net.in.rrrekin.ittoolbox.os;

import com.google.inject.Inject;
import java.util.Locale;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * Factory of {@link OsServices} implementations based on OS name.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public final class OsServicesFactory {

  private static final String LINUX = "linux";
  private static final String WINDOWS = "windows";
  private static final String MAC = "mac";

  private final @NonNull ProgramLocationService locationService;

  @Inject
  public OsServicesFactory(final @NonNull ProgramLocationService locationService) {
    this.locationService = locationService;
  }

  /**
   * Create {@link OsServices} based on OS name (from System.getProperty("os.name").
   *
   * @param osName the os name
   * @return the os services
   */
  public OsServices create(final @NonNull String osName) {
    if (OsServices.OS_NAME.toLowerCase(Locale.ENGLISH).startsWith(LINUX)) {
      return new OsServicesLinuxImpl(locationService);
    } else if (OsServices.OS_NAME.toLowerCase(Locale.ENGLISH).startsWith(WINDOWS)) {
      return new OsServicesWindowsImpl(locationService);
    } else if (OsServices.OS_NAME.toLowerCase(Locale.ENGLISH).startsWith(MAC)) {
      return new OsServicesMacOsImpl(locationService);
    } else {
      return new OsServicesDefaultImpl(locationService);
    }
  }

  /**
   * Create {@link OsServices} for actual operating system returned by
   * System.getProperty("os.name").
   *
   * @return the os services
   */
  public OsServices create() {
    return create(OsServices.OS_NAME);
  }
}
