package net.in.rrrekin.ittoolbox.infrastructure;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import net.in.rrrekin.ittoolbox.gui.GuiInvokeService;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class responsible for log infrastructure setup.
 *
 * @author michal.rudewicz @gmail.com
 */
public class LogConfigurator {

  private static final String LOGS_SUBDIRECTORY = "logs";

  /**
   * Sets file logs location.
   *
   * @param appDir the logs
   */
  public static void prepareLoggingConfiguration(final @NotNull File appDir) {
    requireNonNull(appDir, "AppDir must not be null");
    appDir.mkdirs(); // NOSONAR
    appDir.setWritable(true); // NOSONAR
    if (!appDir.isDirectory() || !appDir.canWrite()) {
      new CommonResources(new GuiInvokeService(), null).fatalError(localMessage("APP_CANNOT_CREATE_APP_DIR", appDir));
    }

    final File logsDir = new File(appDir, LOGS_SUBDIRECTORY);
    logsDir.mkdirs(); // NOSONAR
    logsDir.setWritable(true); // NOSONAR
    if (!appDir.isDirectory() || !appDir.canWrite()) {
      new CommonResources(new GuiInvokeService(), null).fatalError(localMessage("APP_CANNOT_CREATE_LOGS_DIR", logsDir));
    }

    final File configFile = new File(appDir, "logback.xml");
    if (!(configFile.isFile() && configFile.canRead())) {
      createDefaultLogbackConfig(configFile);
    }
    if (!(configFile.isFile() && configFile.canRead())) {
      new CommonResources(new GuiInvokeService(), null).fatalError(localMessage("APP_CANNOT_CREATE_LOGGING_CONFIG", configFile));
    }

    System.setProperty("LOG_FILE_LOCATION", logsDir.getAbsolutePath());
    System.setProperty("logback.configurationFile", configFile.getAbsolutePath());
  }

  private static void createDefaultLogbackConfig(final @NotNull File file) {
    file.delete(); // NOSONAR
    try {
      final InputStream logbakcConfigStream =
          LogConfigurator.class.getClassLoader().getResourceAsStream("logback.xml");
      if (logbakcConfigStream != null) {
        Files.copy(logbakcConfigStream, file.toPath());
      }
    } catch (final IOException e) {
      new CommonResources(new GuiInvokeService(), null).fatalError(localMessage("APP_CANNOT_CREATE_LOGGING_CONFIG", e.getLocalizedMessage()));
    }
  }
}
