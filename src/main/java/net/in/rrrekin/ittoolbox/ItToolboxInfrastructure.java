package net.in.rrrekin.ittoolbox;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.ItToolboxApplication.APP_DIRECTORY;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.io.File;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.os.OsServices;
import net.in.rrrekin.ittoolbox.os.OsServicesFactory;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Configuration if DI by Guice.
 *
 * @author michal.rudewicz @gmail.com
 */
public class ItToolboxInfrastructure extends AbstractModule {

  private static final Logger log =
      org.slf4j.LoggerFactory.getLogger(ItToolboxInfrastructure.class);
  private final @NotNull File appDirectory;

  public ItToolboxInfrastructure(final @NotNull File appDirectory) {
    this.appDirectory = requireNonNull(appDirectory, "AppDirectory must not be null");
  }

  private static final String MAIN_EVENT_BUS_NAME = "MainEventBus";

  @Override
  protected void configure() {
    log.debug("Configuring DI bindings");
    final EventBus eventBus = new EventBus(MAIN_EVENT_BUS_NAME);
    bind(EventBus.class).toInstance(eventBus);
    bind(File.class).annotatedWith(Names.named(APP_DIRECTORY)).toInstance(appDirectory);
    bind(ProgramLocationService.class)
        .toInstance(new ProgramLocationService(System.getenv("PATH")));
    requestStaticInjection(IconDescriptor.class);
  }

  /**
   * Provides {@link OsServices instance}.
   *
   * @param locationService the location service
   * @return the os services
   */
  @Provides
  @Singleton
  @Inject
  OsServices getOsServices(
      final @NotNull ProgramLocationService locationService, final @NotNull SystemWrapper system) {
    return new OsServicesFactory(locationService, system).create();
  }
}
