package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.NotNull;

/**
 * Application preferences.
 *
 * @author michal.rudewicz @gmail.com
 */
public class AppPreferences {

  private int iconSize = 20;
  private int maxRecentFiles = 20;
  private @NotNull String fontFamily = "DejaVu Sans";
  private int fontSize = 11;

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

  public @NotNull String getFontFamily() {
    return fontFamily;
  }

  public void setFontFamily(final @NotNull String fontFamily) {
    this.fontFamily = requireNonNull(fontFamily, "FontFamily must not be null");
  }

  public int getFontSize() {
    return fontSize;
  }

  public void setFontSize(final int fontSize) {
    checkArgument(fontSize>=6 && fontSize<=24, "Font size mus be in the range from 6 to 24.");
    this.fontSize = fontSize;
  }
}
