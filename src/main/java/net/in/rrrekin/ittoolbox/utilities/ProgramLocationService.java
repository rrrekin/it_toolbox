package net.in.rrrekin.ittoolbox.utilities;

import java.io.File;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Program location service - enables checking if given program is present on this computer.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ProgramLocationService {

  /**
   * Is program available on this computer.
   *
   * @param name the name
   * @return the file
   */
  public @Nullable File isProgramAvailable(final @NonNull String name) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
