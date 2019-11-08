package net.in.rrrekin.ittoolbox.infrastructure;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javax.swing.JOptionPane;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handler of {@link net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent} events.
 *
 * @author michal.rudewicz@gmail.com
 */
@Slf4j
public class BlockingApplicationEventsHandler {

  private final @NonNull EventBus eventBus;

  @Inject
  public BlockingApplicationEventsHandler(final @NonNull EventBus eventBus) {
    log.info("Creating BlockingApplicationEventsHandler");
    this.eventBus = eventBus;
  }

  public void init() {
    log.info("Initializing UnhandledMessagesLogger");
    eventBus.register(this);
  }

  @Subscribe
  public void handlEvents(final @NotNull BlockingApplicationErrorEvent event) {

    // TODO: implement Main window blocking

    log.error("Blocking event: {}", event);
    if (event.isFatal()) {
      JOptionPane.showMessageDialog(
          null, event.getMessage(), event.getTitle(), JOptionPane.ERROR_MESSAGE);
      event.getErrorCode().exit();
    } else {
      final int response =
          JOptionPane.showConfirmDialog(
              null,
              event.getMessage(),
              event.getTitle(),
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.ERROR_MESSAGE);
      if (response != JOptionPane.OK_OPTION) {
        log.info("User selected to terminate application.");
        event.getErrorCode().exit();
      } else {
        log.info("User selected to continue operation");
      }
    }
  }
}
