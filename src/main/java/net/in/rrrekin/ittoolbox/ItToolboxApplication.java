package net.in.rrrekin.ittoolbox;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.LINUX;
import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.MAC;
import static net.in.rrrekin.ittoolbox.os.OsServicesFactory.WINDOWS;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.application.LauncherImpl;
import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.in.rrrekin.ittoolbox.configuration.OpenConfigurationsService;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.infrastructure.LogConfigurator;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.infrastructure.UnhandledMessagesLogger;
import net.in.rrrekin.ittoolbox.os.OsServices;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ItToolboxApplication extends Application {

  /** Application name. */
  @NonNls public static final String APPLICATION_NAME = "IT Toolbox";

  /** Application ID. */
  @NonNls public static final String APPLICATION_ID = "it_toolbox";

  /** Application vendor name. */
  @NonNls public static final String VENDOR_NAME = "rrrekin";

  /** File instance name for application directory constant. */
  @NonNls public static final String APP_DIRECTORY = "appDirectory";

  private static final String APPDATA_ENV_VAR = "APPDATA";

  @NonNls private static Logger log = null;
  private EventBus eventBus;
  private CommonResources commonResources;
  private OpenConfigurationsService manager;

  private static Injector injector;
  private static File appDirectory;

  /**
   * Main application entry point.
   *
   * @param args the args
   */
  public static void main(final String[] args) {
    // Configure logging before anything else
    appDirectory = calculateAppDirectory(new SystemWrapper());
    LogConfigurator.prepareLoggingConfiguration(appDirectory);
    log = LoggerFactory.getLogger(ItToolboxApplication.class);
    injector = Guice.createInjector(new ItToolboxInfrastructure(appDirectory));

    // Start application
    log.info("Starting {} Application", APPLICATION_NAME);

    LauncherImpl.launchApplication(ItToolboxApplication.class, ItToolboxPreloader.class, args);
  }

  /** Initialize application. */
  @Override
  public void init() {
    checkState(log != null, "Log must not be null.");

    commonResources =
        requireNonNull(
            injector.getInstance(CommonResources.class), "Failed to get CommonResources singleton");

    try {
      log.info("Initializing {} Application", APPLICATION_NAME);
      super.init();
      checkState(injector != null, "Injector must not be null.");
      eventBus =
          requireNonNull(injector.getInstance(EventBus.class), "Failed to get eventBus singleton");
      manager =
          requireNonNull(
              injector.getInstance(OpenConfigurationsService.class),
              "Failed to get OpenConfigurationsManager singleton");

      requireNonNull(
              injector.getInstance(UnhandledMessagesLogger.class),
              "Failed to get UnhandledMessagesLogger singleton")
          .init();
      manager.init();

    } catch (final Exception e) {
      log.error("Unexpected application initialization error.", e);
      commonResources.fatalException(e, null, localMessage("APP_INITIALIZATION_FAILURE"));
    }
  }

  @Override
  public void start(final Stage splashStage) {
    checkState(commonResources != null, "CommonResources must not be null.");
    checkState(log != null, "Log must not be null.");
    try {
      checkState(manager != null, "OpenConfigurationsManager must not be null.");

      splashStage.setTitle(APPLICATION_NAME);
      splashStage.initStyle(StageStyle.UNDECORATED);
      requireNonNull(commonResources, "CommonResources not initialized")
          .setUpStageIcons(splashStage);

      final Image image =
          new Image(
              requireNonNull(
                  getClass().getResourceAsStream("/images/splash.jpg"),
                  "Failed to get splash screen image"));
      final Scene scene = new Scene(new HBox(new ImageView(image)), 640, 480);
      splashStage.setScene(scene);
      splashStage.setAlwaysOnTop(true);
      splashStage.show();
      new Thread(
              () -> {
                manager.reopenLastOpenedConfigs();
                Platform.runLater(splashStage::hide);
              })
          .start();

    } catch (final Exception e) {
      log.error("Unexpected error while application running.", e);
      commonResources.fatalException(e, splashStage);
    }
  }

  @Override
  public void stop() throws Exception {
    checkState(log != null, "Log must not be null.");
    super.stop();
    try {
      log.debug("Terminating background tasks");
      commonPool().awaitQuiescence(10, TimeUnit.SECONDS);
    } catch (final Exception e) {
      log.error("Error while shutting down.", e);
    }
    log.info("Application stopped");
  }

  /** Determine application main directory for configuration, data and logs of application. */
  static File calculateAppDirectory(final @NotNull SystemWrapper system) {
    requireNonNull(system, "SystemWrapper must not be null");
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

  public static class ItToolboxPreloader extends Preloader {
    @Override
    public void start(Stage primaryStage) throws Exception {
      com.sun.glass.ui.Application.GetApplication().setName(APPLICATION_NAME);

    }
  }
}
