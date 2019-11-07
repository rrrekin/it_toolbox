package net.in.rrrekin.ittoolbox.infrastructure;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for lost events.
 *
 * @author michal.rudewicz@gmail.com
 */
@Slf4j
public class UnhandledMessagesLogger {

  private final @NonNull EventBus eventBus;

  @Inject
  public UnhandledMessagesLogger(final @NonNull EventBus eventBus) {
    log.info("Creating UnhandledMessagesLogger");
    this.eventBus = eventBus;
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
