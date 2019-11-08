package net.in.rrrekin.ittoolbox.configuration.nodes;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enum defining possible configuration file objects and their mappings to configuration classes.
 *
 * @author michal.rudewicz @gmail.com
 */
public enum NodeType {
  /** Server group configuration node. */
  GROUP("Group", GroupingNode.class),
  /** Server configuration node. */
  SERVER("Server", Server.class),
  /** Generic node configuration node. */
  GENERIC_NODE("GenericNode", GenericNode.class);

  @Getter private final @NotNull String typeName;
  @Getter private final @NotNull Class<? extends NetworkNode> clazz;

  private static final Map<String, NodeType> typeNameMapping = Maps.newHashMap();
  private static final Map<Class<? extends NetworkNode>, NodeType> classMapping = Maps.newHashMap();

  static {
    for (final NodeType it : NodeType.values()) {
      typeNameMapping.put(it.getTypeName().toLowerCase(Locale.ENGLISH), it);
      classMapping.put(it.getClazz(), it);
    }
  }

  @Contract(pure = true)
  NodeType(final @NotNull String typeName, final @NotNull Class<? extends NetworkNode> clazz) {
    this.typeName = typeName;
    this.clazz = clazz;
  }

  /**
   * Get enum for given configuration node.
   *
   * @param name the configuration key
   * @return the config enum
   */
  public static @Nullable NodeType of(final @Nullable String name) {
    return typeNameMapping.get(Strings.nullToEmpty(name).toLowerCase(Locale.ENGLISH));
  }

  /**
   * Gwt enum for given class.
   *
   * @param clazz the object class
   * @return the config enum
   */
  public static @Nullable NodeType of(final @Nullable Class<?> clazz) {
    return classMapping.get(clazz);
  }
}
