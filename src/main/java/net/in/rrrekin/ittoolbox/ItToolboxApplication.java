package net.in.rrrekin.ittoolbox;

import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.LINUX;
import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.MAC;
import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.WINDOWS;
import static net.in.rrrekin.ittoolbox.utilities.ErrorCode.RUNTIME_ERROR;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import lombok.NonNull;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager;
import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent;
import net.in.rrrekin.ittoolbox.infrastructure.BlockingApplicationEventsHandler;
import net.in.rrrekin.ittoolbox.infrastructure.LogConfigurator;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.infrastructure.UnhandledMessagesLogger;
import net.in.rrrekin.ittoolbox.os.OsServices;
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
  private static final String UNEXPECTED_APP_ERROR = "APP_UNEXPECTED_APP_ERROR";

  @NonNls private static Logger log = null;
  private final @NonNull UnhandledMessagesLogger unhandledMessagesLogger;
  private final @NonNull EventBus eventBus;
  private final @NonNull BlockingApplicationEventsHandler blockingApplicationEventsHandler;
  private final @NonNull ConfigurationManager configurationManager;

  /**
   * Main application entry point.
   *
   * @param args the args
   */
  public static void main(final String[] args) {
    // Configure logging before anything else
    final File appDirectory = calculateAppDirectory(new SystemWrapper());
    LogConfigurator.prepareLoggingConfiguration(appDirectory);
    log = LoggerFactory.getLogger(ItToolboxApplication.class);

    // Start application
    log.info("Starting {} Application", APPLICATION_NAME);
    final Injector injector = Guice.createInjector(new ItToolboxInfrastructure(appDirectory));
    final ItToolboxApplication application = injector.getInstance(ItToolboxApplication.class);

    application.init();
    application.run();
    application.shutdown();
  }

  /**
   * Instantiates a new IT Toolbox application.
   *
   * @param unhandledMessagesLogger UnhandledMessagesLogger singleton instance
   * @param eventBus EventBus singleton instance
   * @param blockingApplicationEventsHandler BlockingApplicationEventsHandler singleton instance
   * @param configurationManager ConfigurationManager singleton instance
   */
  @Inject
  public ItToolboxApplication(
      final @NonNull UnhandledMessagesLogger unhandledMessagesLogger,
      final @NonNull EventBus eventBus,
      final @NonNull BlockingApplicationEventsHandler blockingApplicationEventsHandler,
      final @NonNull ConfigurationManager configurationManager) {
    this.unhandledMessagesLogger = unhandledMessagesLogger;
    this.eventBus = eventBus;
    this.blockingApplicationEventsHandler = blockingApplicationEventsHandler;
    this.configurationManager = configurationManager;
  }

  /** Initialize application. */
  void init() {
    try {
      log.info("Initializing {} Application", APPLICATION_NAME);
      unhandledMessagesLogger.init();
      blockingApplicationEventsHandler.init();
      configurationManager.init();
    } catch (final Exception e) {
      log.error("Unexpected application initialization error.", e);
      eventBus.post(
          new BlockingApplicationErrorEvent(
              ErrorCode.INITIALIZATION_ERROR,
              localMessage(UNEXPECTED_APP_ERROR),
              localMessage("APP_INITIALIZATION_FAILURE", e.getLocalizedMessage()),
              true));
    }
  }

  /** Run application. */
  void run() {
    try {
      Thread.sleep(10000);
    } catch (final InterruptedException e) {
      log.info("Application interrupted.", e);
      Thread.currentThread().interrupt();
      ErrorCode.INTERRUPTED.exit();
    } catch (final Exception e) {
      log.error("Unexpected error while application running.", e);
      eventBus.post(
          new BlockingApplicationErrorEvent(
              RUNTIME_ERROR,
              localMessage(UNEXPECTED_APP_ERROR),
              localMessage("APP_RUNTIME_FAILURE", e.getLocalizedMessage()),
              true));
    }
  }

  /** Shutdown application. */
  void shutdown() {
    try {
      log.debug("Terminating scheduled jobs");
      configurationManager.shutdown();
      log.info("Finishing application");
    } catch (final Exception e) {
      log.info("Error while shutting down.", e);
    }
  }

  /** Determine application main directory for configuration, data and logs of application. */
  static File calculateAppDirectory(final @NonNull SystemWrapper system) {
    final String osName = Strings.nullToEmpty(system.getProperty(OsServices.OS_NAME_ENV_VAR));
    if (osName.toLowerCase(Locale.ENGLISH).startsWith(LINUX)) {
      return Paths.get(
              StringUtils.defaultIfBlank(system.getProperty(OsServices.USER_HOME_ENV_VAR), "/tmp"),
              ".local",
              "share",
              VENDOR_NAME,
              APPLICATION_ID)
          .toFile();
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(WINDOWS)) {
      final String appdir = system.getenv(APPDATA_ENV_VAR);
      if (StringUtils.isNotBlank(appdir)) {
        return Paths.get(appdir, VENDOR_NAME, APPLICATION_ID).toFile();
      } else {
        return Paths.get(
                StringUtils.defaultIfBlank(
                    system.getProperty(OsServices.USER_HOME_ENV_VAR), "C:\\tmp"),
                "AppData",
                VENDOR_NAME,
                APPLICATION_ID)
            .toFile();
      }
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(MAC)) {
      return Paths.get(
              StringUtils.defaultIfBlank(system.getProperty(OsServices.USER_HOME_ENV_VAR), "/tmp"),
              "Library",
              "Application Support",
              VENDOR_NAME,
              APPLICATION_ID)
          .toFile();
    }
    return new File(
        StringUtils.defaultIfBlank(system.getProperty(OsServices.USER_HOME_ENV_VAR), "/tmp"),
        "." + APPLICATION_ID);
  }
}
