package net.in.rrrekin.ittoolbox.gui;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.configuration.nodes.Server;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.FontAwesome.Glyph;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller of server editor duialog.
 *
 * @author michal.rudewicz@gmail.com
 */
public class NodeEditor {

  @NonNls private static final @NotNull Logger log = LoggerFactory.getLogger(NodeEditor.class);
  public static final String WIDTH_PREF = "w";
  public static final String HEIGHT_PREF = "h";
  public static final String SPLIT_PREF = "s";

  private final @NotNull UserPreferencesFactory userPreferencesFactory;
  private @Nullable UserPreferences preferences;
  private MainWindowController mainWindowController;

  private Stage stage;

  @FXML private TextField name;
  @FXML private Label addressLabel;
  @FXML private TextField address;
  @FXML private TextArea description;
  @FXML private ListView<String> properties;
  @FXML private Button editPropertyButton;
  @FXML private Button removePropertyButton;
  @FXML private ListView<String> services;
  @FXML private Button editServiceButton;
  @FXML private Button removeServiceButton;
  @FXML private CheckBox useIconGradient;
  @FXML private Pane iconPane;
  @FXML private ColorPicker iconColor;
  @FXML private SearchableComboBox<Glyph> iconSelector;
  @FXML private BorderPane propertiesPane;
  @FXML private BorderPane descriptionPane;
  @FXML private BorderPane servicePane;
  @FXML private SplitPane splitPane;

  private NetworkNode node;
  private TreeItem<NetworkNode> item;

  @Inject
  public NodeEditor(final @NotNull UserPreferencesFactory userPreferencesFactory) {
    log.debug("Creating ServerEditor");

    this.userPreferencesFactory =
        requireNonNull(userPreferencesFactory, "userPreferencesFactory must be not null");
  }

  public void initialize() {
    log.debug("Initializing ServerEditor");
    iconSelector.getItems().addAll(FontAwesome.Glyph.values());
    iconSelector.setCellFactory(
        new Callback<>() {
          @Override
          public ListCell<Glyph> call(final ListView<Glyph> param) {
            return new ListCell<>() {
              @Override
              protected void updateItem(Glyph item1, boolean empty) {
                super.updateItem(item1, empty);
                if (item1 == null || empty) {
                  setText(null);
                  setGraphic(null);
                } else {
                  setText(item1.name());
                  setGraphic(new IconDescriptor(item1, Color.BLACK, false).getIcon());
                }
              }
            };
          }
        });
    iconSelector.setEditable(false);
  }

  public void setUpDialog(
      final @NotNull MainWindowController mainWindowController,
      final @NotNull Stage stage,
      final @NotNull TreeItem<NetworkNode> item) {
    this.mainWindowController =
        requireNonNull(mainWindowController, "mainWindowController must not be null");
    this.stage = requireNonNull(stage, "stage must not be null");
    this.item = requireNonNull(item, "selected item must not be null");
    this.node = requireNonNull(item.getValue(), "selected node must not be null");
    log.debug("Prepare edit dialog for {}", node);
    prepareDialogContent(node);
    prepareStage(stage, node);
  }

  private void prepareDialogContent(final @NotNull NetworkNode node) {
    preferences = userPreferencesFactory.create(node.getClass());

    if (!(node instanceof Server)) {
      addressLabel.setVisible(false);
      address.setVisible(false);
      addressLabel.setMaxHeight(0);
      address.setMaxHeight(0);
    }
    if (node instanceof GroupingNode) {
      splitPane.getItems().remove(propertiesPane);
    }
    populateForm();
  }

  private void populateForm() {
    name.setText(node.getName());
    if (node instanceof Server) {
      address.setText(((Server) node).getAddress());
    }
    description.setText(node.getDescription());
    properties
        .getItems()
        .addAll(
            node.getProperties().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.toList()));
    services
        .getItems()
        .addAll(
            node.getServiceDescriptors().stream()
                .map(ServiceDescriptor::toString)
                .collect(Collectors.toList()));
    final @NotNull IconDescriptor iconDescriptor = node.getIconDescriptor();
    iconSelector.setValue(iconDescriptor.getGlyph());
    iconColor.setValue(iconDescriptor.getColor());
    useIconGradient.setSelected(iconDescriptor.isGradient());
    generateIcon();
  }

  private void prepareStage(final @NotNull Stage stage, final @NotNull NetworkNode node) {
    final double width = preferences.getDouble(WIDTH_PREF, 480);
    final double height = preferences.getDouble(HEIGHT_PREF, 480);
    final List<Double> splitterPos = preferences.getList(SPLIT_PREF, null, Double.class);
    log.trace("Server editor window size: {}x{}", width, height);
    stage.setTitle(localMessage("NODE_EDITOR_TITLE", node.getName(), node.getLocalNodeTypeName()));
    stage.setWidth(width);
    stage.setHeight(height);

    stage.addEventHandler(
        WindowEvent.WINDOW_SHOWN,
        event -> {
          final double[] dividerPositions = splitPane.getDividerPositions();
          if (splitterPos != null && splitterPos.size() == dividerPositions.length) {
            log.debug("Set splitter: {}", splitterPos);
            splitPane.setDividerPositions(Doubles.toArray(splitterPos));
          } else {
            log.debug(
                "Cannot set {} as divider positions - current:{}", splitterPos, dividerPositions);
          }
        });

    stage.addEventHandler(
        WindowEvent.WINDOW_HIDING,
        event -> {
          log.debug("Hiding server editorwindow: {}", event);
          if (preferences != null) {
            preferences.putDouble(WIDTH_PREF, stage.getWidth());
            preferences.putDouble(HEIGHT_PREF, stage.getHeight());
            preferences.putList(
                SPLIT_PREF,
                Arrays.stream(splitPane.getDividerPositions())
                    .boxed()
                    .collect(Collectors.toList()));
          }
        });

    final Window parent = stage.getOwner();
    if (parent != null) {
      final double yPos = parent.getY() + (parent.getHeight() - height) / 2;
      final double xPos = parent.getX() + (parent.getWidth() - width) / 2;
      log.trace("Server editor window position: {}, {}", xPos, yPos);
      if (yPos > 0) {
        stage.setY(yPos);
      }
      if (xPos > 0) {
        stage.setX(xPos);
      }
    }
  }

  private NetworkNode getModifiedNode() {
    final NetworkNode newNode;
    Glyph selectedIcon = iconSelector.getValue();
    if (selectedIcon == null) {
      selectedIcon = node.getIconDescriptor().getGlyph();
    }
    final IconDescriptor newIcon =
        new IconDescriptor(selectedIcon, iconColor.getValue(), useIconGradient.isSelected());
    // TODO: implement
    final @NotNull Map<String, String> newProperties = node.getProperties();
    final @NotNull ImmutableList<ServiceDescriptor> newServices = node.getServiceDescriptors();

    if (node instanceof Server) {
      newNode =
          new Server(
              name.getText(),
              address.getText(),
              description.getText(),
              newIcon,
              newProperties,
              newServices);
    } else if (node instanceof GenericNode) {
      newNode =
          new GenericNode(
              name.getText(), description.getText(), newIcon, newProperties, newServices);
    } else if (node instanceof GroupingNode) {
      newNode = new GroupingNode(name.getText(), description.getText(), newIcon, newServices);
    } else {
      throw new IllegalStateException("Invalid node type");
    }

    return newNode;
  }

  private void generateIcon() {
    iconPane.getChildren().clear();
    Glyph selectedIcon = iconSelector.getValue();
    if (selectedIcon == null) {
      selectedIcon = node.getIconDescriptor().getGlyph();
    }
    iconPane
        .getChildren()
        .add(
            new IconDescriptor(selectedIcon, iconColor.getValue(), useIconGradient.isSelected())
                .getIcon()
                .size(64));
  }

  public void onAddProperty(final ActionEvent actionEvent) {
    log.trace("Add property: {}", actionEvent);
  }

  public void onRemoveProperty(final ActionEvent actionEvent) {
    log.trace("Demove property: {}", actionEvent);
  }

  public void onEditProperty(final ActionEvent actionEvent) {
    log.trace("Edit property: {}", actionEvent);
  }

  public void onEditService(final ActionEvent actionEvent) {
    log.trace("Edit service: {}", actionEvent);
  }

  public void onAddService(final ActionEvent actionEvent) {
    log.trace("Add service: {}", actionEvent);
  }

  public void onRemoveService(final ActionEvent actionEvent) {
    log.trace("Remove service: {}", actionEvent);
  }

  public void onFormReset(final ActionEvent actionEvent) {
    log.trace("Reset form: {}", actionEvent);
    populateForm();
  }

  public void onFormOk(final ActionEvent actionEvent) {
    log.trace("OK: {}", actionEvent);
    mainWindowController.updateNodeAt(item, getModifiedNode());
    stage.close();
  }

  public void onFormCancel(final ActionEvent actionEvent) {
    log.trace("Cancel: {}", actionEvent);
    stage.close();
  }

  public void onFormApply(final ActionEvent actionEvent) {
    log.trace("Apply: {}", actionEvent);
    mainWindowController.updateNodeAt(item, getModifiedNode());
  }

  public void onIconChange(final ActionEvent actionEvent) {
    log.trace("Icon change: {}", actionEvent);
    generateIcon();
  }
}
