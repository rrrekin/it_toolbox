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

package net.in.rrrekin.ittoolbox.os;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.in.rrrekin.ittoolbox.configuration.AppPreferences;
import net.in.rrrekin.ittoolbox.services.exceptions.ServiceExecutionException;
import net.in.rrrekin.ittoolbox.utilities.StringUtils;
import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author michal.rudewicz@gmail.com */
public class OsCommandExecutor {

  @NonNls
  private static final Logger log = LoggerFactory.getLogger(OsCommandExecutor.class);
  //

  private final @NotNull AppPreferences appPreferences;

  @Inject
  public OsCommandExecutor(final @NotNull AppPreferences appPreferences) {
    log.info("Creating OsCommandExecutor");
    this.appPreferences = Objects.requireNonNull(appPreferences, "appPreferences must not be null");
  }

  /**
   * Execute command.
   *
   * @param command the command
   * @param env the environment for executed command
   * @param inTerminal if true, the command will be executed in a terminal window
   */
  public void executeCommand(
      final @NotNull String command,
      @NotNull final Map<String, String> env,
      final boolean inTerminal)
      throws ServiceExecutionException {
    log.info(
        "Executing command '{}' {} terminal with environment {}",
        command,
        inTerminal ? "in" : "without",
        env);

    final @NotNull String commandToExecute;
    if (inTerminal) {
      @NotNull final String commandToExecuteTemplate = appPreferences.getTerminalCommand();
      log.info("Terminal command template: {}", commandToExecuteTemplate);
      final Map<String, Object> variables = Map.of("command", command);
      try {
        commandToExecute = StringUtils.applyTemplate(commandToExecuteTemplate, variables);
      } catch (final TemplateException e) {
        log.error(
            "Failed to evaluate terminal command template '{}' with  variables '{}'.",
            commandToExecuteTemplate,
            variables,
            e);
        throw new ServiceExecutionException(
            "ERR_CANNOT_APPLY_TEMPLATE", e, commandToExecuteTemplate, variables);
      }
    } else {
      commandToExecute = command;
    }
    try {
      final List<String> commandElements = new ArrayList<>();
      commandElements.add(appPreferences.getShellCommand());
      commandElements.addAll(appPreferences.getShellOptions());
      commandElements.add(commandToExecute);
      log.debug("Full command to execute: {} ", commandElements);
      new ProcessBuilder().command(commandElements).inheritIO().start();
    } catch (final IOException e) {
      throw new ServiceExecutionException(
          "ERR_FAILED_TO_EXECUTE_COMMAND", e, commandToExecute, e.getLocalizedMessage());
    }
    log.info("Command to execute: {}", commandToExecute);
  }
}
