package net.in.rrrekin.ittoolbox.configuration.nodes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of Network node representation.
 *
 * @author michal.rudewicz @gmail.com
 */
public interface NetworkNode {

  /** The constant TYPE key in configuration DTO. */
  String TYPE_PROPERTY = "type";

  /** The constant NAME key in configuration DTO. */
  String NAME_PROPERTY = "name";

  /** The constant ADDRESS key in configuration DTO. */
  String ADDRESS_PROEPRTY = "address";

  /** The constant DESCRIPTION key in configuration DTO. */
  String DESCRIPTION_PROPERTY = "description";

  /** The constant CHILD_NODES key in configuration DTO. */
  String CHILD_NODES_PROPERTY = "children";

  /** The constant SERVICES_PROPERTY key in configuration DTO. */
  String SERVICES_PROPERTY = "services";

  /** The constant PROPERTIES_PREFIX for custom properties in configuration DTO. */
  String PROPERTIES_PREFIX = "_";

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
  @NotNull String getDescription();

  /**
   * Gets icon.
   *
   * @return the icon
   */
  @NotNull
  ImageIcon getIcon();

  /**
   * True if given node cannot have children.
   *
   * @return true if given node cannot have children
   */
  default boolean isLeaf() {
    return true;
  }

  /**
   * Gets child nodes.
   *
   * @return the child nodes
   */
  default @NotNull List<NetworkNode> getChildNodes() {
    return Collections.emptyList();
  }

  /**
   * Gets network node properties. Null for nodes that di not support custom properties.
   *
   * @return the properties
   */
  @Nullable
  default Map<String, String> getProperties() {
    return null;
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
  Collection<String> getServiceDescriptors();

  /**
   * Gets dto properties.
   *
   * @return the dto properties
   */
  @NotNull
  Map<String, Object> getDtoProperties();
}
