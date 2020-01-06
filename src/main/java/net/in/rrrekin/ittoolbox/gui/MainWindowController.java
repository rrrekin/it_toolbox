package net.in.rrrekin.ittoolbox.gui;

import static com.google.common.collect.Maps.newHashMap;
import static net.in.rrrekin.ittoolbox.ItToolboxApplication.APPLICATION_NAME;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationPersistenceService;
import net.in.rrrekin.ittoolbox.configuration.OpenConfigurationsService;
import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeConverter;
import net.in.rrrekin.ittoolbox.configuration.nodes.Server;
import net.in.rrrekin.ittoolbox.gui.commands.DeleteNodesCommand;
import net.in.rrrekin.ittoolbox.gui.commands.InsertForestCommand;
import net.in.rrrekin.ittoolbox.gui.commands.InsertLocation;
import net.in.rrrekin.ittoolbox.gui.commands.InsertNodeCommand;
import net.in.rrrekin.ittoolbox.gui.commands.ModifyNodeCommand;
import net.in.rrrekin.ittoolbox.gui.commands.OperationCommand;
import net.in.rrrekin.ittoolbox.gui.model.NodeForest;
import net.in.rrrekin.ittoolbox.gui.model.NodeForestConverter;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceRegistry;
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author michal.rudewicz@gmail.com */
public class MainWindowController {

  @NonNls static final @NotNull Logger log = LoggerFactory.getLogger(MainWindowController.class);

  private static final String[] OWN_EXTENSIONS = {"*.itt", "*.yml", "*.yaml"};
  private static final String[] ALL_EXTENSIONS = {"*.*", "*"};
  private static final String WIDTH_PREF = "w";
  private static final String HEIGHT_PREF = "h";
  private static final String X_PREF = "x";
  private static final String Y_PREF = "y";
  private static final String SPLIT_PREF = "s";
  public static final DataFormat IT_TOOLBOX_DATA = new DataFormat(APPLICATION_NAME);

  private Stage stage;

  @FXML private TreeView<NetworkNode> nodeTree;
  @FXML private SplitPane splitter;
  @FXML private TextFlow nodePreview;

  // Menu items
  @FXML private MenuItem menuFileSave;
  @FXML private MenuItem menuEditEditNode;
  @FXML private MenuItem menuEditUndo;
  @FXML private MenuItem menuEditRedo;
  @FXML private MenuItem menuEditCut;
  @FXML private MenuItem menuEditCopy;
  @FXML private MenuItem menuEditDuplicate;
  @FXML private MenuItem menuEditDelete;

  // Context menu
  @FXML public ContextMenu nodeContextMenu;

  // Buttons
  @FXML Button nodeDeleteButton;
  @FXML Button nodeEditButton;

  private final @NotNull BooleanProperty needSave = new SimpleBooleanProperty(false);
  private final @NotNull BooleanProperty noNodeSelected = new SimpleBooleanProperty(true);
  private final @NotNull BooleanProperty canUndo = new SimpleBooleanProperty(false);
  private final @NotNull BooleanProperty canRedo = new SimpleBooleanProperty(false);
  private Configuration configuration;
  private final @NotNull Deque<OperationCommand> undoStack = new ArrayDeque<>();
  private final @NotNull Deque<OperationCommand> redoStack = new ArrayDeque<>();
  private final @NotNull IntegerProperty stateSerialNumber = new SimpleIntegerProperty(0);
  private @NotNull UserPreferences preferences;
  private final @NotNull UserPreferences genericPreferences;

  private final @NotNull CommonResources commonResources;
  private final @NotNull OpenConfigurationsService openConfigurationsService;
  private final @NotNull UserPreferencesFactory userPreferencesFactory;
  private final @NotNull AppPreferences appPreferences;
  private final @NotNull GuiInvokeService guiInvokeService;
  private final @NotNull NodeConverter nodeConverter;
  private final @NotNull NodeForestConverter nodeForestConverter;
  private final @NotNull ServiceRegistry serviceRegistry;
  private final @NotNull Injector injector;

  @Inject
  public MainWindowController(
      final @NotNull CommonResources commonResources,
      final @NotNull OpenConfigurationsService openConfigurationsService,
      final @NotNull UserPreferencesFactory userPreferencesFactory,
      final @NotNull AppPreferences appPreferences,
      final @NotNull GuiInvokeService guiInvokeService,
      final @NotNull NodeConverter nodeConverter,
      final @NotNull NodeForestConverter nodeForestConverter,
      final @NotNull ServiceRegistry serviceRegistry,
      final @NotNull Injector injector) {
    log.info("Creating MainWindowController");
    this.commonResources =
        Objects.requireNonNull(commonResources, "CommonResources must not be null");
    this.openConfigurationsService =
        Objects.requireNonNull(
            openConfigurationsService, "OpenConfigurationsService must not be null");
    this.userPreferencesFactory =
        Objects.requireNonNull(userPreferencesFactory, "UserPreferencesFactory must not be null");
    this.appPreferences = Objects.requireNonNull(appPreferences, "AppPreferences must not be null");
    this.guiInvokeService =
        Objects.requireNonNull(guiInvokeService, "GuiInvokeService must not be null");
    this.nodeConverter = Objects.requireNonNull(nodeConverter, "NodeConverter must not be null");
    this.nodeForestConverter =
        Objects.requireNonNull(nodeForestConverter, "nodeForestConverter must not be null");
    this.serviceRegistry =
        Objects.requireNonNull(serviceRegistry, "serviceRegistry must not be null");
    this.injector = Objects.requireNonNull(injector, "injector must not be null");

    genericPreferences = userPreferencesFactory.create(this.getClass());
    preferences = genericPreferences;
  }

  public void initialize() {
    log.info("Initializing MainWindowController");
    setupNodeTree();
    setupBindings();
  }

  private void setupNodeTree() {
    nodeTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    nodeTree.setCellFactory(
        treeView ->
            new TextFieldTreeCell<>(
                new StringConverter<>() {
                  @Override
                  public String toString(final NetworkNode object) {
                    return object.getName();
                  }

                  @Override
                  public NetworkNode fromString(final String string) {
                    return null;
                  }
                }));

    nodeTree
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldVal, newVal) -> {
              nodePreview.getChildren().clear();
              nodePreview.getChildren().clear();
              nodePreview.getChildren().addAll(nodeConverter.toGui(newVal));
            });

    nodeTree
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
          (observable, oldValue, newValue) -> {
            populateNodeMenus(newValue);
          });
  }




  private void setupBindings() {
    noNodeSelected.bind(Bindings.isEmpty(nodeTree.getSelectionModel().getSelectedItems()));

    menuFileSave.disableProperty().bind(needSave.not());
    menuEditEditNode.disableProperty().bind(noNodeSelected);
    menuEditUndo.disableProperty().bind(canUndo.not());
    menuEditRedo.disableProperty().bind(canRedo.not());
    menuEditCut.disableProperty().bind(noNodeSelected);
    menuEditCopy.disableProperty().bind(noNodeSelected);
    menuEditDuplicate.disableProperty().bind(noNodeSelected);
    menuEditDelete.disableProperty().bind(noNodeSelected);

    nodeEditButton.disableProperty().bind(noNodeSelected);
    nodeDeleteButton.disableProperty().bind(noNodeSelected);
  }

  public void setConfig(final @Nullable Configuration configuration) {
    this.configuration =
        Objects.requireNonNullElseGet(
            configuration,
            () ->
                new Configuration(
                    new TreeItem<>(new GroupingNode(ConfigurationPersistenceService.ROOT)),
                    newHashMap()));
    undoStack.clear();
    redoStack.clear();
    stateSerialNumber.setValue(0);
    updateCanUndoRedo();
    updateTitleAndPreferences();
    guiInvokeService.runInGui(
        () -> {
          nodeTree.setRoot(this.configuration.getNetworkNodes());
        });
  }

  private void updateTitleAndPreferences() {
    if (this.configuration.getFilePath() != null) {
      preferences =
          userPreferencesFactory.create(this.getClass(), this.configuration.getFilePath());
    } else {
      preferences = genericPreferences;
    }
    final String title =
        this.configuration.getFilePath() == null
            ? APPLICATION_NAME
            : APPLICATION_NAME + ": " + this.configuration.getFilePath().getFileName();
    setTitle(title);
  }

  private void setTitle(final @NotNull String title) {
    stage.setTitle(title);
  }

  private @NotNull Stage getStage() {
    return stage;
  }

  public void setUpStage(final @NotNull Stage stage) {
    this.stage = stage;
    commonResources.setUpStageIcons(stage);
    stage.addEventHandler(
        WindowEvent.WINDOW_SHOWN,
        event -> {
          log.info("Showing window: {}", event);
          final double width = getDoubleFromPrefs(WIDTH_PREF, 800);
          final double height = getDoubleFromPrefs(HEIGHT_PREF, 600);
          final double x = getDoubleFromPrefs(X_PREF, Double.MAX_VALUE);
          final double y = getDoubleFromPrefs(Y_PREF, Double.MAX_VALUE);
          final double split = getDoubleFromPrefs(SPLIT_PREF, 0.3);
          stage.setWidth(width);
          stage.setHeight(height);
          try {
            final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            if (x >= screenBounds.getMinX() && x < screenBounds.getMaxX() - 200) {
              stage.setX(x);
            }
            if (y >= screenBounds.getMinY() && y < screenBounds.getMaxY() - 200) {
              stage.setY(y);
            }
          } catch (final Exception ignore) {
          }
          splitter.setDividerPosition(0, split);
        });
    stage.addEventHandler(
        WindowEvent.WINDOW_HIDING,
        event -> {
          log.debug("Hiding window: {}", event);
          preferences.putDouble(WIDTH_PREF, stage.getWidth());
          preferences.putDouble(HEIGHT_PREF, stage.getHeight());
          preferences.putDouble(X_PREF, stage.getX());
          preferences.putDouble(Y_PREF, stage.getY());
          preferences.putDouble(SPLIT_PREF, splitter.getDividerPositions()[0]);
        });
    stage.addEventHandler(
        WindowEvent.WINDOW_CLOSE_REQUEST,
        event -> {
          log.debug("Closing window: {}", event);
          if (configuration != null && configuration.getFilePath() != null) {
            openConfigurationsService.removeOpenFile(configuration.getFilePath());
          }
        });
  }

  private double getDoubleFromPrefs(final @NotNull String key, final double def) {
    return preferences.getDouble(key, genericPreferences.getDouble(key, def));
  }

  private InsertLocation getInsertionPoint() {
    final TreeItem<NetworkNode> selectedItem = nodeTree.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      if (selectedItem.getValue() instanceof GroupingNode) {
        return new InsertLocation(selectedItem, -1);
      } else {
        int index = selectedItem.getParent().getChildren().indexOf(selectedItem);
        if (index >= 0) {
          index++;
        }
        return new InsertLocation(selectedItem.getParent(), index);
      }
    }
    return new InsertLocation(nodeTree.getRoot(), -1);
  }

  private void editServer(final @NotNull TreeItem<NetworkNode> item) {
    final @NotNull FXMLLoader fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(injector::getInstance);
    final URL location = getClass().getResource("/fxml/server-editor.fxml");
    fxmlLoader.setLocation(location);
    fxmlLoader.setResources(LocaleUtil.getMessages());

    try {
      final Parent content = fxmlLoader.load();
      final NodeEditor controller = fxmlLoader.getController();
      if (content != null && controller != null) {
        final Stage editorStage = new Stage();
        editorStage.setScene(new Scene(content));
        editorStage.initOwner(stage);
        controller.setUpDialog(this, editorStage, item);
        editorStage.show();
      }
    } catch (final IOException e) {
      log.error("Cannot load server editor window definition", e);
      commonResources.fatalError(
          localMessage("ERR_FAILED_LOAD_SERVER_EDITOR_WINDOW_DEFINITION", e.getLocalizedMessage()));
    }
  }

  // === action handling ===

  public void onNewGroup(ActionEvent actionEvent) {
    log.trace("New group: {}", actionEvent);
    final InsertLocation insertionPoint = getInsertionPoint();
    final OperationCommand command =
        new InsertNodeCommand(stateSerialNumber, insertionPoint, new GroupingNode());
    final @Nullable TreeItem<NetworkNode> newItem = executeCommand(command);
    if (newItem != null) {
      editServer(newItem);
    }
  }

  public void onNewServer(ActionEvent actionEvent) {
    log.trace("New server: {}", actionEvent);
    final InsertLocation insertionPoint = getInsertionPoint();
    final OperationCommand command =
        new InsertNodeCommand(stateSerialNumber, insertionPoint, new Server());
    final @Nullable TreeItem<NetworkNode> newItem = executeCommand(command);
    if (newItem != null) {
      editServer(newItem);
    }
  }

  public void onNewGeneric(ActionEvent actionEvent) {
    log.trace("new generic node: {}", actionEvent);
    final InsertLocation insertionPoint = getInsertionPoint();
    final OperationCommand command =
        new InsertNodeCommand(stateSerialNumber, insertionPoint, new GenericNode());
    final @Nullable TreeItem<NetworkNode> newItem = executeCommand(command);
    if (newItem != null) {
      editServer(newItem);
    }
  }

  public void updateNodeAt(
      final @NotNull TreeItem<NetworkNode> item, final @NotNull NetworkNode modifiedNode) {
    if (modifiedNode.equals(item.getValue())) {
      log.debug("Node update not needed");
    } else {
      log.debug("Updating node {} to {}", item.getValue(), modifiedNode);
      final OperationCommand command = new ModifyNodeCommand(stateSerialNumber, item, modifiedNode);
      executeCommand(command);
    }
  }

  private @Nullable TreeItem<NetworkNode> executeCommand(final @NotNull OperationCommand command) {
    log.trace("Executing command {}", command);
    @Nullable TreeItem<NetworkNode> select = null;
    try {
      select = command.execute();
      undoStack.push(command);
      redoStack.clear();
      needSave.set(true);
    } catch (final Exception e) {
      log.error("Unexpected error while executing operation {}", command, e);
      commonResources.errorDialog(
          stage,
          localMessage("APP_UNEXPECTED_APP_ERROR"),
          localMessage("ERR_INTERNAL_ERROR", e.getLocalizedMessage()));
    }
    updateCanUndoRedo();
    return select;
  }

  private void undoCommand() {
    if (undoStack.isEmpty()) {
      log.debug("Trying to undo without anything to undo");
      return;
    }
    final OperationCommand command = undoStack.pop();
    if (command == null) {
      log.debug("Null command on undo stack");
      return;
    }
    log.debug("Undo {}", command);
    try {
      command.revoke();
    } catch (final Exception e) {
      log.error("Unexpected error while undoing operation {}", command, e);
      undoStack.push(
          command); // This potentially blocks undo, but it is necessary to save consistency
      commonResources.errorDialog(
          stage,
          localMessage("APP_UNEXPECTED_APP_ERROR"),
          localMessage("ERR_INTERNAL_ERROR", e.getLocalizedMessage()));
      return;
    }
    redoStack.push(command);
    needSave.set(true);
    updateCanUndoRedo();
  }

  private void redoCommand() {
    if (redoStack.isEmpty()) {
      log.debug("Trying to redo without anything to redo");
      return;
    }
    final OperationCommand command = redoStack.pop();
    if (command == null) {
      log.debug("Null command on redo stack");
      return;
    }
    log.debug("Redo {}", command);
    try {
      command.execute();
    } catch (final Exception e) {
      log.error("Unexpected error while redoing operation {}", command, e);
      redoStack.push(
          command); // This potentially blocks redo, but it is necessary to save consistency
      commonResources.errorDialog(
          stage,
          localMessage("APP_UNEXPECTED_APP_ERROR"),
          localMessage("ERR_INTERNAL_ERROR", e.getLocalizedMessage()));
      return;
    }
    undoStack.push(command);
    needSave.set(true);
    updateCanUndoRedo();
  }

  private void updateCanUndoRedo() {
    canUndo.set(!undoStack.isEmpty());
    canRedo.set(!redoStack.isEmpty());
    //    log.info("Undo
    // stack:\n{}",undoStack.stream().map(Object::toString).collect(Collectors.joining("\n")));
    //    log.info("Redo
    // stack:\n{}",redoStack.stream().map(Object::toString).collect(Collectors.joining("\n")));
    if (canUndo.get()) {
      menuEditUndo.setText(
          localMessage("MM_EDIT_UNDO") + ": " + undoStack.getFirst().getDescription());
    } else {
      menuEditUndo.setText(localMessage("MM_EDIT_UNDO"));
    }
    if (canRedo.get()) {
      menuEditRedo.setText(
          localMessage("MM_EDIT_REDO") + ": " + redoStack.getFirst().getDescription());
    } else {
      menuEditRedo.setText(localMessage("MM_EDIT_REDO"));
    }
  }

  // Command handlers
  public void onOpenFile(final ActionEvent actionEvent) {
    final FileChooser fileChooser = new FileChooser();
    final File initialDirectory = openConfigurationsService.getLastOpenLocation().toFile();
    log.debug("Will try to open dialog in '{}' directory.", initialDirectory);

    fileChooser.setInitialDirectory(initialDirectory);
    fileChooser
        .getExtensionFilters()
        .addAll(
            new ExtensionFilter(localMessage("FF_IT_TOOLBOX_FILES"), OWN_EXTENSIONS),
            new ExtensionFilter(localMessage("FF_ALL_FILES"), ALL_EXTENSIONS));
    fileChooser.setTitle(localMessage("MM_FILE_OPEN"));
    File selectedFile;
    try {
      selectedFile = fileChooser.showOpenDialog(stage);
    } catch (final Exception ignore) {
      log.warn("Last used open directory ({}) do not exist", initialDirectory);
      fileChooser.setInitialDirectory(null);
      selectedFile = fileChooser.showOpenDialog(stage);
    }
    if (selectedFile != null) {
      final File fileToLoad = selectedFile;
      log.info("Selected file: {}", fileToLoad);
      if ((configuration == null
          || configuration.getFilePath() == null) /*&& !configuration.isDirty()*/) {
        // Empty new window - reload in this window
        // Optional: Enable load signalization
        final Configuration config = openConfigurationsService.loadFile(null, fileToLoad);
        if (config != null) {
          setConfig(config);
        }
        // Optional: Disable load signalization
        //      } else if (configuration.getFilePath() == null /*&& configuration.isDirty()*/) {
        //        // Unsaved new edited window - ask what to do
      } else {
        // Window with existing file - load in separate thread
        new Thread(
                () -> {
                  final Configuration config = openConfigurationsService.loadFile(null, fileToLoad);
                  if (config != null) {
                    openConfigurationsService.openNewWindow(config);
                  }
                })
            .start();
      }
    }
  }

  public void onNewFile(final ActionEvent actionEvent) {
    openConfigurationsService.openNewWindow(null);
  }

  public void onQuit(final ActionEvent actionEvent) {
    log.trace("Request to quit application");
    Platform.exit();
  }

  public void onNodeEdit(final @Nullable ActionEvent actionEvent) {
    log.trace("Node edit: {}", actionEvent);
    final TreeItem<NetworkNode> treeItem = nodeTree.getSelectionModel().getSelectedItem();
    if (treeItem == null) {
      log.trace("Nothing selected");
      return;
    }
    editServer(treeItem);
  }

  public void onNodeDelete(final ActionEvent actionEvent) {
    log.trace("Node delete: {}", actionEvent);
    deleteSelection();
  }

  private void deleteSelection() {
    ObservableList<TreeItem<NetworkNode>> selected =
        nodeTree.getSelectionModel().getSelectedItems();
    if (selected != null) {
      final NodeForest selection = new NodeForest(selected);
      final OperationCommand command = new DeleteNodesCommand(stateSerialNumber, selected);
      executeCommand(command);
    }
  }

  public void onCloseFile(final ActionEvent actionEvent) {
    log.trace("Close file: {}", actionEvent);
    stage.close();
  }

  public void onSaveFile(final ActionEvent actionEvent) throws InterruptedException {
    log.trace("Save file: {}", actionEvent);
    if (configuration.getFilePath() == null) {
      onSaveFileAs(actionEvent);
    } else {
      try {
        openConfigurationsService.saveFile(configuration);
        needSave.set(false);
      } catch (final Exception e) {
        log.warn("Failed to save file", e);
        commonResources.errorDialog(stage, localMessage("ERR_SAVE_ERROR"), e.getLocalizedMessage());
      }
    }
  }

  public void onSaveFileAs(final ActionEvent actionEvent) throws InterruptedException {
    log.trace("Save file as: {}", actionEvent);
    FileChooser fileChooser = new FileChooser();
    final File initialDirectory = openConfigurationsService.getLastOpenLocation().toFile();
    log.debug("Will try to open dialog in '{}' directory.", initialDirectory);

    fileChooser.setInitialDirectory(initialDirectory);
    fileChooser
        .getExtensionFilters()
        .addAll(
            new ExtensionFilter(localMessage("FF_IT_TOOLBOX_FILES"), OWN_EXTENSIONS),
            new ExtensionFilter(localMessage("FF_ALL_FILES"), ALL_EXTENSIONS));
    fileChooser.setInitialFileName(localMessage("DEFAULT_FILENAME") + ".itt");
    fileChooser.setTitle(localMessage("MM_FILE_SAVE_AS"));
    File selectedFile;
    try {
      selectedFile = fileChooser.showSaveDialog(stage);
    } catch (final Exception ignore) {
      log.warn("Last used open directory ({}) do not exist", initialDirectory);
      fileChooser.setInitialDirectory(null);
      selectedFile = fileChooser.showSaveDialog(stage);
    }
    if (selectedFile != null) {
      if (selectedFile.isDirectory()) {
        commonResources.errorDialog(
            stage,
            localMessage("ERR_CANNOT_SAVE_FILE"),
            localMessage("ERR_FOLDER_WITH_THE_SAME_NAME"));
        return;
      }
      try {
        openConfigurationsService.saveFileAs(configuration, selectedFile.toPath());
        needSave.set(false);
        updateTitleAndPreferences();
      } catch (final Exception e) {
        log.warn("Failed to save file", e);
        configuration.setFilePath(null);
        commonResources.errorDialog(stage, localMessage("ERR_SAVE_ERROR"), e.getLocalizedMessage());
      }
    }
  }

  public void onPreferences(final ActionEvent actionEvent) {
    log.trace("Preferences: {}", actionEvent);
    // TODO: IMPLEMENT
  }

  public void onUndo(final ActionEvent actionEvent) {
    log.trace("Undo: {}", actionEvent);
    if (!undoStack.isEmpty()) {
      undoCommand();
    }
  }

  public void onRedo(final ActionEvent actionEvent) {
    log.trace("Redo: {}", actionEvent);
    if (!redoStack.isEmpty()) {
      redoCommand();
    }
  }

  public void onCut(final ActionEvent actionEvent) {
    log.trace("Cut: {}", actionEvent);
    putSeletionToClipboard();
    deleteSelection();
  }

  public void onCopy(final ActionEvent actionEvent) {
    log.trace("Copy: {}", actionEvent);
    putSeletionToClipboard();
  }

  private void putSeletionToClipboard() {
    ObservableList<TreeItem<NetworkNode>> selected =
        nodeTree.getSelectionModel().getSelectedItems();
    if (selected == null) {
      log.trace("Nothing selected");
    } else {
      final NodeForest selection = new NodeForest(selected);
      final Clipboard clipboard = Clipboard.getSystemClipboard();
      final ClipboardContent content = new ClipboardContent();
      content.put(IT_TOOLBOX_DATA, selection);
      content.putString(nodeForestConverter.toPlainText(selection));
      content.putHtml(nodeForestConverter.toHtml(selection));
      clipboard.setContent(content);
    }
  }

  public void onOPaste(final ActionEvent actionEvent) {
    log.trace("Paste: {}", actionEvent);
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final InsertLocation insertionPoint = getInsertionPoint();
    final Object nativeClipboard = clipboard.getContent(IT_TOOLBOX_DATA);
    final String textClipboard = clipboard.getString();
    final List<File> filesClipboard = clipboard.getFiles();
    if (nativeClipboard instanceof NodeForest) {
      log.debug("Pasting native:\n{}", nativeClipboard);
      final NodeForest nodeForest = (NodeForest) nativeClipboard;
      final OperationCommand command =
          new InsertForestCommand(
              stateSerialNumber, insertionPoint, nodeForest, localMessage("OP_PASTE"));
      executeCommand(command);
    } else if (filesClipboard != null && !filesClipboard.isEmpty()) {
      commonResources.infoDialog(stage, "NOT IMPLEMENTED", "Not implemented yet");
      log.debug("Pasting files:\n{}", filesClipboard);
      // TODO Implement
    } else if (textClipboard != null) {
      log.debug("Pasting text:\n{}", textClipboard);
      // TODO: IMPLEMENT
      final NodeForest nodeForest = nodeForestConverter.fromText(textClipboard);
      final OperationCommand command =
          new InsertForestCommand(
              stateSerialNumber, insertionPoint, nodeForest, localMessage("OP_PASTE"));
      executeCommand(command);

    } else {
      log.debug("Paste format unrecognized");
    }
  }

  public void onNodeDuplicate(ActionEvent actionEvent) {
    log.trace("Duplicate: {}", actionEvent);
    ObservableList<TreeItem<NetworkNode>> selected =
        nodeTree.getSelectionModel().getSelectedItems();
    if (selected == null) {
      log.trace("Nothing selected");
    } else {
      final NodeForest nodeForest = new NodeForest(selected);
      final InsertLocation insertionPoint = getInsertionPoint();
      final OperationCommand command =
          new InsertForestCommand(
              stateSerialNumber, insertionPoint, nodeForest, localMessage("OP_DUPLICATE"));
      executeCommand(command);
    }
  }

  public void onSelectAll(final ActionEvent actionEvent) {
    log.trace("Select all: {}", actionEvent);
    nodeTree.getSelectionModel().selectAll();
  }

  public void onUnSelectAll(final ActionEvent actionEvent) {
    log.trace("UnselectAll: {}", actionEvent);
    nodeTree.getSelectionModel().clearSelection();
  }

  public void onAbout(final ActionEvent actionEvent) {
    log.trace("About: {}", actionEvent);
    // TODO: IMPLEMENT
  }

  public void onDescriptionClick(final MouseEvent mouseEvent) {
    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() > 1) {
      log.trace("Description clicked: {}", mouseEvent);
      onNodeEdit(null);
    }
  }

  public void onTreeClicked(final MouseEvent mouseEvent) {
    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() > 1) {
      log.trace("Tree clicked: {}", mouseEvent);
      onNodeEdit(null);
    }
  }

  public void populateNodeMenus(final TreeItem<NetworkNode> selectedItem) {
    log.trace("Building menus for: {}", selectedItem);
    final ObservableList<MenuItem> menuItems = nodeContextMenu.getItems();
    menuItems.clear();
    if (selectedItem != null && selectedItem.getValue() != null) {
      final NetworkNode node = selectedItem.getValue();
      final @NotNull List<ServiceDefinition> defaultServices =
          serviceRegistry.getDefaultServicesFor(node.getType());
      final @NotNull List<ServiceDefinition> definedServices =
          serviceRegistry.getDefinedServicesFor(node.getType());
      log.trace("Default services: {}",defaultServices);
      log.trace("Defined services: {}",definedServices);
      final ImmutableList<ServiceDescriptor> serviceDescriptors = node.getServiceDescriptors();
      if (!defaultServices.isEmpty() || !serviceDescriptors.isEmpty()) {
        defaultServices.forEach(
            s -> {
              final ServiceDescriptor defaultDescriptor = s.getDefaultDescriptor();
              final MenuItem serviceMenuItem = new MenuItem(s.getName(defaultDescriptor));
              serviceMenuItem.setOnAction(
                  event -> {
                    s.getExecutor(defaultDescriptor, configuration).execute(stage, node);
                  });
              menuItems.add(serviceMenuItem);
            });
        if (!defaultServices.isEmpty() && !serviceDescriptors.isEmpty()) {
          menuItems.add(new SeparatorMenuItem());
        }
        serviceDescriptors.stream()
            .sorted(Comparator.comparing(o -> o.getType().name()))
            .forEach(
                sd -> {
                  final MenuItem serviceMenuItem = new MenuItem(serviceRegistry.getNameFor(sd));
                  serviceMenuItem.setOnAction(
                      event -> {
                        serviceRegistry.getExecutorFor(sd, configuration).execute(stage, node);
                      });
                  menuItems.add(serviceMenuItem);
                });
      }
    }
  }
}
