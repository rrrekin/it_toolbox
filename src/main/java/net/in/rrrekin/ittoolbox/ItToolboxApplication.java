package net.in.rrrekin.ittoolbox;

import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import lombok.NonNull;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager;
import net.in.rrrekin.ittoolbox.configuration.LogConfigurator;
import net.in.rrrekin.ittoolbox.utilities.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ItToolboxApplication {

  /** Application name. */
  @NonNls public static final String APPLICATION_NAME = "IT Toolbox";

  /** Application ID. */
  @NonNls public static final String APPLICATION_ID = "it_toolbox";

  /** Application vendor name. */
  @NonNls public static final String VENDOR_NAME = "rrrekin";

  private static final String APPDATA_ENV_VAR = "APPDATA";
  private static final String UNEXPECTED_APP_ERROR = "UNEXPECTED_APP_ERROR";

  @NonNls private static Logger log = null;
  private final @NonNull File appDirectory;
  private ConfigurationManager configurationManager = null;

  /**
   * Instantiates a new IT Toolbox application.
   *
   * @param appDirectory the app directory
   */
  public ItToolboxApplication(final @NonNull File appDirectory) {
    this.appDirectory = appDirectory;
  }

  /**
   * Main application entry point.
   *
   * @param args the args
   */
  public static void main(final String[] args) {
    // Configure logging before anything else
    final File appDirectory = calculateAppDirectory();
    LogConfigurator.prepareLoggingConfiguration(appDirectory);
    log = LoggerFactory.getLogger(ItToolboxApplication.class);

    // Start application
    final ItToolboxApplication application = new ItToolboxApplication(appDirectory);
    application.init();
    application.run();
    application.shutdown();
  }

  /** Initialize application. */
  private void init() {
    try {

      log.info("Starting {} Application", APPLICATION_NAME);

      final Injector injector = Guice.createInjector(new ItToolboxInfrastructure(appDirectory));

      // Necessary initialization code
      configurationManager = injector.getInstance(ConfigurationManager.class);
      configurationManager.init();
    } catch (final Exception e) {
      if (log != null) {
        log.error("Unexpected application initialization error.", e);
      }
      JOptionPane.showMessageDialog(
          null,
          localMessage("INITIALIZATION_FAILURE", e.getLocalizedMessage()),
          localMessage(UNEXPECTED_APP_ERROR),
          JOptionPane.ERROR_MESSAGE);
      //noinspection CallToSystemExit
      System.exit(ErrorCode.INITIALIZATION_ERROR.getValue());
    }
  }

  /** Run application. */
  private void run() {
    try {
      Thread.sleep(10000);
    } catch (final InterruptedException e) {
      log.info("Application interrupted.", e);
      Thread.currentThread().interrupt();
      //noinspection CallToSystemExit
      System.exit(ErrorCode.INTERRUPTED.getValue());
    } catch (final Exception e) {
      log.error("Unexpected error while application running.", e);
      JOptionPane.showMessageDialog(
          null,
          localMessage("RUNTIME_FAILURE", e.getLocalizedMessage()),
          localMessage(UNEXPECTED_APP_ERROR),
          JOptionPane.ERROR_MESSAGE);
      //noinspection CallToSystemExit
      System.exit(ErrorCode.RUNTIME_ERROR.getValue());
    }
  }

  /** Shutdown application. */
  private void shutdown() {
    try {
      log.debug("Terminating scheduled jobs");
      configurationManager.shutdown();
      log.info("Finishing application");
    } catch (final Exception e) {
      log.info("Error while shutting down.", e);
    }
  }

  /** Determine application main directory for configuration, data and logs of application. */
  private static File calculateAppDirectory() {
    if (IS_OS_LINUX) {
      return Paths.get(USER_HOME, ".local", "share", VENDOR_NAME, APPLICATION_ID).toFile();
    } else if (IS_OS_WINDOWS) {
      final String appdir = System.getenv(APPDATA_ENV_VAR);
      if (StringUtils.isNotBlank(appdir)) {
        return Paths.get(appdir, VENDOR_NAME, APPLICATION_ID).toFile();
      } else {
        return Paths.get(USER_HOME, "AppData", VENDOR_NAME, APPLICATION_ID).toFile();
      }
    } else if (IS_OS_MAC) {
      return Paths.get(USER_HOME, "Library", "Application Support", VENDOR_NAME, APPLICATION_ID)
          .toFile();
    }
    return new File(USER_HOME, "." + APPLICATION_ID);
  }
}
