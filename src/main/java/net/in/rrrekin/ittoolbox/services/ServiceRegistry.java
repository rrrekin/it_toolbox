package net.in.rrrekin.ittoolbox.services;

import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public class ServiceRegistry {
  public Stream<ServiceDefinition> stream() {
    return Stream.empty();
  }

  public void configureService(final @NotNull String serviceId, final @NotNull String serviceOptions) {

  }
}
