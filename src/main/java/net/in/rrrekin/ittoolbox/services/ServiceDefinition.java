package net.in.rrrekin.ittoolbox.services;

import org.jetbrains.annotations.NotNull;

/**
 * Interface of service definition.
 *
 * @param <T> the type parameter
 * @author michal.rudewicz @gmail.com
 */
public interface ServiceDefinition {
  /**
   * Gets service name.
   *
   * @return the name
   */
  String getName();

  /**
   * Gets service id.
   *
   * @return the id
   */
  @NotNull String getId();

//  /**
//   * Gets service type.
//   *
//   * @return the type
//   */
//  ServiceType getType();
//
//  /**
//   * Execute service synchronously.
//   *
//   * @param node the node
//   * @return mixed output of stdout and stderr. Stdout lines begins with space, stderr lines starts
//   *     with exclamation mark.
//   */
//  String executeSync(T node);
//
//  /**
//   * Execute asynchronous service.
//   *
//   * @param node the node
//   * @return the asynchronous process holder
//   */
//  AsyncExecution executeAsync(T node);
//
//  /**
//   * Start background service, detached from the application process.
//   *
//   * @param node the node
//   */
//  void startInBackground(T node);
//
  /**
   * Gets service configuration serialized to string. It can be service specific implementation.
   * For simple services it can be just parameter or parameters concatenated with simple separator.
   * For more complex cases it can be an object serialized to JSON or other format.
   *
   * @return the dto properties
   */
  @NotNull
  String getConfiguration();
}
