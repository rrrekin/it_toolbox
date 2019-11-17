package net.in.rrrekin.ittoolbox.configuration.nodes;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
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
  GROUP("Group") {
    @Override
    @NotNull
    NetworkNode create(
        final Map<String, Object> dto,
        final String parentInfo,
        final NodeFactory nodeFactory) {
      return new GroupingNode(dto, nodeFactory, parentInfo);
    }
  },
  /** Server configuration node. */
  SERVER("Server") {
    @Override
    @NotNull
    NetworkNode create(
        final Map<String, Object> dto,
        final String parentInfo,
        final NodeFactory nodeFactory) {
      return new Server(dto);
    }
  },
  /** Generic node configuration node. */
  GENERIC_NODE("GenericNode") {
    @Override
    @NotNull
    NetworkNode create(
        final Map<String, Object> dto,
        final String parentInfo,
        final NodeFactory nodeFactory) {
      return new GenericNode(dto);
    }
  };

  @Getter private final @NotNull String typeName;

  private static final Map<String, NodeType> typeNameMapping = Maps.newHashMap();

  static {
    for (final NodeType it : NodeType.values()) {
      typeNameMapping.put(it.getTypeName().toLowerCase(Locale.ENGLISH), it);
    }
  }

  @Contract(pure = true)
  NodeType(final @NotNull String typeName) {
    this.typeName = typeName;
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
   * Create a new {@link NetworkNode} of given type.
   *
   * @param dto the dto with node data
   * @param parentInfo the parent info (path in hierarchy)
   * @param nodeFactory the node factory (to create possible child nodes)
   * @return the network node
   */
  abstract @NotNull NetworkNode create(
      final @NonNull Map<String, Object> dto,
      final @NonNull String parentInfo,
      final @NonNull NodeFactory nodeFactory);
}
