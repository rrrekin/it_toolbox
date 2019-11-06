package net.in.rrrekin.ittoolbox.os;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link OsServices}. Used for OS without specific support or when given
 * OS do not need specific version.
 *
 * @author michal.rudewicz @gmail.com
 */
@Slf4j
public class OsServicesDefaultImpl implements OsServices {

  private static final String SHELL_ENV_VAR = "SHELL";
  private static final String SHELL_LIST_FILE = "/etc/shells";
  private static final String OPTIONS_PLACEHOLDER = "${options.trim()?' '+options:''}";
  private static final String ADDRESS_PLACEHOLDER = " ${server.address}";
  private static final String PORT_SEMICOLON_PLACEHOLDER = "${port>0?':'+port:''}";
  @NonNls private final ProgramLocationService locationService;

  public OsServicesDefaultImpl(final @NonNull ProgramLocationService locationService) {
    this.locationService = locationService;
  }

  @Override
  public void logOsConfiguration() {
    log.info("OS_NAME: {}", OsServices.OS_NAME);
    log.info("OS_VERSION: {}", OsServices.OS_VERSION);
    log.info("OS_ARCH: {}", OsServices.OS_ARCH);
    log.info("USER_NAME: {}", OsServices.USER_NAME);
    log.info("USER_HOME: {}", OsServices.USER_HOME);
    log.info("LINE_SEPARATOR: {}", escapeJava(System.lineSeparator()));
    log.info("FILE_SEPARATOR: {}", File.separator);
    log.info("PATH_SEPARATOR: {}", File.pathSeparator);
  }

  @NonNls
  @Override
  public @NotNull String getDefaultTerminalCommand() {
    return getPossibleTerminalCommands().get(0);
  }

  @Override
  public @NotNull List<String> getPossibleTerminalCommands() {
    @NonNls final List<String> response = newArrayList();
    appendCommandIfPresent("terminator", " -x $command ';' sleep 10d", response);
    appendCommandIfPresent("konsole", " --hold -e $command", response);
    appendCommandIfPresent("xterm", " -hold -e $command", response);
    appendCommandIfPresent("gnome-terminal", " -- $command", response);
    if (response.isEmpty()) {
      response.add("xterm -hold -e $command");
    }
    return response;
  }

  private void appendCommandIfPresent(
      @NonNls final @NotNull String command,
      @NonNls final @NotNull String arguments,
      final @NotNull List<String> list) {
    final File commandPath = locationService.getProgramBinary(command);
    if (commandPath != null) {
      list.add(commandPath.getAbsolutePath() + arguments);
    }
  }

  @NonNls
  @Override
  public @NotNull String getDefaultPingCommand() {
    return "ping" + OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER;
  }

  @Override
  public @NotNull List<String> getPossiblePingCommands() {
    return ImmutableList.of(getDefaultPingCommand());
  }

  @NonNls
  @Override
  public @NotNull String getDefaultTracerouteCommand() {
    return getPossibleTracerouteCommands().get(0);
  }

  @Override
  public @NotNull List<String> getPossibleTracerouteCommands() {
    @NonNls final List<String> response = newArrayList();
    appendCommandIfPresent("mtr", OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER, response);
    appendCommandIfPresent("traceroute", OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER, response);
    if (response.isEmpty()) {
      response.add("traceroute" + OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER);
    }
    return response;
  }

  @NonNls
  @Override
  public @NotNull String getDefaultNslookupCommand() {
    return getPossibleNslookupCommands().get(0);
  }

  @Override
  public @NotNull List<String> getPossibleNslookupCommands() {
    @NonNls final List<String> response = newArrayList();
    appendCommandIfPresent("nslookup", OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER, response);
    appendCommandIfPresent("dig", OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER, response);
    if (response.isEmpty()) {
      response.add("nslookup" + OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER);
    }
    return response;
  }

  @NonNls
  @Override
  public @NotNull String getDefaultSshCommand() {
    return "ssh"
        + ADDRESS_PLACEHOLDER
        + "${user.trim()?' -l \"'+user+'\"':''}${port>0?' -p '+port:''}"
        + OPTIONS_PLACEHOLDER;
  }

  @Override
  public @NotNull List<String> getPossibleSshCommands() {
    return ImmutableList.of(getDefaultSshCommand());
  }

  @NonNls
  @Override
  public @NotNull String getDefaultRdpCommand() {
    return getPossibleRdpCommands().get(0);
  }

  @Override
  public @NotNull List<String> getPossibleRdpCommands() {
    @NonNls final List<String> response = newArrayList();
    appendCommandIfPresent(
        "rdesktop",
        "${user.trim()?' -u \"'+user+'\"':''}${password.trim()?' -p \"'+password+'\"':''}"
            + OPTIONS_PLACEHOLDER
            + ADDRESS_PLACEHOLDER
            + PORT_SEMICOLON_PLACEHOLDER,
        response);
    appendCommandIfPresent(
        "xfreerdp",
        "${user.trim()?' /u:\"'+user+'\"':''}${password.trim()?' /p:\"'+password+'\"':''}"
            + OPTIONS_PLACEHOLDER
                + " /v:${server.address}"
            + PORT_SEMICOLON_PLACEHOLDER,
        response);
    if (response.isEmpty()) {
      response.add(
          "rdesktop${user.trim()?' -u \"'+user+'\"':''}${password.trim()?' -p \"'+password+'\"':''}"
              + OPTIONS_PLACEHOLDER
              + ADDRESS_PLACEHOLDER
              + PORT_SEMICOLON_PLACEHOLDER);
    }
    return response;
  }

  @NonNls
  @Override
  public @NotNull String getDefaultVncCommand() {
    return "xvncviewer" + ADDRESS_PLACEHOLDER + PORT_SEMICOLON_PLACEHOLDER + OPTIONS_PLACEHOLDER;
  }

  @Override
  public @NotNull List<String> getPossibleVncCommands() {
    return ImmutableList.of(getDefaultVncCommand());
  }

  @NonNls
  @Override
  public @NotNull String getDefaultShellCommand() {
    return "/bin/sh${options.trim()?' '+options:''}";
  }

  @Override
  public @NotNull List<String> getPossibleShellCommands() {
    final String shellFromEnv = System.getenv(SHELL_ENV_VAR);
    @NonNls final List<String> response = newArrayList();
    if (!StringUtils.isBlank(shellFromEnv)) {
      log.info("Detected default shell: {}", shellFromEnv);
      response.add(shellFromEnv);
    } else {
      log.info("Default shell not detected");
    }
    try (final Stream<String> lines = Files.lines(Paths.get(SHELL_LIST_FILE))) {
      final List<String> otherShells =
          lines.filter(shell -> !shell.equals(shellFromEnv)).collect(Collectors.toList());
      log.info("Other shells from {}: {}", SHELL_LIST_FILE, String.join(", ", otherShells));
      response.addAll(otherShells);
    } catch (final IOException e) {
      log.info("Cannot read '/etc/shells' file: {}", e.getMessage());
    }
    if (response.isEmpty()) {
      response.add("/bin/sh");
    }
    return response.stream().map(shell -> shell + OPTIONS_PLACEHOLDER).collect(Collectors.toList());
  }

  @Override
  public void executeCommand(
      final String command, @NonNull Map<String, String> env, final boolean inTerminal) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
