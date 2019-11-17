package net.in.rrrekin.ittoolbox.gui.nodetree;

import static com.google.common.collect.Lists.newArrayList;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.NEW;
import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.OK;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager;
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent;
import net.in.rrrekin.ittoolbox.gui.EdtInvokeService;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link TreeModel} facade for {@link ConfigurationManager}.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class NetworkNodesTreeModelFacade implements TreeModel {

  private static final String INVALID_NODE = "?";

  private final @NonNull ConfigurationManager configurationManager;
  private final @NonNull EventBus eventBus;
  private final @NonNull EdtInvokeService invokeService;

  private final EventListenerList listeners = new EventListenerList();
  private final GroupingNode root;

  /**
   * Instantiates a new Network nodes tree model facade.
   *
   * @param configurationManager the configuration manager
   * @param eventBus the event bus
   */
  @Inject
  public NetworkNodesTreeModelFacade(
      final @NonNull ConfigurationManager configurationManager,
      final @NonNull EventBus eventBus,
      final @NonNull EdtInvokeService invokeService) {
    log.debug("Creating NetworkNodesTreeModelFacade");
    this.configurationManager = configurationManager;
    this.eventBus = eventBus;
    this.invokeService = invokeService;
    root =
        new GroupingNode(localMessage("N_ROOT_NODE"), "", newArrayList(), Collections.emptyList());
    root.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.APPS, NetworkNode.ICON_SIZE));
  }

  /** Init. */
  public void init() {
    log.debug("Initializing NetworkNodesTreeModelFacade");
    eventBus.register(this);
  }

  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent instanceof NetworkNode) {
      final NetworkNode node = (NetworkNode) parent;
      final List<NetworkNode> childNodes = node.getChildNodes();
      if (index >= 0 && index < childNodes.size()) {
        return childNodes.get(index);
      }
    }
    return INVALID_NODE;
  }

  @Override
  public int getChildCount(final Object parent) {
    if (parent instanceof NetworkNode) {
      final NetworkNode node = (NetworkNode) parent;
      final List<NetworkNode> childNodes = node.getChildNodes();
      return childNodes.size();
    }
    return 0;
  }

  @Override
  public boolean isLeaf(final Object node) {
    if (node instanceof NetworkNode) {
      return ((NetworkNode) node).isLeaf();
    }
    return true;
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    final Object object = path.getLastPathComponent();
    if (object instanceof NetworkNode) {
      final NetworkNode node = (NetworkNode) object;
      node.setName(StringUtils.toStringOrEmpty(newValue));
      nodeChanged(path);
    }
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    if (parent instanceof NetworkNode && child instanceof NetworkNode) {
      final NetworkNode parentNode = (NetworkNode) parent;
      final NetworkNode childNode = (NetworkNode) child;
      final List<NetworkNode> childNodes = parentNode.getChildNodes();
      return childNodes.indexOf(childNode);
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(final TreeModelListener listener) {
    log.trace("Adding listener: {}", listener);
    listeners.add(TreeModelListener.class, listener);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener listener) {
    log.trace("Removing listener: {}", listener);
    listeners.remove(TreeModelListener.class, listener);
  }

  /**
   * Handle file synchronization events.
   *
   * @param event the event
   */
  @Subscribe
  public void handleFileSynchronizationEvents(final @NotNull ConfigurationFileSyncEvent event) {
    log.trace("File sync eevt received: {}", event);
    if (event.getCode() == OK || event.getCode() == NEW) {
      invokeService.runInEdt(
          () -> {
            root.getChildNodes().clear();
            root.getChildNodes().addAll(configurationManager.getConfig().getNetworkNodes());
            this.notifyListenersModelReloaded();
          });
    }
  }

  /** implemented according to reference design - {@link EventListenerList}. */
  private void notifyListenersModelReloaded() {
    final Object[] listenersList = listeners.getListenerList();
    TreeModelEvent changeEvent = null;
    for (int i = listenersList.length - 2; i >= 0; i -= 2) {
      if (listenersList[i] == TreeModelListener.class) {
        if (changeEvent == null) {
          changeEvent = new TreeModelEvent(this, new TreePath(root));
        }
        ((TreeModelListener) listenersList[i + 1]).treeStructureChanged(changeEvent);
      }
    }
  }

  /**
   * Notify listeners that particular node has been modified. Also sets configuration to dirty
   * state.
   *
   * @param node the modified node
   */
  public void nodeChanged(final @NonNull NetworkNode node) {
    final TreePath path = getPathForNode(new TreePath(root), node);
    if (path != null) {
      nodeChanged(path);
    }
  }

  public void nodeChanged(final @NonNull TreePath path) {
    final Object[] listenersList = listeners.getListenerList();
    final TreePath parentPath = path.getParentPath();
    final Object parentObject = parentPath == null ? null : parentPath.getLastPathComponent();
    final Object childObject = path.getLastPathComponent();
    final NetworkNode parent;
    final NetworkNode child;
    if ((parentObject instanceof NetworkNode || parentObject == null)
        && childObject instanceof NetworkNode) {
      configurationManager.setDirty(true);
      parent = (NetworkNode) parentObject;
      child = (NetworkNode) childObject;
      final TreeModelEvent changeEvent =
          new TreeModelEvent(
              this,
              parentPath,
              new int[] {parent == null ? 0 : parent.getChildNodes().indexOf(child)},
              new NetworkNode[] {child});
      for (int i = listenersList.length - 2; i >= 0; i -= 2) {
        if (listenersList[i] == TreeModelListener.class) {
          ((TreeModelListener) listenersList[i + 1]).treeNodesChanged(changeEvent);
        }
      }
    }
  }

  private @Nullable TreePath getPathForNode(
      final @NotNull TreePath treePath, final @NotNull NetworkNode node) {
    // TODO: consider introduction of children to parent linking to simplify code
    final NetworkNode parent = (NetworkNode) treePath.getLastPathComponent();
    if (node == parent) {
      return treePath;
    } else {
      final @NotNull List<@NotNull NetworkNode> childNodes = parent.getChildNodes();
      for (final NetworkNode child : childNodes) {
        final @NotNull TreePath childPath = treePath.pathByAddingChild(child);
        if (child == node) {
          return childPath;
        } else {
          final @Nullable TreePath foundPath = getPathForNode(childPath, node);
          if (foundPath != null) {
            return foundPath;
          }
        }
      }
    }

    return null;
  }
}
