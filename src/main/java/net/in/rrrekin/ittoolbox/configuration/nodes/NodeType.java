package net.in.rrrekin.ittoolbox.configuration.nodes;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enum defining possible configuration file objects and provides method to create them from
 * configuration data.
 *
 * @author michal.rudewicz @gmail.com
 */
public enum NodeType {
  /** Server group configuration node. */
  GROUP("Group"),
  /** Server configuration node. */
  SERVER("Server"),
  /** Generic node configuration node. */
  GENERIC_NODE("GenericNode");

  /**
   * Gets name of node type used in YAML serialization.
   *
   * @return name of node type used in YAML serialization
   */
  public @NotNull String getTypeName() {
    return typeName;
  }

  private final @NotNull String typeName;
  private final @NotNull String lcTypeName;

  NodeType(final @NotNull String typeName) {
    this.typeName = typeName;
    this.lcTypeName = typeName.toLowerCase();
  }

  /**
   * Get enum for given configuration node.
   *
   * @param name the configuration key
   * @return the config enum
   */
  public static @Nullable NodeType of(final @Nullable String name) {
    if (name != null) {
      final String lcName = name.toLowerCase(Locale.ENGLISH);
      for (final NodeType type : values()) {
        if (type.lcTypeName.equals(lcName)) {
          return type;
        }
      }
    }
    return null;
  }
}
