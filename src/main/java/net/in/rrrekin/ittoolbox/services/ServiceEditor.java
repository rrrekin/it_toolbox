package net.in.rrrekin.ittoolbox.services;

import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author michal.rudewicz@gmail.com
 */
public interface ServiceEditor {



  void openEditor(@Nullable Stage owner, @NotNull ServiceDescriptor descriptor);

}
