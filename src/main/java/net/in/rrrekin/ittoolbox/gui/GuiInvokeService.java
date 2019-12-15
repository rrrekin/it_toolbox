package net.in.rrrekin.ittoolbox.gui;

import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * Service that helps to run code in EDT.
 *
 * @author michal.rudewicz @gmail.com
 */
public class GuiInvokeService {

  /**
   * Execute given code in application thread. If current thread is application thread, it is
   * executed synchronously. Otherwise it is scheduled for invocation using {@link
   * Platform#runLater(Runnable)}
   *
   * @param code the code
   */
  public void runInGui(final @NotNull Runnable code) {
    if (Platform.isFxApplicationThread()) {
      code.run();
    } else {
      Platform.runLater(code);
    }
  }

  /**
   * Execute given code always in application thread. {@link Platform#runLater(Runnable)}
   *
   * @param code the code
   */
  public void runLater(final @NotNull Runnable code) {
    Platform.runLater(code);
  }
}
