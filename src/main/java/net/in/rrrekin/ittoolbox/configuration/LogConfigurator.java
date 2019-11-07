package net.in.rrrekin.ittoolbox.configuration;

import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import javax.swing.JOptionPane;
import lombok.NonNull;
import net.in.rrrekin.ittoolbox.utilities.ErrorCode;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class responsible for log infractructure setup.
 *
 * @author michal.rudewicz @gmail.com
 */
public class LogConfigurator {

  private static final String LOGS_SUBDIRECTORY = "logs";
  private static final String STARTUP_ERROR_TITLE = "STARTUP_ERROR";

  /**
   * Sets file logs location.
   *
   * @param appDir the logs
   */
  public static void prepareLoggingConfiguration(final @NonNull File appDir) {

    appDir.mkdirs(); // NOSONAR
    appDir.setWritable(true); // NOSONAR
    if (!appDir.isDirectory() || !appDir.canWrite()) {
      JOptionPane.showMessageDialog(
          null,
          localMessage("CANNOT_CREATE_APP_DIR", appDir),
          localMessage(STARTUP_ERROR_TITLE),
          JOptionPane.ERROR_MESSAGE);
      ErrorCode.CANNOT_CREATE_APP_DIRECTORY.exit();
    }

    final File logsDir = new File(appDir, LOGS_SUBDIRECTORY);
    logsDir.mkdirs(); // NOSONAR
    logsDir.setWritable(true); // NOSONAR
    if (!appDir.isDirectory() || !appDir.canWrite()) {
      JOptionPane.showMessageDialog(
          null,
          localMessage("CANNOT_CREATE_LOGS_DIR", logsDir),
          localMessage(STARTUP_ERROR_TITLE),
          JOptionPane.ERROR_MESSAGE);
      ErrorCode.CANNOT_CREATE_LOGS_DIRECTORY.exit();
    }

    final File configFile = new File(appDir, "logback.xml");
    if (!(configFile.isFile() && configFile.canRead())) {
      createDefaultLogbackConfig(configFile);
    }
    if (!(configFile.isFile() && configFile.canRead())) {
      JOptionPane.showMessageDialog(
          null,
          localMessage("CANNOT_CREATE_LOGGING_CONFIG", configFile),
          localMessage(STARTUP_ERROR_TITLE),
          JOptionPane.ERROR_MESSAGE);
      ErrorCode.CANNOT_CREATE_LOGS_CONFIG.exit();
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
      JOptionPane.showMessageDialog(
          null,
          localMessage("CANNOT_CREATE_LOGGING_CONFIG", e.getLocalizedMessage()),
          localMessage(STARTUP_ERROR_TITLE),
          JOptionPane.ERROR_MESSAGE);
      ErrorCode.CANNOT_CREATE_LOGS_CONFIG.exit();
    }
  }
}
