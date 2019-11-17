package net.in.rrrekin.ittoolbox.gui;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 * Service that helps to run code in EDT.
 *
 * @author michal.rudewicz @gmail.com
 */
public class EdtInvokeService {

  /**
   * Execute given code in EDT. If current thread is EDT, it is executed synchronously. Otherwise it
   * is scheduled for invocation using {@link SwingUtilities#invokeLater(Runnable)}
   *
   * @param code the code
   */
  public void runInEdt(final Runnable code) {
    if (SwingUtilities.isEventDispatchThread()) {
      code.run();
    } else {
      SwingUtilities.invokeLater(code);
    }
  }

  /**
   * Execute given code in EDT. Current thread is EDT, it is executed synchronously. Otherwise it is
   * executed using {@link SwingUtilities#invokeAndWait(Runnable)}.
   *
   * @param code the code
   */
  public void runInEdtAndWait(final Runnable code) throws InvocationTargetException, InterruptedException {
    if (SwingUtilities.isEventDispatchThread()) {
      code.run();
    } else {
      SwingUtilities.invokeAndWait(code);
    }
  }
}
