package net.in.rrrekin.ittoolbox.services;

import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.services.exceptions.ServiceExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author michal.rudewicz@gmail.com
 */
public interface ServiceExecutor {

  void execute(@Nullable Stage stage, @NotNull NetworkNode node) throws ServiceExecutionException;

}
