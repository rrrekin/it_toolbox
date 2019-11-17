package net.in.rrrekin.ittoolbox.gui.nodetree;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.gui.MainWindow;

/**
 * Listener that handles network node tree context menu.
 *
 * @author michal.rudewicz@gmail.com
 */
@Slf4j
public class NodeTreePopupListener extends MouseAdapter {

  final JPopupMenu popup;

  public NodeTreePopupListener(final @NonNull MainWindow mainWindow) {
    popup = new JPopupMenu();
    popup.add(new JMenuItem(mainWindow.getAddServerAction()));
    popup.add(new JMenuItem(mainWindow.getAddGenericAction()));
    popup.add(new JMenuItem(mainWindow.getAddGroupAction()));
    popup.addSeparator();
    popup.add(new JMenuItem(mainWindow.getEditNodeAction()));
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    maybeShowPopup(event);
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    maybeShowPopup(event);
  }

  private void maybeShowPopup(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      final Component component = event.getComponent();
      if (component instanceof JTree) {
        final JTree tree = (JTree) component;
        final TreePath pathForLocation = tree.getPathForLocation(event.getX(), event.getY());
        if (pathForLocation != null) {
          tree.setSelectionPath(pathForLocation);
          popup.show(component, event.getX(), event.getY());
        }
      }
    }
  }
}
