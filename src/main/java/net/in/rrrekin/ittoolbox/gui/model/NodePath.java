package net.in.rrrekin.ittoolbox.gui.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public class NodePath {

  private final LinkedList<NetworkNode> path = new LinkedList<>();

  public NodePath(final @NotNull TreeItem<NetworkNode> node) {
    for (TreeItem<NetworkNode> n = node; n != null; n = n.getParent()) {
      path.addFirst(n.getValue());
    }
  }

  @Override
  public String toString() {
    return path.stream().map(NetworkNode::getName).collect(Collectors.joining("/"));
  }

  NetworkNode getTopParent() {
    return path.size() > 0 ? path.get(0) : null;
  }

  private void cutParents() {
    var node = path.getLast();
    path.clear();
    path.addFirst(node);
  }

  private void cutTopParent() {
    path.removeFirst();
  }

  int len() {
    return path.size();
  }

  NetworkNode getAt(final int i) {
    return path.get(i);
  }

  static void removeCommonParents(final @NotNull List<NodePath> paths) {
    if (!paths.isEmpty()) {
      if (paths.size() == 1) {
        paths.get(0).cutParents();
      } else {
        boolean change;
        int minPathLen;
        do {
          change = true;
          final var firstPath = paths.get(0);
          minPathLen = firstPath.len();
          final var root = firstPath.getTopParent();
          for (final NodePath path : paths) {
            if (root != path.getTopParent()) {
              change = false;
              break;
            }
          }
          if (change) {
            for (final NodePath path : paths) {
              path.cutTopParent();
            }
          }
        } while (change && minPathLen > 1);
      }
    }
  }
}
