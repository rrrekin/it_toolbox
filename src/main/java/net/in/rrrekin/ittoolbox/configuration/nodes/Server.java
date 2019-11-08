package net.in.rrrekin.ittoolbox.configuration.nodes;

import static com.google.common.base.Preconditions.checkArgument;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.enMessage;
import static net.in.rrrekin.ittoolbox.utilities.StringUtils.toStringOrEmpty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of a standard server with its own address.
 *
 * @author michal.rudewicz @gmail.com
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Server implements NetworkNode {

  @Getter @Setter private @NonNull String name;
  @Getter @Setter private @NonNull String address;
  @Getter @Setter private @NonNull String description;
  @Getter private final @NonNull Map<String, String> properties;
  @Getter private final @NonNull List<String> serviceDescriptors;

  /**
   * Instantiates a new Server for given address.
   *
   * @param address the address
   */
  public Server(final @NonNull String address) {
    this.name = address;
    this.address = address;
    this.description = address;
    properties = Maps.newHashMap();
    serviceDescriptors = Lists.newArrayList();
  }

  /**
   * Instantiates a new Server.
   *
   * @param dto the dto
   */
  public Server(final @NonNull Map<String, Object> dto) {
    final String type = toStringOrEmpty(dto.get(TYPE_PROPERTY));
    checkArgument(
        NodeType.SERVER.getTypeName().equalsIgnoreCase(type),
        enMessage("NODE_CONSTRUCTOR_TYPE_MISMATCH"),
        type);
    name = toStringOrEmpty(dto.get(NAME_PROPERTY));
    address = toStringOrEmpty(dto.get(ADDRESS_PROEPRTY));
    description = toStringOrEmpty(dto.get(DESCRIPTION_PROPERTY));
    properties = Maps.newHashMap();
    serviceDescriptors = Lists.newArrayList();
    if (dto.get(SERVICES_PROPERTY) instanceof List) {
      ((List<?>) dto.get(SERVICES_PROPERTY))
          .forEach(it -> serviceDescriptors.add(toStringOrEmpty(it)));
    }
    for (final Map.Entry<String, Object> entry : dto.entrySet()) {
      final String property = entry.getKey();
      final Object value = entry.getValue();
      if (property != null && property.startsWith(PROPERTIES_PREFIX)) {
        final String propertyName = property.substring(PROPERTIES_PREFIX.length());
        properties.put(propertyName, toStringOrEmpty(value));
      }
    }
  }

  @Override
  public @NotNull ImageIcon getIcon() {
    return new ImageIcon();
  }

  @Override
  public @NotNull Map<String, Object> getDtoProperties() {

    final Map<String, Object> response = Maps.newLinkedHashMap();
    response.put(TYPE_PROPERTY, NodeType.SERVER.getTypeName());
    response.put(NAME_PROPERTY, name);
    response.put(ADDRESS_PROEPRTY, address);
    response.put(DESCRIPTION_PROPERTY, description);
    response.put(SERVICES_PROPERTY, serviceDescriptors);
    properties.forEach((property, value) -> response.put(PROPERTIES_PREFIX + property, value));
    return response;
  }

  /**
   * Data copy object.
   *
   * @return the object
   */
  public Server.Data immutableDataCopy() {
    return new Server.Data(name, address, description, ImmutableMap.copyOf(properties));
  }

  /** The type Data - DTO for access in templating. */
  @Value
  public static class Data {
    private @NonNull String name;
    private @NonNull String address;
    private @NonNull String description;
    private final @NonNull Map<String, String> properties;
  }
}
