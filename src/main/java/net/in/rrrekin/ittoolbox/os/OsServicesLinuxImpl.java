package net.in.rrrekin.ittoolbox.os;

import lombok.NonNull;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * Linux specific {@link OsServices} implementation.
 *
 * @author michal.rudewicz@gmail.com
 */
public class OsServicesLinuxImpl extends OsServicesDefaultImpl {
  public OsServicesLinuxImpl(final @NonNull SystemWrapper system, final @NonNull ProgramLocationService locationService) {
    super(system, locationService);
  }
}
