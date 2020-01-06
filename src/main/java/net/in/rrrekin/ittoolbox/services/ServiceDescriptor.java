package net.in.rrrekin.ittoolbox.services;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author michal.rudewicz@gmail.com */
public class ServiceDescriptor {

  @NonNls private static final String SEPARATOR = ":";
  private static final @NotNull Gson gson = new Gson();

  private final @NotNull ServiceType type;
  private final @NotNull List<ServiceProperty> properties;
  private final @NotNull Map<String, ServiceProperty> nameToProperty;

  public ServiceDescriptor(
      @NotNull final ServiceType type, @Nullable final List<ServiceProperty> properties) {
    this.type = requireNonNull(type, "type must not be null");
    this.properties =
        Collections.unmodifiableList(properties == null ? List.of() : List.copyOf(properties));
    this.nameToProperty =
        this.properties.stream()
            .collect(Collectors.toMap(ServiceProperty::getName, Function.identity()));
  }

  public ServiceDescriptor(final @NotNull String description) {
    final String[] parts =
        requireNonNull(description, "description must be not null").split(SEPARATOR, 2);
    checkArgument(parts.length == 2, localMessage("ERR_INVALID_SERVICE_DESCRIPTOR"));
    type = ServiceType.valueOf(parts[0]);
    final List<ServiceProperty> serviceProperties =
        gson.fromJson(parts[1], new TypeToken<ArrayList<ServiceProperty>>() {}.getType());
    this.properties =
        Collections.unmodifiableList(serviceProperties == null ? List.of() : serviceProperties);
    this.nameToProperty =
        this.properties.stream()
            .collect(Collectors.toMap(ServiceProperty::getName, Function.identity()));
  }

  @NotNull
  public ServiceType getType() {
    return type;
  }

  public @NotNull List<ServiceProperty> getProperties() {
    return properties;
  }

  public @Nullable ServiceProperty getProperty(final @Nullable String name) {
    if (name == null) {
      return null;
    }
    return nameToProperty.get(name);
  }

  @Override
  public String toString() {
    return type.name() + SEPARATOR + gson.toJson(properties);
  }
}
