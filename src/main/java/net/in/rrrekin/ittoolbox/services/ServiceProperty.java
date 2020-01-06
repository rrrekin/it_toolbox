package net.in.rrrekin.ittoolbox.services;

import com.google.common.base.MoreObjects;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/** @author michal.rudewicz@gmail.com */
@NonNls
public class ServiceProperty {

  private @NotNull String name;
  private @NotNull String rawValue;
  private boolean evaluate;
  private @NotNull PropertyType type;

  public ServiceProperty(
      final @NotNull String name,
      final @NotNull String rawValue,
      final boolean evaluate,
      final @NotNull PropertyType type) {
    this.name = name;
    this.rawValue = rawValue;
    this.evaluate = evaluate;
    this.type = PropertyType.valueOf(String.valueOf(type));
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull final String name) {
    this.name = name;
  }

  @NotNull
  public String getRawValue() {
    return rawValue;
  }

  public void setRawValue(@NotNull final String rawValue) {
    this.rawValue = rawValue;
  }

  public boolean isEvaluate() {
    return evaluate;
  }

  public void setEvaluate(final boolean evaluate) {
    this.evaluate = evaluate;
  }

  @NotNull
  public PropertyType getType() {
    return type;
  }

  public void setType(final @NotNull PropertyType type) {
    this.type = type;
  }

  public @NotNull Object getValueFor(
      final @NotNull Configuration configuration, final @NotNull NetworkNode node) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("rawValue", rawValue)
        .add("evaluate", evaluate)
        .add("type", type)
        .toString();
  }
}
