package net.in.rrrekin.ittoolbox.services;

import java.util.Map;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import org.jetbrains.annotations.NotNull;

/**
 * Interface of service definition.
 *
 * @author michal.rudewicz @gmail.com
 */
public interface ServiceDefinition {
  /**
   * Gets service descriptive raw name. Should be evaluated against configuration and node before
   * usage.
   *
   * @return the name
   */
  @NotNull
  String getName(@NotNull ServiceDescriptor descriptor);

  @NotNull
  ServiceType getType();

  @NotNull
  ServiceDescriptor getDefaultDescriptor();

  @NotNull
  ServiceExecutor getExecutor(
      @NotNull ServiceDescriptor descriptor);

  boolean hasEditor();

  @NotNull
  ServiceEditor getEditor();

  boolean isApplicableTo(@NotNull NodeType type);

  boolean isDefaultFor(@NotNull NodeType type);

  @Deprecated
  default @NotNull Map<String, ?> getConfiguration() {
    return Map.of();
  }
}
