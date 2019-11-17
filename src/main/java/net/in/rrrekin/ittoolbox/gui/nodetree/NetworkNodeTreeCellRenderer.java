package net.in.rrrekin.ittoolbox.gui.nodetree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;

/**
 * Rendere of network nodes in main window tree.
 *
 * @author michal.rudewicz@gmail.com
 */
public class NetworkNodeTreeCellRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(
      final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    if (value instanceof NetworkNode) {
      final NetworkNode node = (NetworkNode) value;
      setIcon(node.getIcon());
      setText(node.getName());
      setToolTipText(node.getDescription());
    }
    return this;
  }
}
