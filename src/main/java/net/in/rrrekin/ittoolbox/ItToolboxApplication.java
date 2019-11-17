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
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import lombok.NonNull;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager;
import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent;
import net.in.rrrekin.ittoolbox.gui.MainWindow;
import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodesTreeModelFacade;
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
  private final @NonNull NetworkNodesTreeModelFacade treeModelFacade;
  private final @NonNull ConfigurationManager configurationManager;
  private final @NonNull MainWindow mainWindow;

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
    IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
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
   * @param treeModelFacade NetworkNodesTreeModelFacade singleton instance
   * @param configurationManager ConfigurationManager singleton instance
   * @param mainWindow Application main window
   */
  @Inject
  public ItToolboxApplication(
      final @NonNull UnhandledMessagesLogger unhandledMessagesLogger,
      final @NonNull EventBus eventBus,
      final @NonNull BlockingApplicationEventsHandler blockingApplicationEventsHandler,
      final @NonNull NetworkNodesTreeModelFacade treeModelFacade,
      final @NonNull ConfigurationManager configurationManager,
      final @NonNull MainWindow mainWindow) {
    this.unhandledMessagesLogger = unhandledMessagesLogger;
    this.eventBus = eventBus;
    this.blockingApplicationEventsHandler = blockingApplicationEventsHandler;
    this.treeModelFacade = treeModelFacade;
    this.configurationManager = configurationManager;
    this.mainWindow = mainWindow;
  }

  /** Initialize application. */
  void init() {
    try {
      log.info("Initializing {} Application", APPLICATION_NAME);
      unhandledMessagesLogger.init();
      blockingApplicationEventsHandler.init();
      treeModelFacade
          .init(); // Should be initialized before configuration manager to get events from its
                   // initialization
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
      mainWindow.start();
      //      Thread.sleep(10000);
      //    } catch (final InterruptedException e) {
      //      log.info("Application interrupted.", e);
      //      Thread.currentThread().interrupt();
      //      ErrorCode.INTERRUPTED.exit();
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
      final String home = system.getProperty(OsServices.USER_HOME_ENV_VAR);
      return StringUtils.isBlank(home)
          ? new File(APPLICATION_ID)
          : Paths.get(home, ".local", "share", VENDOR_NAME, APPLICATION_ID).toFile();
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(WINDOWS)) {
      final String appdir = system.getenv(APPDATA_ENV_VAR);
      if (StringUtils.isNotBlank(appdir)) {
        return Paths.get(appdir, VENDOR_NAME, APPLICATION_ID).toFile();
      } else {
        final String home = system.getProperty(OsServices.USER_HOME_ENV_VAR);
        return StringUtils.isBlank(home)
            ? new File(APPLICATION_ID)
            : Paths.get(home, "AppData", VENDOR_NAME, APPLICATION_ID).toFile();
      }
    } else if (osName.toLowerCase(Locale.ENGLISH).startsWith(MAC)) {
      final String home = system.getProperty(OsServices.USER_HOME_ENV_VAR);
      return StringUtils.isBlank(home)
          ? new File(APPLICATION_ID)
          : Paths.get(home, "Library", "Application Support", VENDOR_NAME, APPLICATION_ID).toFile();
    }
    final String home = system.getProperty(OsServices.USER_HOME_ENV_VAR);
    return StringUtils.isBlank(home)
        ? new File(APPLICATION_ID)
        : new File(home, "." + APPLICATION_ID);
  }
}
