package net.in.rrrekin.ittoolbox.services.definitions;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.ServiceProperty;
import net.in.rrrekin.ittoolbox.services.PropertyType;
import net.in.rrrekin.ittoolbox.services.ServiceType;
import net.in.rrrekin.ittoolbox.services.editors.EditorPlaceholder;
import net.in.rrrekin.ittoolbox.services.executors.NoOpExecutor;
import org.jetbrains.annotations.NotNull;

/** @author michal.rudewicz@gmail.com */
public class SshService implements ServiceDefinition {

  private final @NotNull CommonResources commonResources;

  public SshService(final @NotNull CommonResources commonResources) {
    this.commonResources = requireNonNull(commonResources, "commonResources must be not null");
  }

  @Override
  public @NotNull String getName(final @NotNull ServiceDescriptor descriptor) {
    final ServiceProperty name = descriptor.getProperty("name");
    return name == null || name.getRawValue().trim().isEmpty()
        ? localMessage("SERVICE_SSH")
        : name.getRawValue();
  }

  @Override
  public @NotNull ServiceType getType() {
    return ServiceType.SSH;
  }

  @Override
  public @NotNull ServiceDescriptor getDefaultDescriptor() {
    return new ServiceDescriptor(
        ServiceType.SSH,
        List.of(
          new ServiceProperty("name", "", false, PropertyType.STRING),
          new ServiceProperty("port", "", false, PropertyType.INT),
          new ServiceProperty("credentials", "", false, PropertyType.STRING),
          new ServiceProperty("options", "", false, PropertyType.STRING)));
  }

  @Override
  public @NotNull ServiceExecutor getExecutor(
      final @NotNull ServiceDescriptor descriptor, final @NotNull Configuration configuration) {
    return new NoOpExecutor(commonResources);
  }

  @Override
  public boolean hasEditor() {
    return true;
  }

  @Override
  public @NotNull ServiceEditor getEditor() {
    return new EditorPlaceholder(commonResources);
  }

  @Override
  public boolean isApplicableTo(@NotNull final NodeType type) {
    return type == NodeType.SERVER;
  }

  @Override
  public boolean isDefaultFor(@NotNull final NodeType type) {
    return false;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .toString();
  }

}
