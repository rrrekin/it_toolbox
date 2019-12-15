//package net.in.rrrekin.ittoolbox.infrastructure;
//
//import com.google.common.eventbus.EventBus;
//import com.google.common.eventbus.Subscribe;
//import com.google.inject.Inject;
//import java.util.Optional;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ButtonBar;
//import javafx.scene.control.ButtonType;
//import javafx.scene.paint.Color;
//import javafx.stage.Modality;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent;
//import net.in.rrrekin.ittoolbox.gui.GuiInvokeService;
//import org.controlsfx.glyphfont.FontAwesome;
//import org.controlsfx.glyphfont.Glyph;
//import org.jetbrains.annotations.NotNull;
//
///**
// * Handler of {@link net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent} events.
// *
// * @author michal.rudewicz@gmail.com
// */
//@Slf4j
//public class BlockingApplicationEventsHandler {
//
//  public static final int ICON_SIZE = 64;
//  private final @NonNull EventBus eventBus;
//  private final @NonNull GuiInvokeService guiInvokeService;
//
//  @Inject
//  public BlockingApplicationEventsHandler(
//      final @NonNull EventBus eventBus, final @NonNull GuiInvokeService guiInvokeService) {
//    log.info("Creating BlockingApplicationEventsHandler");
//    this.eventBus = eventBus;
//    this.guiInvokeService = guiInvokeService;
//  }
//
//  public void init() {
//    log.info("Initializing UnhandledMessagesLogger");
//    eventBus.register(this);
//  }
//
//  @Subscribe
//  public void handlEvents(final @NotNull BlockingApplicationErrorEvent event) {
//
//    guiInvokeService.runLater(
//        () -> {
//          log.error("Blocking event: {}", event);
//          if (event.isFatal()) {
//            final Alert alert = new Alert(Alert.AlertType.ERROR, event.getMessage());
//            alert.initModality(Modality.APPLICATION_MODAL);
//            alert.getDialogPane().setHeaderText(null);
//            alert.setTitle(event.getTitle());
//            alert.setGraphic(
//                new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION)
//                    .color(Color.RED)
//                    .useGradientEffect()
//                    .size(ICON_SIZE));
//            alert.showAndWait();
//            event.getErrorCode().exit();
//          } else {
//            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, event.getMessage());
//            alert.initModality(Modality.APPLICATION_MODAL);
//            alert.getDialogPane().setHeaderText(null);
//            alert.setTitle(event.getTitle());
//            alert.setGraphic(
//                new Glyph("FontAwesome", FontAwesome.Glyph.QUESTION)
//                    .color(Color.BLUE)
//                    .useGradientEffect()
//                    .size(ICON_SIZE));
//            final Optional<ButtonType> response = alert.showAndWait();
//            if (response.orElse(ButtonType.CANCEL).getButtonData()
//                != ButtonBar.ButtonData.OK_DONE) {
//              log.info("User selected to terminate application.");
//              event.getErrorCode().exit();
//            } else {
//              log.info("User selected to continue operation");
//            }
//          }
//        });
//  }
//}
