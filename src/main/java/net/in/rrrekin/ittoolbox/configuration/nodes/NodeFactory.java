package net.in.rrrekin.ittoolbox.configuration.nodes;

import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;

/**
 * Factory of {@link NetworkNode} object based on a generic Map DTO.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class NodeFactory {
  /**
   * Create network node from Map DTO.
   *
   * @param dto the DTO
   * @return the network node
   * @throws InvalidConfigurationException the invalid configuration exception
   */
  public NetworkNode createFrom(final @NonNull Map<String, Object> dto)
      throws InvalidConfigurationException {
    final String typeName = toStringOrEmpty(dto.get(NetworkNode.TYPE_PROPERTY));
    final NodeType type = NodeType.of(typeName);
    if (type == null) {
      throw new InvalidConfigurationException("CANNOT_CREATE", typeName);
    }
    Constructor<? extends NetworkNode> constructor = null;

    try {
      constructor = type.getClazz().getDeclaredConstructor(Map.class, NodeFactory.class);
    } catch (final NoSuchMethodException | SecurityException ignored) {
      // Try other constructors
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(dto, this);
      } catch (final Exception e) {
        log.error("Failed to create {} with 2 argument constructor", type, e);
        throw new InvalidConfigurationException("FAILED_2_ARG_CONSTRUCTOR", e, type.getTypeName());
      }
    }

    try {
      constructor = type.getClazz().getDeclaredConstructor(Map.class);
    } catch (final NoSuchMethodException | SecurityException ignored) {
      // Try other constructors
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(dto);
      } catch (final Exception e) {
        log.error("Failed to create {} with 1 argument constructor", type, e);
        throw new InvalidConfigurationException("FAILED_1_ARG_CONSTRUCTOR", e, type.getTypeName());
      }
    }

    try {
      constructor = type.getClazz().getDeclaredConstructor();
    } catch (final NoSuchMethodException | SecurityException ignored) {
      // Try other constructors
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(dto);
      } catch (final Exception e) {
        log.error("Failed to create {} with no argument constructor", type, e);
        throw new InvalidConfigurationException("UNABLE_CREATE_NODE", e, type);
      }
    }

    log.error("Unable to find constructor for type {} with {}.", type, dto);
    throw new InvalidConfigurationException("CANNOT_FIND_CONSTRUCTOR_FOR", type.getTypeName());
  }

  /**
   * Create list of NetworkNode elements from list of generic Map DTOs..
   *
   * @param dtoList the DTO list
   * @return the NetworkNode list
   */
  public List<NetworkNode> createFrom(final List<?> dtoList) {
    final List<NetworkNode> response = Lists.newArrayList();
    for (final Object child : dtoList) {
      if (child instanceof Map) {
        final Map<?, ?> childMap = (Map) child;
        final Map<String, Object> propertyMap = Maps.newLinkedHashMap();
        childMap.forEach((property, value) -> propertyMap.put(toStringOrEmpty(property), value));
        try {
          response.add(createFrom(propertyMap));
        } catch (final InvalidConfigurationException e) {
          log.info("{} / Skipping invalid element: {}, ", e.getMessage(), child);
        }
      } else {
        log.warn("Invalid object on element DTO list {}.", child);
      }
    }
    return response;
  }
}
