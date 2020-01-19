package net.in.rrrekin.ittoolbox.services.definitions;

import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.utilities.LocaleUtil.localMessage;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeType;
import net.in.rrrekin.ittoolbox.gui.services.CommonResources;
import net.in.rrrekin.ittoolbox.os.OsCommandExecutor;
import net.in.rrrekin.ittoolbox.services.PropertyType;
import net.in.rrrekin.ittoolbox.services.ServiceDefinition;
import net.in.rrrekin.ittoolbox.services.ServiceDescriptor;
import net.in.rrrekin.ittoolbox.services.ServiceEditor;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.ServiceProperty;
import net.in.rrrekin.ittoolbox.services.ServiceType;
import net.in.rrrekin.ittoolbox.services.editors.NmapServiceEditor;
import net.in.rrrekin.ittoolbox.services.executors.NmapExecutor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Service for nmap scanning.
 *
 * @author michal.rudewicz @gmail.com
 */
public class NmapService implements ServiceDefinition {

  /** The constant OPTIONS. */
  public static final String OPTIONS = "options";
  /** The constant NAME. */
  public static final String NAME = "name";

  private final @NotNull CommonResources commonResources;
  private final @NotNull OsCommandExecutor osCommandExecutor;
  private final @NotNull AppPreferences appPreferences;

  /**
   * Instantiates a new NmapService.
   *
   * @param commonResources the CommonResources instance to use
   * @param osCommandExecutor the OsCommandExecutor instance to use
   * @param appPreferences the AppPreferences instance to use
   */
  public NmapService(
      final @NotNull CommonResources commonResources,
      final @NotNull OsCommandExecutor osCommandExecutor,
      final @NotNull AppPreferences appPreferences) {
    this.commonResources = requireNonNull(commonResources, "commonResources must be not null");
    this.osCommandExecutor =
        requireNonNull(osCommandExecutor, "osCommandExecutor must be not null");
    this.appPreferences = requireNonNull(appPreferences, "appPreferences must be not null");
  }

  @NonNls
  @Override
  public @NotNull String getName(final @NotNull ServiceDescriptor descriptor) {
    final ServiceProperty name = descriptor.getProperty(NAME);
    final ServiceProperty options = descriptor.getProperty(OPTIONS);
    if (name == null || name.getRawValue().trim().isEmpty()) {
      return localMessage("SERVICE_NMAP");
    }
    return name.getRawValue()
        + (options != null && !options.getRawValue().isBlank()
            ? " (" + options.getRawValue() + ")"
            : "");
  }

  @Override
  public @NotNull ServiceType getType() {
    return ServiceType.NMAP;
  }

  @Override
  public @NotNull ServiceDescriptor getDefaultDescriptor() {
    return new ServiceDescriptor(
        ServiceType.NMAP,
        List.of(
            new ServiceProperty(NAME, "", false, PropertyType.STRING),
            new ServiceProperty(OPTIONS, "", false, PropertyType.STRING)));
  }

  @Override
  public @NotNull ServiceExecutor getExecutor(final @NotNull ServiceDescriptor descriptor) {
    return new NmapExecutor(descriptor, osCommandExecutor, appPreferences);
  }

  @Override
  public boolean hasEditor() {
    return true;
  }

  @Override
  public @NotNull ServiceEditor getEditor() {
    return new NmapServiceEditor(commonResources);
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
    return MoreObjects.toStringHelper(this).toString();
  }
}
