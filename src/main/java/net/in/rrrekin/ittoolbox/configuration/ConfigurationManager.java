package net.in.rrrekin.ittoolbox.configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent;
import org.jetbrains.annotations.NotNull;

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
  private static final long CONFIG_DELAY_SYNC_MS = 5000;

  private final @NonNull EventBus eventBus;
  private final @NonNull File appDirectory;
  private final Timer configChangeTimer = new Timer(CONFIG_SYNC_TIMER_NAME);
  private volatile boolean active = false;

  @Inject
  public ConfigurationManager(
      final @NonNull EventBus eventBus, @Named(APP_DIRECTORY) final @NonNull File appDirectory) {
    log.debug("Creating ConfigurationManager");
    this.eventBus = eventBus;
    this.appDirectory = appDirectory;
  }

  public void init() {
    log.debug("Initializing ConfigurationManager");
    eventBus.register(this);
    configChangeTimer.schedule(
        new ConfigurationManager.ConfigSyncTask(this), CONFIG_DELAY_SYNC_MS, CONFIG_DELAY_SYNC_MS);
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
    log.error("CFG_ERR: {}: {}", event.getCode(), event.getMessage());
  }

  public @NotNull File getConfigFile() {
    return new File("config.yml");
  }

  void loadIfChanged() {
    log.trace("ConfigurationManager#loadIfChanged");
  }

  void saveIfDirty() {
    log.trace("ConfigurationManager#saveIfDirty");
  }

  @Slf4j
  private static class ConfigSyncTask extends TimerTask {
    private final ConfigurationManager configurationManager;

    public ConfigSyncTask(final ConfigurationManager configurationManager) {
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
