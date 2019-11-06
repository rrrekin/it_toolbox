package net.in.rrrekin.ittoolbox.utilities;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * The Program location service - enables checking if given program is present on this computer.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ProgramLocationService {

  @Getter private final List<Path> paths;

  /**
   * Instantiates a new Program location service.
   *
   * @param path the path as provided by System.getenv("PATH"). If null, the search paths list will
   *     be empty.
   */
  @Inject
  public ProgramLocationService(final @Nullable String path) {
    if (path == null) {
      paths = ImmutableList.of();
    } else {
      paths =
          Arrays.stream(StringUtils.split(path, File.pathSeparatorChar))
              .filter(StringUtils::isNotBlank)
              .map(Paths::get)
              .collect(ImmutableList.toImmutableList());
    }
  }

  /**
   * Is program available on this computer.
   *
   * @param name the name
   * @return the file
   */
  public @Nullable File getProgramBinary(final @NonNull String name) {

    for (final Path path : paths) {
      final Path binary = path.resolve(name);
      if (binary.toFile().isFile() && binary.toFile().canExecute()) {
        return binary.toFile();
      }
    }

    return null;
  }
}
