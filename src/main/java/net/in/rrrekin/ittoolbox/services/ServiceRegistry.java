package net.in.rrrekin.ittoolbox.services;

import java.util.stream.Stream;
import lombok.NonNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public class ServiceRegistry {
  public Stream<ServiceDefinition> stream() {
    return Stream.empty();
  }

  public void configureService(final @NonNull String serviceId, final @NonNull String serviceOptions) {

  }
}
