package net.in.rrrekin.ittoolbox.gui.nodetree;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;

/**
 * Editor for node names in tree.
 *
 * @author michal.rudewicz@gmail.com
 */
public class NetworkNodeTreeCellEditor extends DefaultCellEditor {

  public NetworkNodeTreeCellEditor() {
    super(new JTextField());
  }

  @Override
  public Component getTreeCellEditorComponent(
      final JTree tree,
      final Object value,
      final boolean isSelected,
      final boolean expanded,
      final boolean leaf,
      final int row) {
    final JTextField editor;
    if (value instanceof NetworkNode) {
      final NetworkNode node = (NetworkNode) value;
      editor =
          (JTextField)
              super.getTreeCellEditorComponent(
                  tree, node.getName(), isSelected, expanded, leaf, row);
      if (node.getName().length() < 30) {
        editor.setPreferredSize(new Dimension(150, node.getIcon().getIconHeight() - 2));
        editor.setMinimumSize(new Dimension(150, 12));
      } else {
        editor.setPreferredSize(null);
      }
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
      final JLabel icon = new JLabel();
      icon.setIcon(node.getIcon());
      panel.add(icon);
      panel.add(editor);
      panel.setOpaque(false);
      return panel;
    } else {
      editor =
          (JTextField)
              super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
      editor.setPreferredSize(null);
    }
    return editor;
  }
}
