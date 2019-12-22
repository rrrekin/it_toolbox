package net.in.rrrekin.ittoolbox.gui.model;

import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

/**
 * NodeTree selection representation for copy/paste.
 *
 * @author michal.rudewicz@gmail.com
 */
public class NodeForest implements Serializable {

  private final List<Node> roots = new ArrayList<>();

  public NodeForest(final @NotNull List<TreeItem<NetworkNode>> treeItems) {
    final List<NodePath> paths = treeItems.stream().map(NodePath::new).collect(Collectors.toList());
    NodePath.removeCommonParents(paths);
    paths.forEach(this::addPath);
  }

  private void addPath(final @NotNull NodePath path) {
    if (path.len() != 0) {
      final NetworkNode rootNetworkNode = path.getTopParent();
      final Node root = findOrAddRoot(rootNetworkNode);
      final int len = path.len();
      Node node = root;
      for (int i = 1; i < len; i++) {
        node = node.findOrAddChild(path.getAt(i));
      }
    }
  }

  private Node findOrAddRoot(final @NotNull NetworkNode root) {
    for (final Node node : roots) {
      if (root.equals(node.value)) {
        return node;
      }
    }
    final Node node = new Node(root);
    roots.add(node);
    return node;
  }

  public @NotNull List<Node> getRoots() {
    return roots;
  }

  @Override
  public String toString() {
    return roots.stream().map(Node::toString).collect(Collectors.joining("\n"));
  }

  static class Node implements Serializable {
    @NotNull NetworkNode value;
    private List<Node> children = null;

    Node(final @NotNull NetworkNode value) {
      this.value = value;
    }

    @NotNull
    List<Node> getChildren() {
      if (children == null) {
        children = new ArrayList<>();
      }
      return children;
    }

    @NotNull
    Node findOrAddChild(final @NotNull NetworkNode child) {
      final List<Node> children = getChildren();
      for (final Node node : children) {
        if (child.equals(node.value)) {
          return node;
        }
      }
      final Node node = new Node(child);
      children.add(node);
      return node;
    }

    @Override
    public String toString() {
      return toString(0).toString();
    }

    private CharSequence toString(final int depth) {
      final String prefix = Strings.repeat("  ", depth);
      final StringBuilder builder = new StringBuilder(512);
      builder.append(prefix).append(StringEscapeUtils.escapeJava(value.toString()));
      if (children != null) {
        builder
            .append("\n")
            .append(
                children.stream()
                    .map(child -> child.toString((depth + 1)))
                    .collect(Collectors.joining("\n")));
      }
      return builder;
    }

    boolean hasChildren() {
      return children != null && !children.isEmpty();
    }
  }

  static class NodePath {
    private final LinkedList<NetworkNode> path = new LinkedList<>();

    NodePath(final @NotNull TreeItem<NetworkNode> node) {
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
}
