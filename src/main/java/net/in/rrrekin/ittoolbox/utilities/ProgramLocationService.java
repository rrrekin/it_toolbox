package net.in.rrrekin.ittoolbox.utilities;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Program location service - enables checking if given program is present on this computer.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ProgramLocationService {

  @NonNls
  private static final Logger log = LoggerFactory.getLogger(ProgramLocationService.class);

  private final List<Path> paths;

  /**
   * Instantiates a new Program location service.
   *
   * @param path the path as provided by System.getenv("PATH"). If null, the search paths list will
   *     be empty.
   */
  public ProgramLocationService(final @Nullable String path) {
    log.info("Creating ProgramLocationService with paths: {}", path);
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
  public @Nullable File getProgramBinary(final @NotNull String name) {
    requireNonNull(name, "Name must not be null");
    log.debug("Looking for {} command in {}", name, paths);
    for (final Path path : paths) {
      final Path binary = path.resolve(name);
      if (binary.toFile().isFile() && binary.toFile().canExecute()) {
        log.debug("Command {} found at {}", name, binary);
        return binary.toFile();
      }
    }
    log.debug("Command {} not found", name);
    return null;
  }

  public List<Path> getPaths() {
    return this.paths;
  }
}
