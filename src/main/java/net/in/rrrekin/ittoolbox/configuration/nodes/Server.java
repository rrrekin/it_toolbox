package net.in.rrrekin.ittoolbox.configuration.nodes;

import static java.util.Objects.requireNonNullElse;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.paint.Color;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import org.controlsfx.glyphfont.FontAwesome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a standard server with its own address.
 *
 * @author michal.rudewicz @gmail.com
 */
public class Server implements NetworkNode {

  private static final @NotNull IconDescriptor DEFAULT_ICON_DESCRIPTOR =
      new IconDescriptor(FontAwesome.Glyph.SERVER, Color.BLUE, true);

  private final @NotNull String name;
  private final @NotNull String address;
  private final @NotNull String description;
  private final @NotNull IconDescriptor iconDescriptor;
  private final @NotNull ImmutableMap<String, String> properties;
  private final @NotNull ImmutableList<String> serviceDescriptors;

  public Server(
      @Nullable final String name,
      @Nullable final String address,
      @Nullable final String description,
      @Nullable final IconDescriptor iconDescriptor,
      @Nullable final Map<String, String> properties,
      @Nullable final List<String> serviceDescriptors) {
    this.name = requireNonNullElse(name, localMessage("NODE_SERVER_DEFAULT_NAME"));
    this.address = requireNonNullElse(address, this.name);
    this.description = requireNonNullElse(description, this.name);
    this.iconDescriptor = requireNonNullElse(iconDescriptor, DEFAULT_ICON_DESCRIPTOR);
    this.properties = ImmutableMap.copyOf(requireNonNullElse(properties, ImmutableMap.of()));
    this.serviceDescriptors =
      ImmutableList.copyOf(requireNonNullElse(serviceDescriptors, ImmutableList.of()));
  }

  /**
   * Instantiates a new Server for given address.
   *
   * @param name the name/address of the server
   */
  public Server(final @Nullable String name) {
    this.name = requireNonNullElse(name, localMessage("NODE_SERVER_DEFAULT_NAME"));
    this.address = this.name;
    this.description = this.name;
    properties = ImmutableMap.of();
    serviceDescriptors = ImmutableList.of();
    iconDescriptor = DEFAULT_ICON_DESCRIPTOR;
  }

  /**
   * Instantiates a new Server.
   */
  public Server() {
    this(null);
  }

  @Override
  public NodeType getType() {
    return NodeType.SERVER;
  }

  @Override
  public String getLocalNodeTypeName() {
    return localMessage("NODE_SERVER");
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull String getDescription() {
    return description;
  }

  @Override
  public @NotNull IconDescriptor getIconDescriptor() {
    return iconDescriptor;
  }

  @Override
  public @NotNull ImmutableList<String> getServiceDescriptors() {
    return serviceDescriptors;
  }

  @Override
  public @NotNull ImmutableMap<String, String> getProperties() {
    return properties;
  }

  public @NotNull String getAddress() {
    return address;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("address", address)
      .add("description", description)
      .add("iconDescriptor", iconDescriptor)
      .add("properties", properties)
      .add("serviceDescriptors", serviceDescriptors)
      .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Server server = (Server) o;
    return name.equals(server.name) &&
      address.equals(server.address) &&
      description.equals(server.description) &&
      iconDescriptor.equals(server.iconDescriptor) &&
      properties.equals(server.properties) &&
      serviceDescriptors.equals(server.serviceDescriptors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, address, description, iconDescriptor, properties, serviceDescriptors);
  }
}
