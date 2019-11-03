package net.in.rrrekin.ittoolbox.configuration;

import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public class ConfigurationManager {

  public @NotNull File getConfigFile() {
    return new File("config.yml");
  }

}
