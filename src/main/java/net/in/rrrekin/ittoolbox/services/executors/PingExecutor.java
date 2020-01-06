package net.in.rrrekin.ittoolbox.services.executors;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import java.util.Map;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferences;
import net.in.rrrekin.ittoolbox.infrastructure.UserPreferencesFactory;
import net.in.rrrekin.ittoolbox.os.OsServices;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.exceptions.ServiceExecutionException;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author michal.rudewicz@gmail.com */
public class PingExecutor implements ServiceExecutor {

  @NonNls private static final Logger log = LoggerFactory.getLogger(PingExecutor.class);

  private static final String PREF_COMMAND = "command";
  private static final String PREF_OPTIONS = "options";

  private final @NotNull OsServices osServices;
  private final @NotNull UserPreferences userPreferences;

  public PingExecutor(
      @NotNull final OsServices osServices,
      final @NotNull UserPreferencesFactory userPreferencesFactory) {
    this.osServices = requireNonNull(osServices, "osServices must not be null");
    this.userPreferences =
        requireNonNull(userPreferencesFactory, "userPreferencesFactory must not be null")
            .create(PingExecutor.class);
  }

  @Override
  public void execute(@Nullable final Stage stage, @NotNull final NetworkNode node)
      throws ServiceExecutionException {
    final @NotNull String pingCommandTemplate =
        Strings.nullToEmpty(userPreferences.get(PREF_COMMAND, osServices.getDefaultPingCommand()));
    final @NotNull String options = Strings.nullToEmpty(userPreferences.get(PREF_OPTIONS, ""));
    final Map<String, Object> variables = Map.of("server", node, "options", options);
    log.debug(
        "Ping command template '{}', node: {}, variables: {}",
        pingCommandTemplate,
        node,
        variables);
    final @NotNull String pingCommand;
    try {
      pingCommand = StringUtils.applyTemplate(pingCommandTemplate, variables);
      log.debug("Ping command '{}'", pingCommand);
      final Map<String, String> env = node.getEnv();
      osServices.executeCommand(pingCommand, env, true);
    } catch (final TemplateException e) {
      log.error(
          "Failed to evaluate template '{}' with variables '{}'.",
          pingCommandTemplate,
          variables,
          e);
      throw new ServiceExecutionException(
          "ERR_CANNOT_APPLY_TEMPLATE", e, pingCommandTemplate, variables);
    }
  }
}
