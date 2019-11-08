package net.in.rrrekin.ittoolbox.os;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Class that provide information nad execution method dependent on operating system where
 * application is executed.
 *
 * @author michal.rudewicz @gmail.com
 */
public interface OsServices {
  /** Java property key for operating system name (os.name). */
  String OS_NAME_ENV_VAR = "os.name";

  /** Java property key for operating system version (os.version). */
  String OS_VERSION_ENV_VAR = "os.version";

  /** Java property key for operating system architecture (os.arch). */
  String OS_ARCH_ENV_VAR = "os.arch";

  /** Java property key for user home directory path (user.home). */
  String USER_HOME_ENV_VAR = "user.home";

  /** Java property key for use name (user.name). */
  String USER_NAME_ENV_VAR = "user.name";


  /** Log OS configuration data. */
  void logOsConfiguration();

  /**
   * Gets default terminal command.
   *
   * @return the default terminal command
   */
  @NonNls
  @NotNull
  String getDefaultTerminalCommand();

  /**
   * Gets possible terminal commands.
   *
   * @return the possible terminal commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleTerminalCommands();

  /**
   * Gets default ping command.
   *
   * @return the default ping command
   */
  @NonNls
  @NotNull
  String getDefaultPingCommand();

  /**
   * Gets possible ping commands.
   *
   * @return the possible ping commands
   */
  @NonNls
  @NotNull
  List<String> getPossiblePingCommands();

  /**
   * Gets default traceroute command.
   *
   * @return the default traceroute command
   */
  @NonNls
  @NotNull
  String getDefaultTracerouteCommand();

  /**
   * Gets possible traceroute commands.
   *
   * @return the possible traceroute commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleTracerouteCommands();

  /**
   * Gets default nslookup command.
   *
   * @return the default nslookup command
   */
  @NonNls
  @NotNull
  String getDefaultNslookupCommand();

  /**
   * Gets possible nslookup commands.
   *
   * @return the possible nslookup commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleNslookupCommands();

  /**
   * Gets default ssh command.
   *
   * @return the default ssh command
   */
  @NonNls
  @NotNull
  String getDefaultSshCommand();

  /**
   * Gets possible ssh commands.
   *
   * @return the possible ssh commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleSshCommands();

  /**
   * Gets default rdp command.
   *
   * @return the default rdp command
   */
  @NonNls
  @NotNull
  String getDefaultRdpCommand();

  /**
   * Gets possible rdp commands.
   *
   * @return the possible rdp commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleRdpCommands();

  /**
   * Gets default vnc command.
   *
   * @return the default vnc command
   */
  @NonNls
  @NotNull
  String getDefaultVncCommand();

  /**
   * Gets possible vnc commands.
   *
   * @return the possible vnc commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleVncCommands();

  /**
   * Gets default shell command.
   *
   * @return the default shell command
   */
  @NonNls
  @NotNull
  String getDefaultShellCommand();

  /**
   * Gets possible shell commands.
   *
   * @return the possible shell commands
   */
  @NonNls
  @NotNull
  List<String> getPossibleShellCommands();

  /**
   * Execute command.
   *
   * @param command the command
   * @param env the environment for executed command
   * @param inTerminal if true, the command will be executed in a terminal window
   */
  void executeCommand(
      @NonNull String command, @NonNull Map<String, String> env, @NonNull boolean inTerminal);
}
