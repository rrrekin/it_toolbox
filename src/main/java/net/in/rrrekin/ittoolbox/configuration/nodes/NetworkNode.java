package net.in.rrrekin.ittoolbox.configuration.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of Network node representation.
 *
 * @author michal.rudewicz @gmail.com
 */
public interface NetworkNode extends Serializable {

  /**
   * Gets node type.
   *
   * @return the type
   */
  @NotNull NodeType getType();

  /**
   * @return local name of given node type
   */
  String getLocalNodeTypeName();
  /**
   * Gets name.
   *
   * @return the name
   */
  @NotNull
  String getName();

  /**
   * Gets description.
   *
   * @return the description
   */
  @NotNull
  String getDescription();

  /**
   * Gets icon descriptor.
   *
   * @return the icon descriptor
   */
  @NotNull
  IconDescriptor getIconDescriptor();

  /**
   * True if given node cannot have children.
   *
   * @return true if given node cannot have children
   */
  default boolean isLeaf() {
    return true;
  }

  /**
   * Gets network node properties. Null for nodes that di not support custom properties.
   *
   * @return the properties
   */
  default @NotNull ImmutableMap<String, String> getProperties() {
    return ImmutableMap.of();
  }

  /**
   * Gets service descriptors. A service descriptor is a string consisting of service id (e.g. ssh,
   * http, https) optionally followed by semicolon and a string that defines options specific for
   * given service (e.g. "ssh:port=2022:user=johndoe",
   * "https:port=8443:basic-credentials=johndoe@work:truststore=workCA:keystore=jonhdoe1"). TODO:
   * Add real examples when implemented.
   *
   * @return the service descriptors
   */
  @NotNull
  ImmutableList<ServiceDescriptor> getServiceDescriptors();
}
