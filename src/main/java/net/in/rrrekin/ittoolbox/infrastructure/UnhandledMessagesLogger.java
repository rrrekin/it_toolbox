package net.in.rrrekin.ittoolbox.infrastructure;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Handler for lost events.
 *
 * @author michal.rudewicz@gmail.com
 */
@Singleton
public class UnhandledMessagesLogger {

  @NonNls
  private static final Logger log =
      org.slf4j.LoggerFactory.getLogger(UnhandledMessagesLogger.class);
  private final @NotNull EventBus eventBus;

  @Inject
  public UnhandledMessagesLogger(final @NotNull EventBus eventBus) {
    log.info("Creating UnhandledMessagesLogger");
    this.eventBus = Objects.requireNonNull(eventBus, "EventBuss must not be null");
  }

  public void init() {
    log.info("Initializing UnhandledMessagesLogger");
    eventBus.register(this);
  }

  @Subscribe
  public void handleDeadEvents(final @NotNull DeadEvent event) {
    log.error("Unhandled message: {}", event.getEvent());
  }
}
