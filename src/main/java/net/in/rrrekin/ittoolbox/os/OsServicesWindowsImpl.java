package net.in.rrrekin.ittoolbox.os;

import lombok.NonNull;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * Windows specific {@link OsServices} implementation.
 *
 * @author michal.rudewicz@gmail.com
 */
public class OsServicesWindowsImpl extends OsServicesDefaultImpl {
  public OsServicesWindowsImpl(final @NonNull ProgramLocationService locationService) {
    super(locationService);
  }
}
