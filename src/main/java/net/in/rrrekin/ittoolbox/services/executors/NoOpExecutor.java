package net.in.rrrekin.ittoolbox.services.executors;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Temporary no-op executor.
 *
 * @author michal.rudewicz@gmail.com */
public class NoOpExecutor implements ServiceExecutor {

  private final CommonResources commonResources;

  @Inject
  public NoOpExecutor(final CommonResources commonResources) {
    this.commonResources = commonResources;
  }

  @Override
  public void execute(@Nullable final Stage stage, @NotNull final NetworkNode node) {
    requireNonNull(node, "node must be not null");
    commonResources.infoDialog(stage, "NoOpExecutor", "Service execution not implemented yet.");
  }
}
