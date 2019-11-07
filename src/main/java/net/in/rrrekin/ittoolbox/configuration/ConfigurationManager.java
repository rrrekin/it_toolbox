package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileReadEvent.Code.FAILED;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileReadEvent.Code.MISSING;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileReadEvent.Code.OK;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
import net.in.rrrekin.ittoolbox.events.ConfigurationFileReadEvent;
import net.in.rrrekin.ittoolbox.utilities.ErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class responsible for managing saving/loading configuration depending on changes and providing
 * access to configuration to other classes.
 *
 * @author michal.rudewicz@gmail.com
 */
@Slf4j
public class ConfigurationManager {

  /** File instance name for application directory constant. */
  public static final String APP_DIRECTORY = "appDirectory";

  private static final String CONFIG_SYNC_TIMER_NAME = "ConfigSyncTimer";
  private static final String CONFIG_FILE_NAME = ItToolboxApplication.APPLICATION_ID + ".yml";
  private static final long CONFIG_DELAY_SYNC_MS = 5000;

  private final @NonNull EventBus eventBus;
  private final @NotNull ConfigurationPersistenceService persistenceService;
  private final @NonNull File appDirectory;
  private @Nullable File configurationFile = null;
  private final @NonNull AtomicReference<Configuration> configuration =
      new AtomicReference<>(new Configuration(newArrayList(), newHashMap()));
  private long lastLoadedChangeTs = 0l;
  private final Timer configChangeTimer = new Timer(CONFIG_SYNC_TIMER_NAME);
  private volatile boolean active = false;
  private final Object configFileAccessMonitor = new Object();
  private final List<ConfigurationErrorEvent> loadErrors = newArrayList();

  @Getter @Setter private boolean dirty = false;

  @Inject
  public ConfigurationManager(
      final @NonNull EventBus eventBus,
      final @NotNull ConfigurationPersistenceService persistenceService,
      @Named(APP_DIRECTORY) final @NonNull File appDirectory) {
    log.debug("Creating ConfigurationManager");
    this.eventBus = eventBus;
    this.persistenceService = persistenceService;
    this.appDirectory = appDirectory;
  }

  public void init() {
    log.debug("Initializing ConfigurationManager");
    eventBus.register(this);
    configChangeTimer.schedule(
        new ConfigurationManager.ConfigSyncTask(this), CONFIG_DELAY_SYNC_MS, CONFIG_DELAY_SYNC_MS);
    configurationFile = new File(appDirectory, CONFIG_FILE_NAME);
    load();
    active = true;
  }

  public void shutdown() {
    log.debug("Stopping ConfigurationManager");
    active = false;
    configChangeTimer.cancel();
    configChangeTimer.purge();
  }

  @Subscribe
  public void handleConfigurationReadErrors(final @NotNull ConfigurationErrorEvent event) {
    log.debug("CFG_ERR: {}: {}", event.getCode(), event.getMessage());
    loadErrors.add(event);
  }

  public @NotNull File getConfigFile() {
    return new File("config.yml");
  }

  /** Blocking initial configuration load. */
  void load() {
    log.trace("ConfigurationManager#loadIfChanged");
    if (configurationFile != null && configurationFile.exists()) {
      try {
        synchronized (configFileAccessMonitor) {
          loadErrors.clear();
          final long lastChangeTs = configurationFile.lastModified();
          if (lastChangeTs > lastLoadedChangeTs) {
            final Configuration newConfig = persistenceService.load(configurationFile);
            if (loadErrors.isEmpty()) {
              configuration.set(newConfig);
              lastLoadedChangeTs = configurationFile.lastModified();
            }
          }
        }
        if (!loadErrors.isEmpty()) {
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
          loadErrors.clear();
        }
      } catch (final InvalidConfigurationException e) {
        log.warn("Failed to read existing configuration file '{}'.", configurationFile, e);
        eventBus.post(
            new BlockingApplicationErrorEvent(
                ErrorCode.LOAD_ERROR,
                localMessage("CFG_LOAD_ERROR_TITLE"),
                localMessage("CFG_LOAD_FAILURE_QUESTION", configurationFile),
                false));
      } catch (final MissingConfigurationException e) {
        log.info("Missing configuration file '{}'. Continue with defaults", configurationFile);
      }
    }
  }

  /** Non blocking configuration reload. */
  void loadIfChanged() {
    log.trace("ConfigurationManager#loadIfChanged");
    if (active && configurationFile != null && configurationFile.exists()) {
      try {
        synchronized (configFileAccessMonitor) {
          loadErrors.clear();
          final long lastChangeTs = configurationFile.lastModified();
          if (lastChangeTs > lastLoadedChangeTs) {
            final Configuration newConfig = persistenceService.load(configurationFile);
            if (loadErrors.isEmpty()) {
              configuration.set(newConfig);
              lastLoadedChangeTs = configurationFile.lastModified();
            }
          }
        }
        if (loadErrors.isEmpty()) {
          eventBus.post(new ConfigurationFileReadEvent(OK, localMessage("CFG_CONFIG_LOADED")));
        } else {
          eventBus.post(
              new ConfigurationFileReadEvent(
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
        eventBus.post(
            new ConfigurationFileReadEvent(
                FAILED, localMessage("CFG_CONFIG_LOAD_ERROR", e.getLocalizedMessage())));
      } catch (final MissingConfigurationException e) {
        log.warn("Failed to re-read configuration", e);
        eventBus.post(
            new ConfigurationFileReadEvent(
                MISSING, localMessage("CFG_MISSING_CFG_FILE_ERROR", configurationFile)));
      }
    }
  }

  void saveIfDirty() {
    log.trace("ConfigurationManager#saveIfDirty");
    if (active && dirty && configurationFile != null) {
      try {
        synchronized (configFileAccessMonitor) {
          if (dirty) {
            persistenceService.save(configurationFile, configuration.get());
            lastLoadedChangeTs = configurationFile.lastModified();
            dirty = false;
          }
        }
        eventBus.post(new ConfigurationFileReadEvent(OK, localMessage("CFG_CONFIG_SAVED")));
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

  @Slf4j
  private static class ConfigSyncTask extends TimerTask {
    private final ConfigurationManager configurationManager;

    ConfigSyncTask(final ConfigurationManager configurationManager) {
      this.configurationManager = configurationManager;
    }

    @Override
    public void run() {
      try {
        log.debug("Syncing configuration with file");
        configurationManager.saveIfDirty();
        configurationManager.loadIfChanged();
      } catch (final Exception e) {
        log.warn("Exception in {} task", CONFIG_SYNC_TIMER_NAME, e);
      }
    }
  }
}
