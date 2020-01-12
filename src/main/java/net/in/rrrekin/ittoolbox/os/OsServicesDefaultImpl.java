package net.in.rrrekin.ittoolbox.os;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper;
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Default implementation of {@link OsServices}. Used for OS without specific support or when given
 * OS do not need specific version.
 *
 * @author michal.rudewicz @gmail.com
 */
public class OsServicesDefaultImpl implements OsServices {

  private static final String SHELL_ENV_VAR = "SHELL";
  private static final String SHELL_LIST_FILE = "/etc/shells";
  private static final String OPTIONS_PLACEHOLDER = "${options.trim()?' '+options:''}";
  private static final String ADDRESS_PLACEHOLDER = " ${server.address}";
  private static final String PORT_SEMICOLON_PLACEHOLDER = "${port>0?':'+port:''}";
  private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");

  @NonNls
  private static final Logger log = org.slf4j.LoggerFactory.getLogger(OsServicesDefaultImpl.class);

  private final @NotNull SystemWrapper system;
  @NonNls private final @NotNull ProgramLocationService locationService;

  public OsServicesDefaultImpl(
      final @NotNull SystemWrapper system, final @NotNull ProgramLocationService locationService) {
    this.system = requireNonNull(system, "SystemWrapper must not be null");
    this.locationService = requireNonNull(locationService, "LocationService must not be null");
  }

  @Override
  public void logOsConfiguration() {
    log.info("OS_NAME: {}", system.getProperty(OsServices.OS_NAME_ENV_VAR));
    log.info("OS_VERSION: {}", system.getProperty(OsServices.OS_VERSION_ENV_VAR));
    log.info("OS_ARCH: {}", system.getProperty(OsServices.OS_ARCH_ENV_VAR));
    log.info("USER_NAME: {}", system.getProperty(OsServices.USER_NAME_ENV_VAR));
    log.info("USER_HOME: {}", system.getProperty(OsServices.USER_HOME_ENV_VAR));
    log.info("LINE_SEPARATOR: {}", escapeJava(system.lineSeparator()));
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
    log.debug("Possible terminal commands: {}", response);
    return response;
  }

  private void appendCommandIfPresent(
      @NonNls final @NotNull String command,
      @NonNls final @NotNull String arguments,
      final @NotNull List<String> list) {
    final File commandPath = locationService.getProgramBinary(command);
    if (commandPath != null) {
      list.add(escapeJava(commandPath.getAbsolutePath()) + arguments);
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
    log.debug("Possible traceroute commands: {}", response);
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
    log.debug("Possible nslookup commands: {}", response);
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

  @NonNls
  @Override
  public @NotNull String getDefaultNmapCommand() {
    return "nmap" + OPTIONS_PLACEHOLDER + ADDRESS_PLACEHOLDER;
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
    log.debug("Possible rdp commands: {}", response);
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
    final @NotNull List<String> shellCommands = getPossibleShellCommands();
    if (shellCommands.isEmpty()) {
      return "/bin/sh";
    } else {
      return shellCommands.get(0);
    }
  }

  @Override
  public @NotNull List<String> getPossibleShellCommands() {
    final String shellFromEnv = system.getenv(SHELL_ENV_VAR);
    @NonNls final List<String> response = newArrayList();
    if (!org.apache.commons.lang3.StringUtils.isBlank(shellFromEnv)) {
      log.info("Detected default shell: {}", shellFromEnv);
      response.add(shellFromEnv);
    } else {
      log.info("Default shell not detected");
    }
    try (final Stream<String> lines = Files.lines(Paths.get(SHELL_LIST_FILE))) {
      final List<String> otherShells =
          lines
              .map(line -> COMMENT_PATTERN.matcher(line).replaceAll(""))
              .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
              .filter(shell -> !shell.equals(shellFromEnv))
              .collect(Collectors.toList());
      log.info("Other shells from {}: {}", SHELL_LIST_FILE, String.join(", ", otherShells));
      response.addAll(otherShells);
    } catch (final IOException e) {
      log.info("Cannot read '/etc/shells' file: {}", e.getMessage());
    }
    if (response.isEmpty()) {
      response.add("/bin/sh");
    }
    log.debug("Possible shell commands: {}", response);
    return response;
  }

  @Override
  public @NotNull List<String> getDefaultShellOptions() {
    return List.of("-c");
  }
}
