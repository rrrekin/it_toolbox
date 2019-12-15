//package net.in.rrrekin.ittoolbox.configuration;
//
//import java.util.TimerTask;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * Task responsible for periodic loading/saving configuration.
// *
// * @author michal.rudewicz@gmail.com
// */
//@Slf4j
//class ConfigSyncTask extends TimerTask {
//  private final @NonNull ConfigurationManager configurationManager;
//
//  /**
//   * Instantiates a new Config sync task.
//   *
//   * @param configurationManager the configuration manager
//   */
//  ConfigSyncTask(final @NonNull ConfigurationManager configurationManager) {
//    this.configurationManager = configurationManager;
//  }
//
//  @Override
//  public void run() {
//    try {
//      log.debug("Syncing configuration with file");
//      configurationManager.saveIfDirty();
//      configurationManager.loadIfChanged();
//    } catch (final Exception e) {
//      log.warn("Exception in ConfigurationManager task", e);
//    }
//  }
//}
