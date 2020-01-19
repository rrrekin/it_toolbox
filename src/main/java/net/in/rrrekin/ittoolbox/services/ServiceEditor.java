package net.in.rrrekin.ittoolbox.services;

import java.util.Optional;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The interface ServiceEditor.
 *
 * @author michal.rudewicz @gmail.com
 */
public interface ServiceEditor {

  /**
   * Open service editor.
   *
   * @param owner the owner stage
   * @param descriptor the current service descriptor
   * @param node the network node owning that service
   * @return the edited service descriptor or null when edit was cancelled
   */
  @NotNull
  Optional<ServiceDescriptor> openEditorAndGetDefinition(
      @Nullable Stage owner, @NotNull ServiceDescriptor descriptor, @NotNull NetworkNode node);
}
