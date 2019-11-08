package net.in.rrrekin.ittoolbox.configuration.nodes;

import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.CANNOT_CREATE_NETWORK_NODE;
import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_OBJECT_ON_DTO_LIST;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;
import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException;
import net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Factory of {@link NetworkNode} object based on a generic Map DTO.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class NodeFactory {

  private static final int MAX_DESCRIPTION_WIDTH = 40;
  private final @NonNull EventBus eventBus;

  @Inject
  public NodeFactory(final @NonNull EventBus eventBus) {
    log.debug("Creating NodeFactory");
    this.eventBus = eventBus;
  }

  /**
   * Create network node from Map DTO.
   *
   * @param dto the DTO
   * @return the network node
   * @throws InvalidConfigurationException the invalid configuration exception
   */
  public NetworkNode createFrom(
      final @NonNull Map<String, Object> dto, final @NonNull String parentInfo)
      throws InvalidConfigurationException {
    final String typeName = toStringOrEmpty(dto.get(NetworkNode.TYPE_PROPERTY));
    final NodeType type = NodeType.of(typeName);
    if (type == null) {
      throw new InvalidConfigurationException(
          "EX_CANNOT_CREATE",
          StringUtils.isBlank(typeName) ? localMessage("CFG_UNKNOWN_TYPE") : typeName);
    }
    Constructor<? extends NetworkNode> constructor = null;

    try {
      constructor =
          type.getClazz().getDeclaredConstructor(Map.class, NodeFactory.class, String.class);
    } catch (final NoSuchMethodException | SecurityException ignored) {
      // Try other constructors
    }
    if (constructor != null) {
      try {
        return constructor.newInstance(dto, this, parentInfo);
      } catch (final Exception e) {
        log.error("Failed to create {} with 3 argument constructor in {}", type, parentInfo, e);
        throw new InvalidConfigurationException(
            "EX_FAILED_3_ARG_CONSTRUCTOR", e, type.getTypeName());
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
        log.error("Failed to create {} with 1 argument constructor in {}", type, parentInfo, e);
        throw new InvalidConfigurationException(
            "EX_FAILED_1_ARG_CONSTRUCTOR", e, type.getTypeName());
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
        log.error("Failed to create {} with no argument constructor in {}", type, parentInfo, e);
        throw new InvalidConfigurationException("EX_UNABLE_CREATE_NODE", e, type);
      }
    }

    log.error("Unable to find constructor for type {} in {} with {}.", type, parentInfo, dto);
    throw new InvalidConfigurationException("EX_CANNOT_FIND_CONSTRUCTOR_FOR", type.getTypeName());
  }

  /**
   * Create list of NetworkNode elements from list of generic Map DTOs..
   *
   * @param dtoList the DTO list
   * @return the NetworkNode list
   */
  public @NotNull List<NetworkNode> createFrom(
      final @NonNull List<?> dtoList, final @NonNull String parentInfo) {
    final List<NetworkNode> response = Lists.newArrayList();
    for (final Object child : dtoList) {
      if (child instanceof Map) {
        final Map<?, ?> childMap = (Map) child;
        final Map<String, Object> propertyMap = Maps.newLinkedHashMap();
        childMap.forEach((property, value) -> propertyMap.put(toStringOrEmpty(property), value));
        try {
          response.add(createFrom(propertyMap, parentInfo));
        } catch (final InvalidConfigurationException e) {
          log.warn("{} / Skipping invalid element in {}: {}, ", e.getMessage(), parentInfo, child);
          eventBus.post(
              new ConfigurationErrorEvent(
                  CANNOT_CREATE_NETWORK_NODE,
                  localMessage(
                      "CFG_CANNOT_CREATE_NETWORK_NODE",
                      parentInfo,
                      e.getLocalizedMessage(),
                      abbreviate(childMap.toString(), MAX_DESCRIPTION_WIDTH))));
        }
      } else {
        log.warn("Invalid object on element DTO list in {}: {}", parentInfo, child);
        eventBus.post(
            new ConfigurationErrorEvent(
                INVALID_OBJECT_ON_DTO_LIST,
                localMessage(
                    "CFG_INVALID_OBJECT_ON_DTO_LIST",
                    parentInfo,
                    abbreviate(String.valueOf(child), MAX_DESCRIPTION_WIDTH))));
      }
    }
    return response;
  }
}
