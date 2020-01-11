package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationPersistenceService.ReadResult;
import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException;
import net.in.rrrekin.ittoolbox.gui.GuiInvokeService;
import net.in.rrrekin.ittoolbox.gui.MainWindowController;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Open configurations manager.
 *
 * @author michal.rudewicz @gmail.com
 */
@Singleton
public class OpenConfigurationsService {

  private static final String OPEN_ON_EXIT_FILES = "open-on-exit-files";
  private static final String RECENT_FILES = "recent-files";
  private static final String LAST_PATH = "last-path";

  @NonNls
  private static final @NotNull Logger log =
      LoggerFactory.getLogger(OpenConfigurationsService.class);

  /** List of recently used files not opened when application was closed. */
  private final @NotNull List<Path> recentFiles = new ArrayList<>();
  /** List of files that was opened when application was closed. */
  private final @NotNull List<Path> openFiles = new ArrayList<>();

  private @Nullable Path lastOpenLocation;

  private final Object recentFilesListMonitor = new Object();

  private final @NotNull CommonResources commonResources;
  private final @NotNull ConfigurationPersistenceService persistenceService;
  private final @NotNull UserPreferencesFactory userPreferencesFactory;
  private final @NotNull UserPreferences preferences;
  private final @NotNull AppPreferences appPreferences;
  private final @NotNull GuiInvokeService guiInvokeService;

  private final @NotNull Injector injector;

  /**
   * Instantiates a new Open configurations manager.
   *
   * @param userPreferencesFactory the user preferences factory
   * @param persistenceService the persistence service
   * @param commonResources the common resources
   * @param injector the injector
   * @param appPreferences the app preferences
   */
  @Inject
  public OpenConfigurationsService(
      final @NotNull UserPreferencesFactory userPreferencesFactory,
      final @NotNull ConfigurationPersistenceService persistenceService,
      final @NotNull CommonResources commonResources,
      final @NotNull Injector injector,
      final @NotNull AppPreferences appPreferences,
      final @NotNull GuiInvokeService guiInvokeService) {
    log.debug("Creating OpenConfigurationsManager");
    this.userPreferencesFactory =
        requireNonNull(userPreferencesFactory, "UserPreferencesFactory mut not be null");
    this.persistenceService =
        requireNonNull(persistenceService, "PersistenceService mut not be null");
    this.commonResources = requireNonNull(commonResources, "CommonResources mut not be null");
    this.injector = requireNonNull(injector, "Injector must be not null");
    this.appPreferences = requireNonNull(appPreferences, "AppPreferences must be not null");
    this.guiInvokeService = requireNonNull(guiInvokeService, "GuiInvokeService must be not null");
    preferences = userPreferencesFactory.create(this.getClass());
  }

  /** Initialize service. */
  public void init() {
    log.debug("Initializing OpenConfigurationsManager");

    final List<Path> lastFilesFromPrefs =
        listOfValidPaths(preferences.getList(OPEN_ON_EXIT_FILES, newArrayList(), String.class));
    openFiles.clear();
    openFiles.addAll(lastFilesFromPrefs);
    log.debug("List of last opened files: {}", openFiles);
    final List<Path> recentFilesFromPrefs =
        listOfValidPaths(preferences.getList(RECENT_FILES, newArrayList(), String.class));
    recentFiles.clear();
    recentFiles.addAll(recentFilesFromPrefs);
    recentFiles.removeAll(openFiles);
    log.debug("List of recently used files: {}", openFiles);
    lastOpenLocation = Path.of(preferences.get(LAST_PATH, SystemUtils.USER_HOME));
  }

  private static @NotNull List<Path> listOfValidPaths(final @Nullable Iterable<String> list) {
    if (list == null) {
      return Collections.emptyList();
    }
    final ImmutableList.Builder<Path> builder = ImmutableList.builder();
    for (final String pathAsString : list) {
      final Path path = Path.of(pathAsString);
      final File pathAsFile = path != null ? path.toFile() : null;
      if (pathAsFile != null && pathAsFile.isFile() && pathAsFile.canRead()) {
        builder.add(path);
      }
    }
    return builder.build();
  }

  /** Reopen last opened configs. */
  public void reopenLastOpenedConfigs() {
    final AtomicInteger loadCounter = new AtomicInteger(0);
    final ExecutorService executor = Executors.newCachedThreadPool();
    for (final Path path : getOpenFiles()) {
      executor.execute(
          () -> {
            try {
              final @NotNull ReadResult configData = persistenceService.load(path.toFile());
              if (configData.isClean()) {
                loadCounter.incrementAndGet();
                openNewWindow(configData.configuration);
              } else {
                final String errorToPrint =
                    configData.warnings.size() <= 10
                        ? join("\n", configData.warnings)
                        : join("\n", configData.warnings.subList(0, 9)) + "\n...";
                final boolean doOpen =
                    commonResources.yesNoDialog(
                        null,
                        localMessage("ERR_FILE_LOAD"),
                        localMessage("ERR_FILE_LOAD_CONTINUE", path),
                        errorToPrint);
                if (doOpen) {
                  loadCounter.incrementAndGet();
                  openNewWindow(configData.configuration);
                } else {
                  removeOpenFile(path);
                  addRecentFile(path);
                }
              }
            } catch (final MissingConfigurationException e) {
              removeOpenFile(path);
              commonResources.errorDialog(
                  null, localMessage("ERR_FILE_LOAD"), e.getLocalizedMessage());
            } catch (final InvalidConfigurationException e) {
              removeOpenFile(path);
              addRecentFile(path);
              commonResources.errorDialog(
                  null, localMessage("ERR_FILE_LOAD"), e.getLocalizedMessage());
            } catch (final InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          });
    }
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
    } catch (final InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
    if (loadCounter.get() == 0) {
      openNewWindow(null);
    }
  }

  /**
   * Load file configuration.
   *
   * @param stage the stage
   * @param file the file
   * @return the configuration or null if load fails
   */
  public @Nullable Configuration loadFile(final @Nullable Stage stage, final @NotNull File file) {
    requireNonNull(file, "File must not been null");
    final Path path = file.getAbsoluteFile().toPath().normalize();
    if (path != null) {
      if (openFiles.contains(path)) {
        log.debug("File '{}' already open.", path);
        commonResources.infoDialog(
            stage,
            localMessage("ERR_FILE_ALREADY_OPEN"),
            localMessage("ERR_FILE_ALREADY_OPEN_MSG", path.getFileName()));
        return null;
      }
      try {
        final @NotNull ReadResult configData = persistenceService.load(file);
        if (!configData.isClean()) {
          final String errorToPrint =
              configData.warnings.size() > 10
                  ? join("\n", configData.warnings)
                  : join("\n", configData.warnings.subList(0, 9)) + "\n...";
          final boolean ignoreConfig =
              !commonResources.yesNoDialog(
                  stage,
                  localMessage("ERR_FILE_LOAD"),
                  localMessage("ERR_FILE_LOAD_CONTINUE", path),
                  errorToPrint);
          if (ignoreConfig) {
            return null;
          }
        }
        addOpenFile(path);
        setLastOpenLocation(path.getParent());
        return configData.configuration;

      } catch (final InvalidConfigurationException | MissingConfigurationException e) {
        log.error("Failed to load file '{}'", file, e);
        commonResources.errorDialog(stage, localMessage("ERR_FILE_LOAD"), e.getLocalizedMessage());
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } else {
      log.debug("Cannot locate file {}.", file);
      commonResources.infoDialog(
          stage,
          localMessage("ERR_CANNOT_LOCATE_FILE"),
          localMessage("ERR_CANNOT_LOCATE_FILE_MSG", file));
      return null;
    }
    return null;
  }

  public void saveFile(final @NotNull Configuration configuration)
      throws FailedConfigurationSaveException {
    checkState(configuration.getFilePath() != null, "file not defined for save");
    persistenceService.save(configuration.getFilePath().toFile(), configuration);
  }

  public void saveFileAs(final @NotNull Configuration configuration, final @NotNull Path path)
      throws FailedConfigurationSaveException {
    persistenceService.save(path.toFile(), configuration);
    removeOpenFile(configuration.getFilePath());
    addRecentFile(configuration.getFilePath());
    addOpenFile(path);
    configuration.setFilePath(path);
  }

  /**
   * Open new window for given configuration.
   *
   * @param configuration the configuration
   */
  public void openNewWindow(final @Nullable Configuration configuration) {
    final Semaphore finished = new Semaphore(0);
    guiInvokeService.runInGui(
        () -> {
          try {
            final @NotNull FXMLLoader mainWindowFxmlLoader = new FXMLLoader();
            mainWindowFxmlLoader.setControllerFactory(injector::getInstance);
            final URL location = getClass().getResource("/fxml/main.fxml");
            mainWindowFxmlLoader.setLocation(location);
            mainWindowFxmlLoader.setResources(LocaleUtil.getMessages());

            final VBox content = mainWindowFxmlLoader.load();
            final MainWindowController controller = mainWindowFxmlLoader.getController();
            if (content != null && controller != null) {
              final Scene scene = new Scene(content);
              final Stage stage = new Stage();
              stage.setScene(scene);
              controller.setUpStage(stage);
              controller.setConfig(configuration);
              stage.show();
            } else {
              log.error(
                  "Cannot create main window. Content: {}, Controller: {}", content, controller);
              commonResources.fatalError(localMessage("ERR_FAILED_TO_CREATE_MAIN_WINDOW"));
            }
          } catch (final IOException e) {
            log.error("Cannot load main window definition", e);
            commonResources.fatalError(
                localMessage("ERR_FAILED_LOAD_MAIN_WINDOW_DEFINITION", e.getLocalizedMessage()));
          } finally {
            finished.release();
          }
        });
    try {
      finished.acquire();
    } catch (final InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Add open file.
   *
   * @param path the path
   */
  public void addOpenFile(final @Nullable Path path) {
    if (path == null) return;
    synchronized (recentFilesListMonitor) {
      openFiles.remove(path);
      openFiles.add(0, path);
      preferences.putList(
          OPEN_ON_EXIT_FILES, openFiles.stream().map(Path::toString).collect(Collectors.toList()));
    }
  }

  /**
   * Remove open file.
   *
   * @param path the path
   */
  public void removeOpenFile(final @Nullable Path path) {
    if (path == null) {
      return;
    }
    synchronized (recentFilesListMonitor) {
      openFiles.remove(path);
      if (!openFiles.isEmpty()) {
        preferences.putList(
            OPEN_ON_EXIT_FILES,
            openFiles.stream().map(Path::toString).collect(Collectors.toList()));
      }
    }
  }

  /**
   * Add recent file.
   *
   * @param path the path
   */
  public void addRecentFile(final @Nullable Path path) {
    if (path == null) {
      return;
    }
    synchronized (recentFilesListMonitor) {
      recentFiles.remove(path);
      recentFiles.add(0, path);
      final int maxLen = appPreferences.getMaxRecentFiles();
      while (recentFiles.size() > maxLen) {
        recentFiles.remove(maxLen);
      }
      preferences.putList(
          RECENT_FILES, recentFiles.stream().map(Path::toString).collect(Collectors.toList()));
    }
  }

  /**
   * Remove recent file.
   *
   * @param path the path
   */
  public void removeRecentFile(final @Nullable Path path) {
    if (path == null) {
      return;
    }
    synchronized (recentFilesListMonitor) {
      recentFiles.remove(path);
      preferences.putList(
          RECENT_FILES, recentFiles.stream().map(Path::toString).collect(Collectors.toList()));
    }
  }

  /**
   * Gets open files.
   *
   * @return the open files
   */
  public @NotNull ImmutableList<Path> getOpenFiles() {
    synchronized (recentFilesListMonitor) {
      return ImmutableList.copyOf(openFiles);
    }
  }

  /**
   * Gets recent files.
   *
   * @return the recent files
   */
  public @NotNull ImmutableList<Path> getRecentFiles() {
    synchronized (recentFilesListMonitor) {
      return ImmutableList.copyOf(recentFiles);
    }
  }

  public Path getLastOpenLocation() {
    return lastOpenLocation;
  }

  public void setLastOpenLocation(final Path lastOpenLocation) {
    this.lastOpenLocation = lastOpenLocation;
    preferences.put(LAST_PATH, lastOpenLocation.toString());
  }
}
