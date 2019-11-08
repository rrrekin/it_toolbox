package net.in.rrrekin.ittoolbox.infrastructure;

import java.io.InputStream;
import java.io.PrintStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class for System standard Java class.
 *
 * @author michal.rudewicz @gmail.com
 */
@SuppressWarnings("ALL")
public class SystemWrapper {

  /** Wrapper for {@link System#out} */
  public PrintStream out = System.out; // NOSONAR

  /** Wrapper for {@link System#err} */
  public PrintStream err = System.err; // NOSONAR

  /** Wrapper for {@link System#in} */
  public InputStream in = System.in; // NOSONAR

  /**
   * Wrapper for {@link System#exit(int)}
   *
   * @param status exit status.
   * @throws SecurityException if a security manager exists and its <code>checkExit</code> method
   *     doesn't allow exit with the specified status.
   * @see java.lang.Runtime#exit(int)
   */
  public void exit(final int status) {
    System.exit(status);
  }

  /**
   * Wrapper for {@link System#getenv(String)} ()}
   *
   * @param name the environement variable name
   * @return the environement variable value
   */
  public @Nullable String getenv(final @NotNull String name) {
    return System.getenv(name);
  }

  /**
   * Wrapper for {@link System#getProperty(String)}
   *
   * @param name the property name
   * @return the property value
   */
  public String getProperty(final @NotNull String name) {
    return System.getProperty(name);
  }

  /**
   * Wrapper for {@link System#getProperty(String, String)}
   *
   * @param name the property name
   * @param defaultVal the default property value
   * @return the property value
   */
  public String getProperty(final @NotNull String name, final @Nullable String defaultVal) {
    return System.getProperty(name, defaultVal);
  }

  /**
   * Wrapper for {@link System#lineSeparator()}
   *
   * @return the system-dependent line separator string
   */
  public String lineSeparator() {
    return System.lineSeparator();
  }
}
