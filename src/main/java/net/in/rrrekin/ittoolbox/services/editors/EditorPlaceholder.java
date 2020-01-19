package net.in.rrrekin.ittoolbox.services.editors;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import java.util.Optional;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author michal.rudewicz@gmail.com */
public class EditorPlaceholder implements ServiceEditor {

  private final @NotNull CommonResources commonResources;

  @Inject
  public EditorPlaceholder(final @NotNull CommonResources commonResources) {
    this.commonResources = requireNonNull(commonResources, "commonResources must be not null");
  }

  @Override
  public Optional<ServiceDescriptor> openEditorAndGetDefinition(
      @Nullable final Stage owner,
      @NotNull final ServiceDescriptor descriptor,
      @NotNull NetworkNode node) {
    commonResources.infoDialog(owner, "Editor placeholder", "Temporary editor placeholder");
    return Optional.of(descriptor);
  }
}
