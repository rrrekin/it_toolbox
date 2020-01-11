package net.in.rrrekin.ittoolbox.services.executors;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode;
import net.in.rrrekin.ittoolbox.os.OsCommandExecutor;
import net.in.rrrekin.ittoolbox.services.ServiceExecutor;
import net.in.rrrekin.ittoolbox.services.exceptions.ServiceExecutionException;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PingExecutor - responsible for execution of the ping command.
 *
 * @author michal.rudewicz@gmail.com
 */
public class PingExecutor implements ServiceExecutor {

  @NonNls private static final Logger log = LoggerFactory.getLogger(PingExecutor.class);

  private final @NotNull OsCommandExecutor osCommandExecutor;
  private final @NotNull AppPreferences appPreferences;

  /**
   * Instantiates a new PingExecutor.
   *
   * @param osCommandExecutor the OsCommandExecutor instance to use
   * @param appPreferences the AppPreferences instance to use
   */
  public PingExecutor(
      @NotNull final OsCommandExecutor osCommandExecutor,
      final @NotNull AppPreferences appPreferences) {
    this.osCommandExecutor = requireNonNull(osCommandExecutor, "osServices must not be null");
    this.appPreferences = requireNonNull(appPreferences, "appPreferences must not be null");
  }

  @Override
  public void execute(@Nullable final Stage stage, @NotNull final NetworkNode node)
      throws ServiceExecutionException {
    final @NotNull String pingCommandTemplate = appPreferences.getPingCommand();
    final @NotNull String options = appPreferences.getPingOptions();
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
      osCommandExecutor.executeCommand(pingCommand, env, true);
    } catch (final TemplateException e) {
      throw new ServiceExecutionException(
          "ERR_CANNOT_APPLY_TEMPLATE", e, pingCommandTemplate, variables);
    }
  }
}
