package net.in.rrrekin.ittoolbox.configuration;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Map;
import javafx.scene.control.TreeItem;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Object representing application configuration.
 *
 * @author michal.rudewicz @gmail.com
 */
public class Configuration {

  private final @NotNull TreeItem<NetworkNode> networkNodes;
  private final @NotNull Map<String, Map<String, String>> modules;
  private @Nullable Path filePath = null;

  /**
   * Instantiates a new Configuration.
   *
   * @param networkNodes the network nodes
   * @param modules the modules
   */
  public Configuration(
      final @NotNull TreeItem<NetworkNode> networkNodes,
      final @NotNull Map<String, Map<String, String>> modules) {
    this.networkNodes = requireNonNull(networkNodes, "NetworkNodes must not be null");
    this.modules = requireNonNull(modules, "Modules must not be null");
  }

  /**
   * Gets network nodes.
   *
   * @return the network nodes
   */
  public @NotNull TreeItem<NetworkNode> getNetworkNodes() {
    return this.networkNodes;
  }

  /**
   * Gets modules.
   *
   * @return the modules
   */
  public @NotNull Map<String, Map<String, String>> getModules() {
    return this.modules;
  }

  /**
   * Gets file path. Null means that config is not bound to any file.
   *
   * @return the file path
   */
  public @Nullable Path getFilePath() {
    return filePath;
  }

  /**
   * Sets file path. Null means that config is not bound to any file.
   *
   * @param filePath the file path
   */
  public void setFilePath(final @Nullable Path filePath) {
    this.filePath = filePath;
  }
}
