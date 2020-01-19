/*
 *
 * Copyright (c) 2020 Michal Rudewicz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.in.rrrekin.ittoolbox.services.editors;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.services.PropertyType;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceProperty;
import net.in.rrrekin.ittoolbox.services.ServiceType;
import net.in.rrrekin.ittoolbox.services.definitions.NmapService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NmapServiceEditorController.
 *
 * @author michal.rudewicz @gmail.com
 */
public class NmapServiceEditorController {

  @NonNls
  private static final Logger log = LoggerFactory.getLogger(NmapServiceEditorController.class);

  private static final String WIDTH_PREF = "w";
  private static final String HEIGHT_PREF = "h";

  private final @NotNull UserPreferences preferences;

  @FXML private VBox sceneContent;
  @FXML private TextField name;
  @FXML private TextField options;
  @FXML private CheckBox evaluateOptions;

  private Stage stage = null;
  private @Nullable ServiceDescriptor response = null;
  private ServiceType type = null;

  @Inject
  public NmapServiceEditorController(final @NotNull UserPreferencesFactory userPreferencesFactory) {
    this.preferences =
        requireNonNull(userPreferencesFactory, "userPreferencesFactory must not be null")
            .create(this.getClass());
  }

  /**
   * On help.
   *
   * @param actionEvent the action event
   */
  public void onHelp(final ActionEvent actionEvent) {
    // TODO: Implement
    log.trace("Help action");
  }

  /**
   * On cancel.
   *
   * @param actionEvent the action event
   */
  public void onCancel(final ActionEvent actionEvent) {
    log.trace("Cancel");
    if (stage != null) {
      stage.close();
    }
  }

  /**
   * On ok.
   *
   * @param actionEvent the action event
   */
  public void onOk(final ActionEvent actionEvent) {
    log.trace("OK");
    if (stage != null) {
      if (type != null) {
        response =
            new ServiceDescriptor(
                type,
                List.of(
                    new ServiceProperty(
                        NmapService.NAME, name.getText(), false, PropertyType.STRING),
                    new ServiceProperty(
                        NmapService.OPTIONS,
                        options.getText(),
                        evaluateOptions.isSelected(),
                        PropertyType.STRING)));
      }
      stage.close();
    }
  }

  /**
   * Show and get input optional.
   *
   * @param owner the owner
   * @param descriptor the descriptor
   * @return the optional
   */
  public @NotNull Optional<ServiceDescriptor> showAndGetInput(
      final @Nullable Stage owner, final @NotNull ServiceDescriptor descriptor) {

    type = descriptor.getType();
    Optional.ofNullable(descriptor.getProperty(NmapService.NAME))
        .map(ServiceProperty::getRawValue)
        .ifPresent(v -> name.setText(v));
    Optional.ofNullable(descriptor.getProperty(NmapService.OPTIONS))
        .ifPresent(
            v -> {
              options.setText(v.getRawValue());
              evaluateOptions.setSelected(v.isEvaluate());
            });
    final Scene scene = new Scene(sceneContent);
    stage = new Stage();
    if (owner != null) {
      stage.initOwner(owner);
    }
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setScene(scene);
    stage.setTitle(localMessage("SERVICE_NMAP_EDITOR_TITLE"));
    final double width = preferences.getDouble(WIDTH_PREF, 300);
    stage.setWidth(width);
    stage.setMinWidth(250);
    log.debug("Min: {}x{}",sceneContent.getMinWidth(),sceneContent.getMinHeight());
    stage.addEventHandler(
        WindowEvent.WINDOW_HIDING,
        event -> {
          log.debug("Hiding nmap editor window: {}", event);
          preferences.putDouble(WIDTH_PREF, stage.getWidth());
        });
    stage.addEventHandler(
        WindowEvent.WINDOW_SHOWN,
        event -> {
          stage.setMinHeight(stage.getHeight());
          stage.setMaxHeight(stage.getHeight());
        });
    response = null;
//    stage.show();
//    stage.setMinHeight(stage.getHeight());
//    stage.setMaxHeight(stage.getHeight());
    stage.showAndWait();
    return Optional.ofNullable(response);
  }
}
