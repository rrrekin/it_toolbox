//package net.in.rrrekin.ittoolbox.gui;
//
//import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
//import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
//import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
//
//import com.google.common.collect.ImmutableList;
//import com.google.inject.Inject;
//import com.intellij.uiDesigner.core.GridConstraints;
//import com.intellij.uiDesigner.core.GridLayoutManager;
//import java.awt.Dimension;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.util.concurrent.Semaphore;
//import javax.swing.AbstractAction;
//import javax.swing.Action;
//import javax.swing.BorderFactory;
//import javax.swing.ImageIcon;
//import javax.swing.JComponent;
//import javax.swing.JEditorPane;
//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JSplitPane;
//import javax.swing.JTree;
//import javax.swing.KeyStroke;
//import javax.swing.SwingUtilities;
//import javax.swing.ToolTipManager;
//import javax.swing.event.TreeModelEvent;
//import javax.swing.event.TreeModelListener;
//import javax.swing.tree.TreeSelectionModel;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
//import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
//import net.in.rrrekin.ittoolbox.configuration.nodes.Server;
//import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodeTreeCellEditor;
//import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodeTreeCellRenderer;
//import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodesTreeModelFacade;
//import net.in.rrrekin.ittoolbox.gui.nodetree.NodeTreePopupListener;
//import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.PropertyKey;
//
///**
// * Application main window.
// *
// * @author michal.rudewicz@gmail.com
// */
//@Slf4j
//public class MainWindow {
//
//  private static final Dimension MINIMUM_SIZE = new Dimension(400, 300);
//  private static final Dimension PREFERRED_SIZE = new Dimension(600, 400);
//  public static final String HTML_CONTENT_TYPE = "text/html";
//
//  private final @NotNull JFrame mainWindow;
//  private final Semaphore runningSemaphore = new Semaphore(0);
//  private final @NonNull NetworkNodesTreeModelFacade treeModel;
//
//  JTree nodesTree;
//  private JEditorPane nodeDescription;
//  private JPanel contentPane;
//  private JPanel statusBar;
//
//  // Editor windows
//
//  private final ServerEditor serverEditor = new ServerEditor();
//
//  // File menu actions
//  private JMenu fileMenu;
//  private final Action preferencesAction = preferencesAction();
//  private final Action exitAction = exitAction();
//
//  // Help menu actions
//  private JMenu helpMenu;
//  private final Action aboutAction = aboutAction();
//
//  // Node menu actions
//  private JMenu nodeMenu;
//  @Getter
//  private final Action addServerAction = addServerAction();
//  @Getter
//  private final Action addGenericAction = addGenericAction();
//  @Getter
//  private final Action addGroupAction = addGroupAction();
//  @Getter
//  private final Action editNodeAction = editNodeAction();
//
//  @Inject
//  public MainWindow(final @NonNull NetworkNodesTreeModelFacade treeModel) {
//    this.treeModel = treeModel;
//    $$$setupUI$$$();
//    mainWindow = new JFrame(localMessage("MW_TITLE"));
//    log.info("Creating main window");
//    mainWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//    try {
//      mainWindow.setIconImages(
//          ImmutableList.of(
//              new ImageIcon(getClass().getResource("/icons/it_toolbox_16.png")).getImage(),
//              new ImageIcon(getClass().getResource("/icons/it_toolbox_24.png")).getImage(),
//              new ImageIcon(getClass().getResource("/icons/it_toolbox_32.png")).getImage(),
//              new ImageIcon(getClass().getResource("/icons/it_toolbox_48.png")).getImage(),
//              new ImageIcon(getClass().getResource("/icons/it_toolbox_64.png")).getImage()));
//    } catch (final RuntimeException e) {
//      log.warn("Failed to load application icon", e);
//    }
//    mainWindow.setContentPane(contentPane);
//    nodeDescription.setContentType(HTML_CONTENT_TYPE);
//    nodesTree.setModel(treeModel);
//    treeModel.addTreeModelListener(new NodeTreeModelListener());
//    nodesTree.setCellRenderer(new NetworkNodeTreeCellRenderer());
//    nodesTree.setCellEditor(new NetworkNodeTreeCellEditor());
//    nodesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
//    nodesTree.setShowsRootHandles(true);
//    nodesTree.addMouseListener(new NodeTreePopupListener(this));
//    disableAddNode();
//    editNodeAction.setEnabled(false);
//    nodesTree.addTreeSelectionListener(
//        event -> {
//          final Object node = nodesTree.getLastSelectedPathComponent();
//          if (node instanceof NetworkNode) {
//            if (node == treeModel.getRoot()) {
//              addServerAction.setEnabled(false);
//              addGenericAction.setEnabled(false);
//              addGroupAction.setEnabled(true);
//              editNodeAction.setEnabled(false);
//              editNodeAction.putValue(Action.NAME, localMessage("MM_NODE_EDIT"));
//            } else if (node instanceof GroupingNode) {
//              enableAddNode();
//              editNodeAction.setEnabled(true);
//              editNodeAction.putValue(
//                  Action.NAME, localMessage("MM_NODE_EDIT." + node.getClass().getSimpleName()));
//            } else {
//              disableAddNode();
//              editNodeAction.setEnabled(true);
//              editNodeAction.putValue(
//                  Action.NAME, localMessage("MM_NODE_EDIT." + node.getClass().getSimpleName()));
//            }
//            nodeDescription.setText(((NetworkNode) node).toHtml());
//          } else {
//            disableAddNode();
//            nodeDescription.setText("");
//          }
//        });
//    ToolTipManager.sharedInstance().registerComponent(nodesTree);
//    mainWindow.setMinimumSize(MINIMUM_SIZE);
//    mainWindow.setJMenuBar(createMainMenu());
//
//    mainWindow.addWindowListener(
//        new WindowAdapter() {
//          public void windowClosing(final WindowEvent e) {
//            exit();
//          }
//        });
//
//    // TODO: Replace with restoration of previous values
//    mainWindow.setPreferredSize(PREFERRED_SIZE);
//    mainWindow.setLocationRelativeTo(null);
//    serverEditor.setDefaultCloseOperation(HIDE_ON_CLOSE);
//    serverEditor.pack();
//    serverEditor.setLocationRelativeTo(null);
//  }
//
//  private JMenuBar createMainMenu() {
//    final JMenuBar menuBar = new JMenuBar();
//
//    // FILE menu
//    fileMenu = createMenu("MM_FILE");
//    menuBar.add(fileMenu);
//    fileMenu.add(new JMenuItem(preferencesAction));
//    fileMenu.add(new JMenuItem(exitAction));
//
//    nodeMenu = createMenu("MM_NODE");
//    menuBar.add(nodeMenu);
//    nodeMenu.add(new JMenuItem(addServerAction));
//    nodeMenu.add(new JMenuItem(addGenericAction));
//    nodeMenu.add(new JMenuItem(addGroupAction));
//    nodeMenu.addSeparator();
//    nodeMenu.add(new JMenuItem(editNodeAction));
//
//    // HELP menu
//    helpMenu = createMenu("MM_HELP");
//    menuBar.add(helpMenu);
//    helpMenu.add(new JMenuItem(aboutAction));
//
//    return menuBar;
//  }
//
//  private JMenu createMenu(
//      @PropertyKey(resourceBundle = LocaleUtil.MESSAGES_PROPERTY_BUNDLE) final @NotNull String id) {
//    final JMenu menu = new JMenu(localMessage(id));
//    final Character mnemonicChar = LocaleUtil.localCharacter(id + ".mnemonic");
//    if (mnemonicChar != null) {
//      menu.setMnemonic(KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    menu.setToolTipText(localMessage(id + ".tooltip"));
//    return menu;
//  }
//
//  public void start() {
//    SwingUtilities.invokeLater(this::createAndShowGUI);
//    // Wait until window closed
//    try {
//      runningSemaphore.acquire();
//    } catch (final InterruptedException ignored) {
//      Thread.currentThread().interrupt();
//    }
//    log.debug("Main window finished it's operations");
//  }
//
//  void exit() {
//    serverEditor.dispose();
//    mainWindow.setVisible(false);
//    mainWindow.dispose();
//    log.debug("Application exit requested");
//    runningSemaphore.release();
//  }
//
//  void createAndShowGUI() {
//    // TODO: Remove temp code
//    // TODO: https://github.com/dorkbox/SystemTray
//    //    try {
//    //      UIManager.setLookAndFeel(new DarculaLaf());
//    ////            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//    //////            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//    ////      //      MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
//    //////            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
//    //    } catch (final Exception e) {
//    //      log.warn("Failed to set Look & Feel", e);
//    //    }
//    mainWindow.pack();
//    mainWindow.setVisible(true);
//    log.debug("Main window displayed");
//  }
//
//  private void disableAddNode() {
//    addGenericAction.setEnabled(false);
//    addGroupAction.setEnabled(false);
//    addServerAction.setEnabled(false);
//  }
//
//  private void enableAddNode() {
//    addGenericAction.setEnabled(true);
//    addGroupAction.setEnabled(true);
//    addServerAction.setEnabled(true);
//  }
//
//  private Action exitAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            exit();
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_FILE_EXIT"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_FILE_EXIT.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "exit");
//    action.putValue(
//        Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_FILE_EXIT.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action preferencesAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            log.error("NOT IMPLEMENTED PREFERENCES ACTION");
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_FILE_PREFERENCES"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_FILE_PREFERENCES.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "preferences");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_FILE_PREFERENCES.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action addServerAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            log.error("NOT IMPLEMENTED MM_NODE_ADD_SERVER ACTION");
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_NODE_ADD_SERVER"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_NODE_ADD_SERVER.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "add_server");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_NODE_ADD_SERVER.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action addGenericAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            log.error("NOT IMPLEMENTED MM_NODE_ADD_GENERIC ACTION");
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_NODE_ADD_GENERIC"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_NODE_ADD_GENERIC.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "add_generic");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_NODE_ADD_GENERIC.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action addGroupAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            log.error("NOT IMPLEMENTED MM_NODE_ADD_GROUP ACTION");
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_NODE_ADD_GROUP"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_NODE_ADD_GROUP.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "add_group");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_NODE_ADD_GROUP.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action editNodeAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            final Object node = nodesTree.getLastSelectedPathComponent();
//            if (node instanceof Server) {
//              final Server server = (Server) node;
//              log.debug("Edit {}", server);
//              serverEditor.openForServer(server);
//              if (serverEditor.isChanged()) {
//                treeModel.nodeChanged(server);
//              }
//              log.debug("End of editing {}", server);
//            } else {
//              log.error("NOT IMPLEMENTED MM_NODE_EDIT ACTION: {}", node);
//            }
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_NODE_EDIT"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_NODE_EDIT.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "edit_node");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_NODE_EDIT.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  private Action aboutAction() {
//    final Action action =
//        new AbstractAction() {
//          @Override
//          public void actionPerformed(final ActionEvent e) {
//            log.error("NOT IMPLEMENTED ABOUT ACTION");
//          }
//        };
//    action.putValue(Action.NAME, localMessage("MM_HELP_ABOUT"));
//    action.putValue(Action.SHORT_DESCRIPTION, localMessage("MM_HELP_ABOUT.tooltip"));
//    action.putValue(Action.ACTION_COMMAND_KEY, "about");
//    action.putValue(
//        Action.ACCELERATOR_KEY,
//        KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
//    final Character mnemonicChar = LocaleUtil.localCharacter("MM_HELP_ABOUT.mnemonic");
//    if (mnemonicChar != null) {
//      action.putValue(Action.MNEMONIC_KEY, KeyEvent.getExtendedKeyCodeForChar(mnemonicChar));
//    }
//    return action;
//  }
//
//  /**
//   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
//   * call it in your code!
//   *
//   * @noinspection ALL
//   */
//  private void $$$setupUI$$$() {
//    contentPane = new JPanel();
//    contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
//    final JSplitPane splitPane1 = new JSplitPane();
//    splitPane1.setDividerLocation(170);
//    contentPane.add(splitPane1,
//      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
//        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null,
//        new Dimension(400, 300), null, 0, false));
//    nodesTree = new JTree();
//    nodesTree.setEditable(true);
//    nodesTree.setName("NODE_TREE");
//    nodesTree.setRootVisible(false);
//    nodesTree.setShowsRootHandles(false);
//    splitPane1.setLeftComponent(nodesTree);
//    nodeDescription = new JEditorPane();
//    nodeDescription.setEditable(false);
//    nodeDescription.setName("NODE_DESCRIPTION");
//    splitPane1.setRightComponent(nodeDescription);
//    statusBar = new JPanel();
//    statusBar.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
//    contentPane.add(statusBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_SOUTH,
//      GridConstraints.FILL_HORIZONTAL,
//      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
//      GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 16), new Dimension(-1, 16), null, 0,
//      false));
//    statusBar.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
//  }
//
//  /**
//   * @noinspection ALL
//   */
//  public JComponent $$$getRootComponent$$$() {
//    return contentPane;
//  }
//
//  private class NodeTreeModelListener implements TreeModelListener {
//
//    NodeTreeModelListener() {
//    }
//
//    @Override
//    public void treeNodesChanged(final TreeModelEvent e) {
//      if (e.getChildren().length > 0
//          && e.getChildren()[0] == nodesTree.getSelectionPath().getLastPathComponent()) {
//        nodeDescription.setText(
//            ((NetworkNode) nodesTree.getSelectionPath().getLastPathComponent()).toHtml());
//      }
//    }
//
//    @Override
//    public void treeNodesInserted(final TreeModelEvent e) {
//      // not used
//    }
//
//    @Override
//    public void treeNodesRemoved(final TreeModelEvent e) {
//      // not used
//
//    }
//
//    @Override
//    public void treeStructureChanged(final TreeModelEvent e) {
//      // not used
//
//    }
//  }
//}
