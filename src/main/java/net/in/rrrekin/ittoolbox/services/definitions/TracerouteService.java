package net.in.rrrekin.ittoolbox.services.definitions;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.ServiceType;
import net.in.rrrekin.ittoolbox.services.executors.NoOpExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * @author michal.rudewicz@gmail.com
 */
public class TracerouteService implements ServiceDefinition {

  private final @NotNull CommonResources commonResources;

  public TracerouteService(final @NotNull CommonResources commonResources) {
    this.commonResources = requireNonNull(commonResources, "commonResources must be not null");
  }

  @NotNull
  @Override
  public String getName(final @NotNull ServiceDescriptor descriptor) {
    return localMessage("SERVICE_TRACEROUTE");
  }

  @Override
  public @NotNull ServiceType getType() {
    return ServiceType.TRACEROUTE;
  }

  @Override
  public @NotNull ServiceDescriptor getDefaultDescriptor() {
    return new ServiceDescriptor(ServiceType.TRACEROUTE, null);
  }

  @NotNull
  @Override
  public ServiceExecutor getExecutor(@NotNull final ServiceDescriptor descriptor, final @NotNull Configuration configuration) {
    return new NoOpExecutor(commonResources);
  }

  @Override
  public boolean hasEditor() {
    return false;
  }

  @NotNull
  @Override
  public ServiceEditor getEditor() {
    throw new IllegalStateException("Editor not available for TracerouteService.");
  }

  @Override
  public boolean isApplicableTo(@NotNull final NodeType type) {
    return type == NodeType.SERVER;
  }

  @Override
  public boolean isDefaultFor(@NotNull final NodeType type) {
    return type == NodeType.SERVER;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .toString();
  }

}
