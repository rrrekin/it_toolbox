package net.in.rrrekin.ittoolbox.services;

import org.jetbrains.annotations.NotNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public enum PropertyType {
  INT(Long.class),
  DOUBLE(Double.class),
  STRING(String.class),
  BOOLEAN(Boolean.class);

  private final @NotNull Class<?> type;

  PropertyType(final @NotNull Class<?> type) {
    this.type = type;
  }

  public @NotNull Class<?> getType() {
    return type;
  }
}
