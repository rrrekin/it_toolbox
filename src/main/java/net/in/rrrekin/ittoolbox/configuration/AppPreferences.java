package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import net.in.rrrekin.ittoolbox.os.OsServices;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application preferences.
 *
 * @author michal.rudewicz @gmail.com
 */
@Singleton
public class AppPreferences {

  @NonNls private static final Logger log = LoggerFactory.getLogger(AppPreferences.class);

  private final @NotNull OsServices osServices;

  private int iconSize = 20;
  private int maxRecentFiles = 20;
  private @NotNull String fontFamily = "DejaVu Sans";
  private int fontSize = 11;

  // OS related config
  private String shellCommand = "";
  private List<String> shellOptions = null;
  private String terminalCommand = "";

  // Commands config
  private String pingCommand = "";
  private String pingOptions = "";
  private String tracerouteCommand = "";
  private String tracerouteOptions = "";
  private String nmapCommand = "";
  private String nmapOptions = "";

  /**
   * Instantiates a new AppPreferences. @param osServices the OsServices instance to use @param
   * osServices the OsServices instance to use
   *
   * @param osServices the OsServices instance to use
   */
  @Inject
  public AppPreferences(final @NotNull OsServices osServices) {
    log.info("Creating AppPreferences");
    this.osServices = requireNonNull(osServices, "osServices must not be null");
  }

  /**
   * Gets icon size.
   *
   * @return the icon size
   */
  public int getIconSize() {
    return iconSize;
  }

  /**
   * Sets icon size.
   *
   * @param iconSize the icon size
   */
  public void setIconSize(final int iconSize) {
    this.iconSize = iconSize;
  }

  /**
   * Gets max number of recent files.
   *
   * @return the max recent files
   */
  public int getMaxRecentFiles() {
    return maxRecentFiles;
  }

  /**
   * Sets max number of recent files.
   *
   * @param maxRecentFiles the max recent files
   */
  public void setMaxRecentFiles(int maxRecentFiles) {
    this.maxRecentFiles = maxRecentFiles;
  }

  /**
   * Gets font family.
   *
   * @return the font family
   */
  public @NotNull String getFontFamily() {
    return fontFamily;
  }

  /**
   * Sets font family.
   *
   * @param fontFamily the font family
   */
  public void setFontFamily(final @NotNull String fontFamily) {
    this.fontFamily = requireNonNull(fontFamily, "FontFamily must not be null");
  }

  /**
   * Gets font size.
   *
   * @return the font size
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   * Sets font size.
   *
   * @param fontSize the font size
   */
  public void setFontSize(final int fontSize) {
    checkArgument(fontSize >= 6 && fontSize <= 24, "Font size mus be in the range from 6 to 24.");
    this.fontSize = fontSize;
  }

  /**
   * Gets shell command.
   *
   * @return the shell command
   */
  public @NotNull String getShellCommand() {
    if (shellCommand == null || shellCommand.trim().isEmpty()) {
      shellCommand = osServices.getDefaultShellCommand();
    }
    return shellCommand;
  }

  /**
   * Sets shell command.
   *
   * @param shellCommand the shell command
   */
  public void setShellCommand(final String shellCommand) {
    this.shellCommand = shellCommand;
  }

  /**
   * Gets shell options.
   *
   * @return the shell options
   */
  public List<String> getShellOptions() {
    if (shellOptions == null) {
      shellOptions = osServices.getDefaultShellOptions();
    }
    return shellOptions;
  }

  /**
   * Sets shell options.
   *
   * @param shellOptions the shell options
   */
  public void setShellOptions(final List<String> shellOptions) {
    this.shellOptions = shellOptions;
  }

  /**
   * Gets terminal command.
   *
   * @return the terminal command
   */
  public @NotNull String getTerminalCommand() {
    if (terminalCommand == null || terminalCommand.trim().isEmpty()) {
      terminalCommand = osServices.getDefaultTerminalCommand();
    }
    return terminalCommand;
  }

  /**
   * Sets terminal command.
   *
   * @param terminalCommand the terminal command
   */
  public void setTerminalCommand(final String terminalCommand) {
    this.terminalCommand = terminalCommand;
  }

  /**
   * Gets ping command.
   *
   * @return the ping command
   */
  public @NotNull String getPingCommand() {
    if (pingCommand == null || pingCommand.trim().isEmpty()) {
      pingCommand = osServices.getDefaultPingCommand();
    }
    return pingCommand;
  }

  /**
   * Sets ping command.
   *
   * @param pingCommand the ping command
   */
  public void setPingCommand(final String pingCommand) {
    this.pingCommand = pingCommand;
  }

  /**
   * Gets ping options.
   *
   * @return the ping options
   */
  public @NotNull String getPingOptions() {
    return Strings.nullToEmpty(pingOptions);
  }

  /**
   * Sets ping options.
   *
   * @param pingOptions the ping options
   */
  public void setPingOptions(String pingOptions) {
    this.pingOptions = pingOptions;
  }

  /**
   * Gets traceroute command.
   *
   * @return the traceroute command
   */
  public String getTracerouteCommand() {
    if (tracerouteCommand == null || tracerouteCommand.trim().isEmpty()) {
      tracerouteCommand = osServices.getDefaultTracerouteCommand();
    }
    return tracerouteCommand;
  }

  /**
   * Sets traceroute command.
   *
   * @param tracerouteCommand the traceroute command
   */
  public void setTracerouteCommand(final String tracerouteCommand) {
    this.tracerouteCommand = tracerouteCommand;
  }

  /**
   * Gets traceroute options.
   *
   * @return the traceroute options
   */
  public String getTracerouteOptions() {
    return Strings.nullToEmpty(tracerouteOptions);
  }

  /**
   * Sets traceroute options.
   *
   * @param tracerouteOptions the traceroute options
   */
  public void setTracerouteOptions(final String tracerouteOptions) {
    this.tracerouteOptions = tracerouteOptions;
  }

  public String getNmapCommand() {
    if (nmapCommand == null || nmapCommand.trim().isEmpty()) {
      nmapCommand = osServices.getDefaultNmapCommand();
    }
    return nmapCommand;
  }

  public void setNmapCommand(final String nmapCommand) {
    this.nmapCommand = nmapCommand;
  }

  public String getNmapOptions() {
    return Strings.nullToEmpty(nmapOptions);
  }

  public void setNmapOptions(final String nmapOptions) {
    this.nmapOptions = nmapOptions;
  }
}
