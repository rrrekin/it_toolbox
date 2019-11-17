package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.FAILED;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.MISSING;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.NEW;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.OK;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.SAVED;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.ItToolboxApplication;
import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException;
import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent;
import net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent;
import net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent;
import net.in.rrrekin.ittoolbox.utilities.ErrorCode;
import org.jetbrains.annotations.NotNull;

/**
 * Class responsible for managing saving/loading configuration depending on changes and providing
 * access to configuration to other classes.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class ConfigurationManager {

  /** File instance name for application directory constant. */
  public static final String APP_DIRECTORY = "appDirectory";

  private static final String CONFIG_SYNC_TIMER_NAME = "ConfigSyncTimer";
  private static final String CONFIG_FILE_NAME =
      ItToolboxApplication.APPLICATION_ID + "-config.yml";
  private static final long CONFIG_DELAY_SYNC_MS = 5000;

  private final @NonNull EventBus eventBus;
  private final @NonNull ConfigurationPersistenceService persistenceService;
  private final @NotNull File configurationFile;
  private final @NonNull AtomicReference<Configuration> configuration =
      new AtomicReference<>(new Configuration(newArrayList(), newHashMap()));
  private final Timer configChangeTimer = new Timer(CONFIG_SYNC_TIMER_NAME, true);
  private final Object configFileAccessMonitor = new Object();
  private final List<ConfigurationErrorEvent> loadErrors = newArrayList();

  /** Returns timestamp of last loaded config file. */
  @Getter private long lastLoadedChangeTs = 0L;
  /** True if ConfigurationManager is initialized. () */
  @Getter private boolean active = false;
  /** True if configuration needs to be saved. */
  // TODO: Provide proper synchronization with saving / reading code
  @Getter @Setter private boolean dirty = false;

  /**
   * Instantiates a new Configuration manager.
   *
   * @param eventBus the event bus
   * @param persistenceService the persistence service
   * @param appDirectory the app directory
   */
  @Inject
  public ConfigurationManager(
      final @NonNull EventBus eventBus,
      final @NonNull ConfigurationPersistenceService persistenceService,
      @Named(APP_DIRECTORY) final @NonNull File appDirectory) {
    log.info("Creating ConfigurationManager");
    this.eventBus = eventBus;
    this.persistenceService = persistenceService;
    configurationFile = new File(appDirectory, CONFIG_FILE_NAME);
  }

  /** Init. */
  public void init() {
    log.info("Initializing ConfigurationManager");
    eventBus.register(this);
    load();
    configChangeTimer.schedule(
        new ConfigSyncTask(this), CONFIG_DELAY_SYNC_MS, CONFIG_DELAY_SYNC_MS);
    active = true;
  }

  /** Shutdown. */
  public void shutdown() {
    log.info("Stopping ConfigurationManager");
    saveIfDirty();
    active = false;
    configChangeTimer.cancel();
    configChangeTimer.purge();
    eventBus.unregister(this);
  }

  /**
   * Gets current configuration.
   *
   * @return the configuration
   */
  public @NotNull Configuration getConfig() {
    return configuration.get();
  }

  /**
   * Handle configuration read errors.
   *
   * @param event the event
   */
  @Subscribe
  public void handleConfigurationReadErrors(final @NotNull ConfigurationErrorEvent event) {
    log.debug("CFG_ERR: {}: {}", event.getCode(), event.getMessage());
    loadErrors.add(event);
  }

  /** Blocking initial configuration load. */
  void load() {
    log.trace("ConfigurationManager#load");
    if (configurationFile.exists()) {
      try {
        final Configuration newConfig;
        synchronized (configFileAccessMonitor) {
          loadErrors.clear();
          newConfig = persistenceService.load(configurationFile);
          if (loadErrors.isEmpty()) {
            configuration.set(newConfig);
            dirty = false;
            lastLoadedChangeTs = configurationFile.lastModified();
          }
        }
        if (loadErrors.isEmpty()) {
          eventBus.post(new ConfigurationFileSyncEvent(OK, localMessage("CFG_CONFIG_LOADED")));
        } else {
          log.info("Errors detected in configuration file {}", configurationFile);
          eventBus.post(
              new BlockingApplicationErrorEvent(
                  ErrorCode.LOAD_ERROR,
                  localMessage("CFG_LOAD_ERROR_TITLE"),
                  localMessage(
                      "CFG_LOAD_ERRORS_QUESTION",
                      configurationFile,
                      loadErrors.stream()
                          .map(ConfigurationErrorEvent::singleLineError)
                          .collect(Collectors.joining("\n"))),
                  false));
          configuration.set(newConfig);
          dirty = false;
          lastLoadedChangeTs = configurationFile.lastModified();
          loadErrors.clear();
          eventBus.post(
              new ConfigurationFileSyncEvent(OK, localMessage("CFG_CONFIG_LOAD_INCOMPLETE")));
        }
      } catch (final InvalidConfigurationException e) {
        log.warn("Failed to read existing configuration file '{}'.", configurationFile, e);
        lastLoadedChangeTs = configurationFile.lastModified();
        final String message =
            e.getCause() == null
                ? localMessage("CFG_LOAD_FAILURE_QUESTION", configurationFile)
                : localMessage(
                    "CFG_LOAD_FAILURE_QUESTION_WITH_ERROR",
                    configurationFile,
                    e.getCause().getLocalizedMessage());
        eventBus.post(
            new BlockingApplicationErrorEvent(
                ErrorCode.LOAD_ERROR, localMessage("CFG_LOAD_ERROR_TITLE"), message, false));
        dirty = false;
        eventBus.post(
            new ConfigurationFileSyncEvent(NEW, localMessage("CFG_CONFIG_NEW", configurationFile)));
      } catch (final MissingConfigurationException e) {
        log.info("Missing configuration file '{}'. Continue with defaults", configurationFile);
        dirty = false;
        eventBus.post(
            new ConfigurationFileSyncEvent(NEW, localMessage("CFG_CONFIG_NEW", configurationFile)));
      }
    }
  }

  /** Non blocking configuration reload. */
  void loadIfChanged() {
    log.trace("ConfigurationManager#loadIfChanged");
    if (active && configurationFile.exists()) {
      try {
        boolean loaded = false;
        synchronized (configFileAccessMonitor) {
          loadErrors.clear();
          final long lastChangeTs = configurationFile.lastModified();
          if (lastChangeTs > lastLoadedChangeTs) {
            final Configuration newConfig = persistenceService.load(configurationFile);
            if (loadErrors.isEmpty()) {
              configuration.set(newConfig);
              dirty = false;
              lastLoadedChangeTs = configurationFile.lastModified();
              loaded = true;
            }
          }
        }
        if (loaded) {
          eventBus.post(new ConfigurationFileSyncEvent(OK, localMessage("CFG_CONFIG_LOADED")));
        } else if (!loadErrors.isEmpty()) {
          log.info(
              "Errors detected in configuration file {}. File ignored while reloading.",
              configurationFile);
          eventBus.post(
              new ConfigurationFileSyncEvent(
                  FAILED,
                  localMessage(
                      "CFG_CONFIG_LOAD_FAILURE",
                      configurationFile,
                      loadErrors.stream()
                          .map(ConfigurationErrorEvent::singleLineError)
                          .collect(Collectors.joining("\n")))));
          loadErrors.clear();
        }
      } catch (final InvalidConfigurationException e) {
        log.warn("Failed to re-read configuration", e);
        lastLoadedChangeTs = configurationFile.lastModified();
        final String message =
            e.getCause() == null
                ? localMessage("CFG_CONFIG_LOAD_ERROR", e.getLocalizedMessage())
                : localMessage(
                    "CFG_CONFIG_LOAD_ERROR_WITH_CAUSE",
                    e.getLocalizedMessage(),
                    e.getCause().getLocalizedMessage());
        eventBus.post(new ConfigurationFileSyncEvent(FAILED, message));
      } catch (final MissingConfigurationException e) {
        log.warn("Failed to re-read configuration", e);
        eventBus.post(
            new ConfigurationFileSyncEvent(
                MISSING, localMessage("CFG_MISSING_CFG_FILE_ERROR", configurationFile)));
      }
    }
  }

  /** Save configuration if dirty. */
  void saveIfDirty() {
    log.trace("ConfigurationManager#saveIfDirty");
    if (active && (dirty || !configurationFile.exists())) {
      try {
        synchronized (configFileAccessMonitor) {
          if (dirty || !configurationFile.exists()) {
            persistenceService.save(configurationFile, configuration.get());
            lastLoadedChangeTs = configurationFile.lastModified();
            dirty = false;
          }
        }
        eventBus.post(new ConfigurationFileSyncEvent(SAVED, localMessage("CFG_CONFIG_SAVED")));
      } catch (final FailedConfigurationSaveException e) {
        eventBus.post(
            new BlockingApplicationErrorEvent(
                ErrorCode.SAVE_ERROR,
                localMessage("CFG_SAVE_ERROR_TITLE"),
                localMessage("CFG_SAVE_FAILURE", configurationFile, e.getLocalizedMessage()),
                false));
      }
    }
  }
}
