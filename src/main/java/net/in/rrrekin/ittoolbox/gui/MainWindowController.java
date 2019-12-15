package net.in.rrrekin.ittoolbox.gui;

import static com.google.common.collect.Maps.newHashMap;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.inject.Inject;
import java.io.File;
import java.util.Objects;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import net.in.rrrekin.ittoolbox.ItToolboxApplication;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.ConfigurationPersistenceService;
import net.in.rrrekin.ittoolbox.configuration.OpenConfigurationsService;
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeConverter;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
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

  private Stage stage;

  @FXML private TreeView<NetworkNode> nodeTree;
  @FXML private SplitPane splitter;
  @FXML private TextFlow nodePreview;

  private Configuration configuration;
  private @NotNull UserPreferences preferences;
  private @NotNull UserPreferences genericPreferences;

  private final @NotNull CommonResources commonResources;
  private final @NotNull OpenConfigurationsService openConfigurationsService;
  private final @NotNull UserPreferencesFactory userPreferencesFactory;
  private final @NotNull AppPreferences appPreferences;
  private final @NotNull GuiInvokeService guiInvokeService;
  private final @NotNull NodeConverter nodeConverter;

  @Inject
  public MainWindowController(
      final @NotNull CommonResources commonResources,
      final @NotNull OpenConfigurationsService openConfigurationsService,
      final @NotNull UserPreferencesFactory userPreferencesFactory,
      final @NotNull AppPreferences appPreferences,
      final @NotNull GuiInvokeService guiInvokeService,
      final @NotNull NodeConverter nodeConverter) {
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

    genericPreferences = userPreferencesFactory.create(this.getClass());
    preferences = genericPreferences;
  }

  public void initialize() {
    log.info("Initializing MainWindowController");
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
  }

  public void setConfig(final @Nullable Configuration configuration) {
    this.configuration =
        Objects.requireNonNullElseGet(
            configuration,
            () ->
                new Configuration(
                    new TreeItem<>(new GroupingNode(ConfigurationPersistenceService.ROOT)),
                    newHashMap()));
    if (this.configuration.getFilePath() != null) {
      preferences =
          userPreferencesFactory.create(this.getClass(), this.configuration.getFilePath());
    } else {
      preferences = genericPreferences;
    }
    final String title =
        this.configuration.getFilePath() == null
            ? ItToolboxApplication.APPLICATION_NAME
            : ItToolboxApplication.APPLICATION_NAME
                + ": "
                + this.configuration.getFilePath().getFileName();
    setTitle(title);
    guiInvokeService.runInGui(
        () -> {
          nodeTree.setRoot(this.configuration.getNetworkNodes());
        });
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

  public void onNewFile(ActionEvent actionEvent) {
    openConfigurationsService.openNewWindow(null);
  }

  public void onQuit(ActionEvent actionEvent) {
    log.info("Request to quit application");
    Platform.exit();
  }

  public void onNodeEdit(ActionEvent actionEvent) {
    log.info("Node edit: {}", actionEvent);
  }

  public void onNodeDelete(ActionEvent actionEvent) {
    log.info("Node delete: {}", actionEvent);
  }
}
