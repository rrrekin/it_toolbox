package net.in.rrrekin.ittoolbox.gui.services;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.inject.Inject;
import net.in.rrrekin.ittoolbox.ItToolboxApplication;
import net.in.rrrekin.ittoolbox.gui.GuiInvokeService;
import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common GUI resources to be used across application.
 *
 * @author michal.rudewicz @gmail.com
 */
public class CommonResources {

  /** The constant ICON_SIZE. */
  public static final int ICON_SIZE = 64;

  /** The constant FONT_AWESOME. */
  public static final @NotNull GlyphFont FONT_AWESOME =
      Objects.requireNonNull(GlyphFontRegistry.font("FontAwesome"));

  @NonNls private static final @NotNull Logger log = LoggerFactory.getLogger(CommonResources.class);

  private final @NotNull GuiInvokeService guiInvokeService;


  @Inject
  public CommonResources(final @NotNull GuiInvokeService guiInvokeService) {
    log.debug("Creating CommonResources");
    this.guiInvokeService = requireNonNull(guiInvokeService, "GuiInvokeService must not be null");
  }

  /**
   * Gets error icon.
   *
   * @return the error icon
   */
  public @NotNull Glyph getErrorIcon() {
    return FONT_AWESOME
        .create(FontAwesome.Glyph.EXCLAMATION)
        .color(Color.RED)
        .useGradientEffect()
        .size(ICON_SIZE);
  }

  /**
   * Gets info icon.
   *
   * @return the error icon
   */
  public @NotNull Glyph getInfoIcon() {
    return FONT_AWESOME
        .create(FontAwesome.Glyph.INFO_CIRCLE)
        .color(Color.GREENYELLOW)
        .useGradientEffect()
        .size(ICON_SIZE);
  }

  /**
   * Gets question icon.
   *
   * @return the question icon
   */
  public @NotNull Glyph getQuestionIcon() {
    return FONT_AWESOME
        .create(FontAwesome.Glyph.QUESTION_CIRCLE)
        .color(Color.BLUE)
        .useGradientEffect()
        .size(ICON_SIZE);
  }

  /**
   * Fatal exception dialog and application termination.
   *
   * @param exception the exception
   */
  public void fatalException(final @NotNull Throwable exception) {
    fatalException(exception, null, localMessage("APP_RUNTIME_FAILURE"));
  }

  /**
   * Fatal exception dialog and application termination.
   *
   * @param exception the exception
   * @param owner the owner
   */
  public void fatalException(final @NotNull Throwable exception, final @Nullable Stage owner) {
    fatalException(exception, owner, localMessage("APP_RUNTIME_FAILURE"));
  }

  /**
   * Fatal exception dialog and application termination.
   *
   * @param exception the exception
   * @param owner the owner
   * @param header the header
   */
  public void fatalException(
      final @NotNull Throwable exception,
      final @Nullable Stage owner,
      final @NotNull String header) {
    guiInvokeService.runInGui(
        () -> {
          final ExceptionDialog exceptionDialog = new ExceptionDialog(exception);
          exceptionDialog.setTitle(localMessage("APP_UNEXPECTED_APP_ERROR"));
          exceptionDialog.setHeaderText(header);
          exceptionDialog.setGraphic(getErrorIcon());
          exceptionDialog.getDialogPane().setPrefWidth(800);

          if (owner != null) {
            exceptionDialog.initOwner(owner);
          }
          exceptionDialog.showAndWait();
          Platform.exit();
        });
  }

  /**
   * Fatal error dialog and application termination.
   *
   * @param header the header
   */
  public void fatalError(final @NotNull String header) {
    fatalError(null, header);
  }

  /**
   * Fatal error dialog and application termination.
   *
   * @param owner the owner
   */
  public void fatalError(final @Nullable Stage owner) {
    fatalError(owner, localMessage("APP_RUNTIME_FAILURE"));
  }

  /**
   * Fatal error dialog and application termination.
   *
   * @param owner the owner
   * @param header the header
   */
  public void fatalError(final @Nullable Stage owner, final @NotNull String header) {
    guiInvokeService.runInGui(
        () -> {
          final Alert dialog = new Alert(AlertType.ERROR, header);
          dialog.initModality(Modality.APPLICATION_MODAL);
          if (owner != null) {
            dialog.initOwner(owner);
          }
          dialog.setTitle(localMessage("APP_UNEXPECTED_APP_ERROR"));
          dialog.setHeaderText(null);
          dialog.setGraphic(getErrorIcon());
          dialog.showAndWait();
          Platform.exit();
        });
  }

  /**
   * Sets application window icons.
   *
   * @param stage the stage
   */
  public void setUpStageIcons(final @NotNull Stage stage) {
    final ObservableList<Image> icons =
        requireNonNull(stage, "No stage provided for icon setup").getIcons();
    icons.add(
        new Image(ItToolboxApplication.class.getResource("/icons/it_toolbox_64.png").toString()));
    icons.add(
        new Image(ItToolboxApplication.class.getResource("/icons/it_toolbox_48.png").toString()));
    icons.add(
        new Image(ItToolboxApplication.class.getResource("/icons/it_toolbox_32.png").toString()));
    icons.add(
        new Image(ItToolboxApplication.class.getResource("/icons/it_toolbox_24.png").toString()));
    icons.add(
        new Image(ItToolboxApplication.class.getResource("/icons/it_toolbox_16.png").toString()));
  }

  public void infoDialog(
      final @Nullable Stage owner, final @NotNull String title, final @NotNull String message) {
    guiInvokeService.runInGui(
        () -> {
          final Alert dialog = new Alert(AlertType.INFORMATION, message);
          dialog.initModality(owner == null ? Modality.APPLICATION_MODAL : Modality.WINDOW_MODAL);
          if (owner != null) {
            dialog.initOwner(owner);
          }
          dialog.setTitle(title);
          dialog.setHeaderText(null);
          dialog.setGraphic(getInfoIcon());
          dialog.showAndWait();
        });
  }

  public void errorDialog(
      final @Nullable Stage owner, final @NotNull String title, final @NotNull String message) {
    guiInvokeService.runInGui(
        () -> {
          final Alert dialog = new Alert(AlertType.ERROR, message);
          dialog.initModality(owner == null ? Modality.APPLICATION_MODAL : Modality.WINDOW_MODAL);
          if (owner != null) {
            dialog.initOwner(owner);
          }
          dialog.setTitle(title);
          dialog.setHeaderText(null);
          dialog.setGraphic(getErrorIcon());
          dialog.showAndWait();
        });
  }

  public boolean yesNoDialog(final @Nullable Stage owner, final @NotNull String title, final @NotNull String question, final @NotNull String context)
    throws InterruptedException {
    final boolean[] response = new boolean[1];
    final Semaphore semaphore = new Semaphore(0);
    guiInvokeService.runInGui(
        () -> {
          final Alert dialog =
              new Alert(AlertType.CONFIRMATION, context, ButtonType.NO, ButtonType.YES);
          dialog.initModality(owner == null ? Modality.APPLICATION_MODAL : Modality.WINDOW_MODAL);
          if (owner != null) {
            dialog.initOwner(owner);
          }
          dialog.setTitle(title);
          dialog.setHeaderText(question);
          dialog.setGraphic(getQuestionIcon());
          Optional<ButtonType> userResponse = dialog.showAndWait();
          response[0] = userResponse.orElse(ButtonType.NO).getButtonData() == ButtonData.YES;
          semaphore.release();
        });
    semaphore.acquire();
    return response[0];
  }
}
