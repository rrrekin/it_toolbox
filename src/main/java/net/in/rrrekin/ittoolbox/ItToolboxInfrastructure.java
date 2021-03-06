package net.in.rrrekin.ittoolbox;

import static net.in.rrrekin.ittoolbox.configuration.ConfigurationManager.APP_DIRECTORY;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import java.io.File;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeFactory;
import net.in.rrrekin.ittoolbox.gui.EdtInvokeService;
import net.in.rrrekin.ittoolbox.gui.MainWindow;
import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodesTreeModelFacade;
import net.in.rrrekin.ittoolbox.infrastructure.BlockingApplicationEventsHandler;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.infrastructure.UnhandledMessagesLogger;
import net.in.rrrekin.ittoolbox.os.OsServices;
import net.in.rrrekin.ittoolbox.os.OsServicesFactory;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;

/**
 * Configuration if DI by Guice.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class ItToolboxInfrastructure extends AbstractModule {

  private final @NonNull File appDirectory;

  public ItToolboxInfrastructure(final @NonNull File appDirectory) {
    this.appDirectory = appDirectory;
  }

  private static final String MAIN_EVENT_BUS_NAME = "MainEventBus";

  @Override
  protected void configure() {
    log.debug("Configuring DI bindings");
    final EventBus eventBus = new EventBus(MAIN_EVENT_BUS_NAME);
    bind(EventBus.class).toInstance(eventBus);
    bind(File.class).annotatedWith(Names.named(APP_DIRECTORY)).toInstance(appDirectory);
    bind(ConfigurationManager.class).asEagerSingleton();
    bind(NodeFactory.class).asEagerSingleton();
    bind(UnhandledMessagesLogger.class).asEagerSingleton();
    bind(BlockingApplicationEventsHandler.class).asEagerSingleton();
    bind(NetworkNodesTreeModelFacade.class).asEagerSingleton();
    bind(MainWindow.class).asEagerSingleton();
    bind(EdtInvokeService.class).asEagerSingleton();
  }

  /**
   * Provides {@link OsServices instance}.
   *
   * @param locationService the location service
   * @return the os services
   */
  @Provides
  @Inject
  OsServices getOsServices(final @NonNull ProgramLocationService locationService, final @NonNull SystemWrapper system) {
    return new OsServicesFactory(locationService, system).create();
  }
}
