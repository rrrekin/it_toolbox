package net.in.rrrekin.ittoolbox.configuration;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import org.jetbrains.annotations.NotNull;

/**
 * Object representing application configuration.
 *
 * @author michal.rudewicz @gmail.com
 */
@AllArgsConstructor
@Slf4j
@EqualsAndHashCode
public class Configuration {

  @Getter private final @NotNull List<NetworkNode> networkNodes;
  @Getter private final @NotNull Map<String, Map<String, String>> modules;
}
