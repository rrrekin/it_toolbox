/*
 *
 * Copyright (c) 2020 Michal Rudewicz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.in.rrrekin.ittoolbox.services.executors;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import javafx.stage.Stage;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.configuration.Configuration;
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

/** @author michal.rudewicz@gmail.com */
public class TracerouteExecutor implements ServiceExecutor {

  @NonNls private static final Logger log = LoggerFactory.getLogger(PingExecutor.class);

  private final @NotNull OsCommandExecutor osCommandExecutor;
  private final @NotNull AppPreferences appPreferences;

  /**
   * Instantiates a new TracerouteExecutor.
   *
   * @param osCommandExecutor the OsCommandExecutor instance to use
   * @param appPreferences the AppPreferences instance to use
   */
  public TracerouteExecutor(
      @NotNull final OsCommandExecutor osCommandExecutor,
      final @NotNull AppPreferences appPreferences) {
    this.osCommandExecutor = requireNonNull(osCommandExecutor, "osServices must not be null");
    this.appPreferences = requireNonNull(appPreferences, "appPreferences must not be null");
  }

  @Override
  public void execute(
      @Nullable final Stage stage,
      @NotNull final Configuration configuration,
      @NotNull final NetworkNode node)
      throws ServiceExecutionException {
    final @NotNull String tracerouteCommandTemplate = appPreferences.getTracerouteCommand();
    final @NotNull String options = appPreferences.getTracerouteOptions();
    final Map<String, Object> variables = Map.of("server", node, "options", options);
    log.debug(
        "Traceroute command template '{}', node: {}, variables: {}",
        tracerouteCommandTemplate,
        node,
        variables);
    final @NotNull String tracerouteCommand;
    try {
      tracerouteCommand = StringUtils.applyTemplate(tracerouteCommandTemplate, variables);
      log.debug("Traceroute command '{}'", tracerouteCommand);
      final Map<String, String> env = node.getEnv();
      osCommandExecutor.executeCommand(tracerouteCommand, env, true);
    } catch (final TemplateException e) {
      throw new ServiceExecutionException(
          "ERR_CANNOT_APPLY_TEMPLATE", e, tracerouteCommandTemplate, variables);
    }
  }
}
