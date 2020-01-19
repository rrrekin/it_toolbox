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

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NmapServiceEditor - implements {@link ServiceEditor} for {@link
 * net.in.rrrekin.ittoolbox.services.definitions.NmapService}.
 *
 * @author michal.rudewicz @gmail.com
 */
public class NmapServiceEditor implements ServiceEditor {

  @NonNls private static final Logger log = LoggerFactory.getLogger(NmapServiceEditor.class);
  private static final String EDITOR_FXML = "/fxml/nmapEditor.fxml";

  private final @NotNull CommonResources commonResources;

  /**
   * Instantiates a new NmapServiceEditor.
   *
   * @param commonResources the CommonResources instance to use
   */
  public NmapServiceEditor(final CommonResources commonResources) {
    log.debug("Creating NmapServiceEditor.");
    this.commonResources = requireNonNull(commonResources, "commonResources must not be null");
  }

  @Override
  public Optional<ServiceDescriptor> openEditorAndGetDefinition(
      @Nullable final Stage owner,
      @NotNull final ServiceDescriptor descriptor,
      @NotNull final NetworkNode node) {

    try {
      final @NotNull FXMLLoader fxmlLoader = commonResources.loadFxml(EDITOR_FXML);
      final Node content = fxmlLoader.load();
      final NmapServiceEditorController controller = fxmlLoader.getController();
      if (content != null && controller != null) {
        return controller.showAndGetInput(owner, descriptor);
      } else {
        log.error(
            "Cannot create NMAP service editor window. Content: {}, Controller: {}",
            content,
            controller);
        commonResources.fatalError(
            localMessage(
                "ERR_FAILED_TO_CREATE_SERVICE_EDITOR_WINDOW", this.getClass().getSimpleName()));
      }
    } catch (final IOException e) {
      log.error("Cannot load NMAP service editor window definition", e);
      commonResources.fatalError(
          localMessage(
              "ERR_FAILED_LOAD_WINDOW_DEFINITION",
              this.getClass().getSimpleName(),
              EDITOR_FXML,
              e.getLocalizedMessage()));
    }
    return Optional.empty();
  }
}
