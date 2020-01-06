package net.in.rrrekin.ittoolbox.services.editors;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import javafx.stage.Stage;
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
  public void openEditor(@Nullable final Stage owner, @NotNull final ServiceDescriptor descriptor) {
    commonResources.infoDialog(owner, "Editor placeholder", "Temporary editor placeholder");
  }
}
