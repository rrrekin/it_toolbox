package net.in.rrrekin.ittoolbox.os;

import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.jetbrains.annotations.NotNull;

/**
 * MacOS specific {@link OsServices} implementation.

 * @author michal.rudewicz@gmail.com
 */
public class OsServicesMacOsImpl extends OsServicesDefaultImpl {
  public OsServicesMacOsImpl(final @NotNull SystemWrapper system, final @NotNull ProgramLocationService locationService) {
    super(system, locationService);
  }
}
