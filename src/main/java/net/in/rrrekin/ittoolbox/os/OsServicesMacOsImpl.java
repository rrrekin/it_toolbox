package net.in.rrrekin.ittoolbox.os;

import lombok.NonNull;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * MacOS specific {@link OsServices} implementation.

 * @author michal.rudewicz@gmail.com
 */
public class OsServicesMacOsImpl extends OsServicesDefaultImpl {
  public OsServicesMacOsImpl(final @NonNull ProgramLocationService locationService) {
    super(locationService);
  }
}
