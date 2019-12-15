package net.in.rrrekin.ittoolbox.os;

import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.jetbrains.annotations.NotNull;

/**
 * Linux specific {@link OsServices} implementation.
 *
 * @author michal.rudewicz@gmail.com
 */
public class OsServicesLinuxImpl extends OsServicesDefaultImpl {
  public OsServicesLinuxImpl(final @NotNull SystemWrapper system, final @NotNull ProgramLocationService locationService) {
    super(system, locationService);
  }
}
