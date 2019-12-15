package net.in.rrrekin.ittoolbox.infrastructure;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.inject.internal.MoreTypes;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Preferences} wrapper to enable storing complex object storage in application preferences.
 *
 * @author michal.rudewicz @gmail.com
 */
public class UserPreferences {

  private static final Gson serializer = new Gson();
  private final @NotNull Preferences preferences;

  /**
   * Creates UserPreferences instance for a class instance connected with a file.
   *
   * @param clazz the class
   * @param file the file
   */
  public UserPreferences(final @NotNull Class<?> clazz, final @NotNull Path file) {
    Objects.requireNonNull(clazz, "Class must not be null");
    Objects.requireNonNull(file, "File path must not be null");
    final String filePathHash =
        Hashing.sha256().hashString(file.toString(), StandardCharsets.UTF_8).toString();
    preferences =
        Objects.requireNonNull(
            Preferences.userNodeForPackage(clazz).node(clazz.getSimpleName()).node(filePathHash),
            () ->
                String.format(
                    "Failed to get preferences node for class %s for file %s",
                    clazz.getName(), filePathHash));
  }

  /**
   * Creates UserPreferences instance for class common for whole application.
   *
   * @param clazz the class
   */
  public UserPreferences(final @NotNull Class<?> clazz) {
    Objects.requireNonNull(clazz, "Class must not be null");
    preferences =
        Objects.requireNonNull(
            Preferences.userNodeForPackage(clazz).node(clazz.getSimpleName()),
            () -> String.format("Failed to get preferences node for class %s", clazz.getName()));
  }

  /**
   * Put object as json serialized string.
   *
   * @param <T> the object type parameter
   * @param key the key
   * @param object the object to store
   */
  public <T> void putObject(final @NotNull String key, final @NotNull T object) {
    final String json = serializer.toJson(object);
    if (json != null) {
      preferences.put(key, json);
    }
  }

  /**
   * Gets json serialized object from properties.
   *
   * @param <T> the object type parameter
   * @param key the key
   * @param def the default value
   * @param clazz the object type
   * @return the object
   */
  public @Nullable <T> T getObject(
      final @NotNull String key, final @Nullable T def, final @NotNull Class<T> clazz) {
    T object;
    final String json = preferences.get(key, null);
    if (json != null) {
      try {
        object = serializer.fromJson(json, clazz);
      } catch (final Exception ex) {
        object = def;
      }
    } else {
      object = def;
    }
    return object;
  }

  /**
   * Put list of objects as json serialized string.
   *
   * @param <T> the object type parameter
   * @param key the key
   * @param list the list to store
   */
  public <T> void putList(final @NotNull String key, final @NotNull Collection<T> list) {
    final String json = serializer.toJson(list);
    if (json != null) {
      preferences.put(key, json);
    }
  }

  /**
   * Gets json serialized list of objects from properties.
   *
   * @param <T> the object type parameter
   * @param key the key
   * @param def the default value
   * @param clazz the object type
   * @return the object
   */
  public @Nullable <T> List<T> getList(
      final @NotNull String key, final @Nullable List<T> def, final @NotNull Class<T> clazz) {
    List<T> list;
    final String json = preferences.get(key, null);
    if (json != null) {
      try {
        final Type type = new MoreTypes.ParameterizedTypeImpl(null, List.class, clazz);
        list = serializer.fromJson(json, type);
      } catch (final Exception ex) {
        list = def;
      }
    } else {
      list = def;
    }
    return list == null ? def : list;
  }

  // Simple delegating methods

  /**
   * Associates the specified value with the specified key in this preference node.
   *
   * @param key key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @throws NullPointerException if key or value is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH} or if
   *     {@code value.length} exceeds {@code MAX_VALUE_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if either key or value contain the null control character,
   *     code point U+0000.
   */
  public void put(final String key, final String value) {
    preferences.put(key, value);
  }

  /**
   * Returns the value associated with the specified key in this preference node. Returns the
   * specified default if there is no value associated with the key, or the backing store is
   * inaccessible.
   *
   * <p>Some implementations may store default values in their backing stores. If there is no value
   * associated with the specified key but there is such a <i>stored default</i>, it is returned in
   * preference to the specified default.
   *
   * @param key key whose associated value is to be returned.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key}.
   * @return the value associated with {@code key}, or {@code def} if no value is associated with
   *     {@code key}, or the backing store is inaccessible.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}. (A {@code null} value for {@code
   *     def} <i>is</i> permitted.)
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   */
  public String get(final String key, @Nullable final String def) {
    return preferences.get(key, def);
  }

  /**
   * Removes the value associated with the specified key in this preference node, if any.
   *
   * <p>If this implementation supports <i>stored defaults</i>, and there is such a default for the
   * specified preference, the stored default will be "exposed" by this call, in the sense that it
   * will be returned by a succeeding call to {@code get}.
   *
   * @param key key whose mapping is to be removed from the preference node.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   */
  public void remove(final String key) {
    preferences.remove(key);
  }

  /**
   * Removes all of the preferences (key-value associations) in this preference node. This call has
   * no effect on any descendants of this node.
   *
   * <p>If this implementation supports <i>stored defaults</i>, and this node in the preferences
   * hierarchy contains any such defaults, the stored defaults will be "exposed" by this call, in
   * the sense that they will be returned by succeeding calls to {@code get}.
   *
   * @throws BackingStoreException if this operation cannot be completed due to a failure in the
   *     backing store, or inability to communicate with it.
   * @see #removeNode() #removeNode()
   */
  public void clear() throws BackingStoreException {
    preferences.clear();
  }

  /**
   * Associates a string representing the specified int value with the specified key in this
   * preference node. The associated string is the one that would be returned if the int value were
   * passed to {@link Integer#toString(int)}. This method is intended for use in conjunction with
   * {@link #getInt}.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getInt(String, int) #getInt(String, int)
   */
  public void putInt(final String key, final int value) {
    preferences.putInt(key, value);
  }

  /**
   * Returns the int value represented by the string associated with the specified key in this
   * preference node. The string is converted to an integer as by {@link Integer#parseInt(String)}.
   * Returns the specified default if there is no value associated with the key, the backing store
   * is inaccessible, or if {@code Integer.parseInt(String)} would throw a {@link
   * NumberFormatException}* if the associated value were passed. This method is intended for use in
   * conjunction with {@link #putInt}.
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists, is
   * accessible, and could be converted to an int with {@code Integer.parseInt}, this int is
   * returned in preference to the specified default.
   *
   * @param key key whose associated value is to be returned as an int.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as an int, or the
   *     backing store is inaccessible.
   * @return the int value represented by the string associated with {@code key} in this preference
   *     node, or {@code def} if the associated value does not exist or cannot be interpreted as an
   *     int.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #putInt(String, int) #putInt(String, int)
   * @see #get(String, String) #get(String, String)
   */
  public int getInt(final String key, final int def) {
    return preferences.getInt(key, def);
  }

  /**
   * Associates a string representing the specified long value with the specified key in this
   * preference node. The associated string is the one that would be returned if the long value were
   * passed to {@link Long#toString(long)}. This method is intended for use in conjunction with
   * {@link #getLong}.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getLong(String, long) #getLong(String, long)
   */
  public void putLong(final String key, final long value) {
    preferences.putLong(key, value);
  }

  /**
   * Returns the long value represented by the string associated with the specified key in this
   * preference node. The string is converted to a long as by {@link Long#parseLong(String)}.
   * Returns the specified default if there is no value associated with the key, the backing store
   * is inaccessible, or if {@code Long.parseLong(String)} would throw a {@link
   * NumberFormatException}* if the associated value were passed. This method is intended for use in
   * conjunction with {@link #putLong}.
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists, is
   * accessible, and could be converted to a long with {@code Long.parseLong}, this long is returned
   * in preference to the specified default.
   *
   * @param key key whose associated value is to be returned as a long.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as a long, or the
   *     backing store is inaccessible.
   * @return the long value represented by the string associated with {@code key} in this preference
   *     node, or {@code def} if the associated value does not exist or cannot be interpreted as a
   *     long.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #putLong(String, long) #putLong(String, long)
   * @see #get(String, String) #get(String, String)
   */
  public long getLong(final String key, final long def) {
    return preferences.getLong(key, def);
  }

  /**
   * Associates a string representing the specified boolean value with the specified key in this
   * preference node. The associated string is {@code "true"} if the value is true, and {@code
   * "false"}* if it is false. This method is intended for use in conjunction with {@link
   * #getBoolean}*.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getBoolean(String, boolean) #getBoolean(String, boolean)
   * @see #get(String, String) #get(String, String)
   */
  public void putBoolean(final String key, final boolean value) {
    preferences.putBoolean(key, value);
  }

  /**
   * Returns the boolean value represented by the string associated with the specified key in this
   * preference node. Valid strings are {@code "true"}, which represents true, and {@code "false"},
   * which represents false. Case is ignored, so, for example, {@code "TRUE"} and {@code "False"}
   * are also valid. This method is intended for use in conjunction with {@link #putBoolean}.
   *
   * <p>Returns the specified default if there is no value associated with the key, the backing
   * store is inaccessible, or if the associated value is something other than {@code "true"} or
   * {@code "false"}, ignoring case.
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists and is
   * accessible, it is used in preference to the specified default, unless the stored default is
   * something other than {@code "true"} or {@code "false"}, ignoring case, in which case the
   * specified default is used.
   *
   * @param key key whose associated value is to be returned as a boolean.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as a boolean, or
   *     the backing store is inaccessible.
   * @return the boolean value represented by the string associated with {@code key} in this
   *     preference node, or {@code def} if the associated value does not exist or cannot be
   *     interpreted as a boolean.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #get(String, String) #get(String, String)
   * @see #putBoolean(String, boolean) #putBoolean(String, boolean)
   */
  public boolean getBoolean(final String key, final boolean def) {
    return preferences.getBoolean(key, def);
  }

  /**
   * Associates a string representing the specified float value with the specified key in this
   * preference node. The associated string is the one that would be returned if the float value
   * were passed to {@link Float#toString(float)}. This method is intended for use in conjunction
   * with {@link #getFloat}.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getFloat(String, float) #getFloat(String, float)
   */
  public void putFloat(final String key, final float value) {
    preferences.putFloat(key, value);
  }

  /**
   * Returns the float value represented by the string associated with the specified key in this
   * preference node. The string is converted to an integer as by {@link Float#parseFloat(String)}.
   * Returns the specified default if there is no value associated with the key, the backing store
   * is inaccessible, or if {@code Float.parseFloat(String)} would throw a {@link
   * NumberFormatException}* if the associated value were passed. This method is intended for use in
   * conjunction with {@link #putFloat}.
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists, is
   * accessible, and could be converted to a float with {@code Float.parseFloat}, this float is
   * returned in preference to the specified default.
   *
   * @param key key whose associated value is to be returned as a float.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as a float, or
   *     the backing store is inaccessible.
   * @return the float value represented by the string associated with {@code key} in this
   *     preference node, or {@code def} if the associated value does not exist or cannot be
   *     interpreted as a float.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #putFloat(String, float) #putFloat(String, float)
   * @see #get(String, String) #get(String, String)
   */
  public float getFloat(final String key, final float def) {
    return preferences.getFloat(key, def);
  }

  /**
   * Associates a string representing the specified double value with the specified key in this
   * preference node. The associated string is the one that would be returned if the double value
   * were passed to {@link Double#toString(double)}. This method is intended for use in conjunction
   * with {@link #getDouble}.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if {@code key.length()} exceeds {@code MAX_KEY_LENGTH}.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getDouble(String, double) #getDouble(String, double)
   */
  public void putDouble(final String key, final double value) {
    preferences.putDouble(key, value);
  }

  /**
   * Returns the double value represented by the string associated with the specified key in this
   * preference node. The string is converted to an integer as by {@link
   * Double#parseDouble(String)}*. Returns the specified default if there is no value associated
   * with the key, the backing store is inaccessible, or if {@code Double.parseDouble(String)} would
   * throw a {@link NumberFormatException} if the associated value were passed. This method is
   * intended for use in conjunction with {@link #putDouble}.
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists, is
   * accessible, and could be converted to a double with {@code Double.parseDouble}, this double is
   * returned in preference to the specified default.
   *
   * @param key key whose associated value is to be returned as a double.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as a double, or
   *     the backing store is inaccessible.
   * @return the double value represented by the string associated with {@code key} in this
   *     preference node, or {@code def} if the associated value does not exist or cannot be
   *     interpreted as a double.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #putDouble(String, double) #putDouble(String, double)
   * @see #get(String, String) #get(String, String)
   */
  public double getDouble(final String key, final double def) {
    return preferences.getDouble(key, def);
  }

  /**
   * Associates a string representing the specified byte array with the specified key in this
   * preference node. The associated string is the <i>Base64</i> encoding of the byte array, as
   * defined in <a href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a>, Section 6.8, with one
   * minor change: the string will consist solely of characters from the <i>Base64 Alphabet</i>; it
   * will not contain any newline characters. Note that the maximum length of the byte array is
   * limited to three quarters of {@code MAX_VALUE_LENGTH} so that the length of the Base64 encoded
   * String does not exceed {@code MAX_VALUE_LENGTH}. This method is intended for use in conjunction
   * with {@link #getByteArray}.
   *
   * @param key key with which the string form of value is to be associated.
   * @param value value whose string form is to be associated with key.
   * @throws NullPointerException if key or value is {@code null}.
   * @throws IllegalArgumentException if key.length() exceeds MAX_KEY_LENGTH or if value.length
   *     exceeds MAX_VALUE_LENGTH*3/4.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #getByteArray(String, byte[]) #getByteArray(String, byte[])
   * @see #get(String, String) #get(String, String)
   */
  public void putByteArray(final String key, final byte[] value) {
    preferences.putByteArray(key, value);
  }

  /**
   * Returns the byte array value represented by the string associated with the specified key in
   * this preference node. Valid strings are <i>Base64</i> encoded binary data, as defined in <a
   * href=http://www.ietf.org/rfc/rfc2045.txt>RFC 2045</a>, Section 6.8, with one minor change: the
   * string must consist solely of characters from the <i>Base64 Alphabet</i>; no newline characters
   * or extraneous characters are permitted. This method is intended for use in conjunction with
   * {@link #putByteArray}.
   *
   * <p>Returns the specified default if there is no value associated with the key, the backing
   * store is inaccessible, or if the associated value is not a valid Base64 encoded byte array (as
   * defined above).
   *
   * <p>If the implementation supports <i>stored defaults</i> and such a default exists and is
   * accessible, it is used in preference to the specified default, unless the stored default is not
   * a valid Base64 encoded byte array (as defined above), in which case the specified default is
   * used.
   *
   * @param key key whose associated value is to be returned as a byte array.
   * @param def the value to be returned in the event that this preference node has no value
   *     associated with {@code key} or the associated value cannot be interpreted as a byte array,
   *     or the backing store is inaccessible.
   * @return the byte array value represented by the string associated with {@code key} in this
   *     preference node, or {@code def} if the associated value does not exist or cannot be
   *     interpreted as a byte array.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @throws NullPointerException if {@code key} is {@code null}. (A {@code null} value for {@code
   *     def} <i>is</i> permitted.)
   * @throws IllegalArgumentException if key contains the null control character, code point U+0000.
   * @see #get(String, String) #get(String, String)
   * @see #putByteArray(String, byte[]) #putByteArray(String, byte[])
   */
  public byte[] getByteArray(final String key, final byte[] def) {
    return preferences.getByteArray(key, def);
  }

  /**
   * Returns all of the keys that have an associated value in this preference node. (The returned
   * array will be of size zero if this node has no preferences.)
   *
   * <p>If the implementation supports <i>stored defaults</i> and there are any such defaults at
   * this node that have not been overridden, by explicit preferences, the defaults are returned in
   * the array in addition to any explicit preferences.
   *
   * @return an array of the keys that have an associated value in this preference node.
   * @throws BackingStoreException if this operation cannot be completed due to a failure in the
   *     backing store, or inability to communicate with it.
   */
  public String[] keys() throws BackingStoreException {
    return preferences.keys();
  }

  /**
   * Removes this preference node and all of its descendants, invalidating any preferences contained
   * in the removed nodes. Once a node has been removed, attempting any method other than {@link
   * #name()}*, {@link #absolutePath()}, {@link #isUserNode()}, {@link #flush()} or {@link
   * #node(String) nodeExists("")}* on the corresponding {@code Preferences} instance will fail with
   * an {@code IllegalStateException}. (The methods defined on {@link Object} can still be invoked
   * on a node after it has been removed; they will not throw {@code IllegalStateException}.)
   *
   * <p>The removal is not guaranteed to be persistent until the {@code flush} method is called on
   * this node (or an ancestor).
   *
   * <p>If this implementation supports <i>stored defaults</i>, removing a node exposes any stored
   * defaults at or below this node. Thus, a subsequent call to {@code nodeExists} on this node's
   * path name may return {@code true}, and a subsequent call to {@code node} on this path name may
   * return a (different) {@code Preferences} instance representing a non-empty collection of
   * preferences and/or children.
   *
   * @throws BackingStoreException if this operation cannot be completed due to a failure in the
   *     backing store, or inability to communicate with it.
   * @see #flush() #flush()
   */
  public void removeNode() throws BackingStoreException {
    preferences.removeNode();
  }

  /**
   * Returns this preference node's name, relative to its parent.
   *
   * @return this preference node's name, relative to its parent.
   */
  public String name() {
    return preferences.name();
  }

  /**
   * Returns this preference node's absolute path name.
   *
   * @return this preference node's absolute path name.
   */
  public String absolutePath() {
    return preferences.absolutePath();
  }

  /**
   * Forces any changes in the contents of this preference node and its descendants to the
   * persistent store. Once this method returns successfully, it is safe to assume that all changes
   * made in the subtree rooted at this node prior to the method invocation have become permanent.
   *
   * <p>Implementations are free to flush changes into the persistent store at any time. They do not
   * need to wait for this method to be called.
   *
   * <p>When a flush occurs on a newly created node, it is made persistent, as are any ancestors
   * (and descendants) that have yet to be made persistent. Note however that any preference value
   * changes in ancestors are <i>not</i> guaranteed to be made persistent.
   *
   * <p>If this method is invoked on a node that has been removed with the {@link #removeNode()}
   * method, flushSpi() is invoked on this node, but not on others.
   *
   * @throws BackingStoreException if this operation cannot be completed due to a failure in the
   *     backing store, or inability to communicate with it.
   * @see #sync() #sync()
   */
  public void flush() throws BackingStoreException {
    preferences.flush();
  }

  /**
   * Ensures that future reads from this preference node and its descendants reflect any changes
   * that were committed to the persistent store (from any VM) prior to the {@code sync} invocation.
   * As a side-effect, forces any changes in the contents of this preference node and its
   * descendants to the persistent store, as if the {@code flush} method had been invoked on this
   * node.
   *
   * @throws BackingStoreException if this operation cannot be completed due to a failure in the
   *     backing store, or inability to communicate with it.
   * @see #flush() #flush()
   */
  public void sync() throws BackingStoreException {
    preferences.sync();
  }

  /**
   * Registers the specified listener to receive <i>preference change events</i> for this preference
   * node. A preference change event is generated when a preference is added to this node, removed
   * from this node, or when the value associated with a preference is changed. (Preference change
   * events are <i>not</i> generated by the {@link #removeNode()} method, which generates a <i>node
   * change event</i>. Preference change events <i>are</i> generated by the {@code clear} method.)
   *
   * <p>Events are only guaranteed for changes made within the same JVM as the registered listener,
   * though some implementations may generate events for changes made outside this JVM. Events may
   * be generated before the changes have been made persistent. Events are not generated when
   * preferences are modified in descendants of this node; a caller desiring such events must
   * register with each descendant.
   *
   * @param pcl The preference change listener to add.
   * @throws NullPointerException if {@code pcl} is null.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @see #removePreferenceChangeListener(PreferenceChangeListener)
   *     #removePreferenceChangeListener(PreferenceChangeListener)
   * @see #addNodeChangeListener(NodeChangeListener) #addNodeChangeListener(NodeChangeListener)
   */
  public void addPreferenceChangeListener(final PreferenceChangeListener pcl) {
    preferences.addPreferenceChangeListener(pcl);
  }

  /**
   * Removes the specified preference change listener, so it no longer receives preference change
   * events.
   *
   * @param pcl The preference change listener to remove.
   * @throws IllegalArgumentException if {@code pcl} was not a registered preference change listener
   *     on this node.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @see #addPreferenceChangeListener(PreferenceChangeListener)
   *     #addPreferenceChangeListener(PreferenceChangeListener)
   */
  public void removePreferenceChangeListener(final PreferenceChangeListener pcl) {
    preferences.removePreferenceChangeListener(pcl);
  }

  /**
   * Registers the specified listener to receive <i>node change events</i> for this node. A node
   * change event is generated when a child node is added to or removed from this node. (A single
   * {@link #removeNode()} invocation results in multiple <i>node change events</i>, one for every
   * node in the subtree rooted at the removed node.)
   *
   * <p>Events are only guaranteed for changes made within the same JVM as the registered listener,
   * though some implementations may generate events for changes made outside this JVM. Events may
   * be generated before the changes have become permanent. Events are not generated when indirect
   * descendants of this node are added or removed; a caller desiring such events must register with
   * each descendant.
   *
   * <p>Few guarantees can be made regarding node creation. Because nodes are created implicitly
   * upon access, it may not be feasible for an implementation to determine whether a child node
   * existed in the backing store prior to access (for example, because the backing store is
   * unreachable or cached information is out of date). Under these circumstances, implementations
   * are neither required to generate node change events nor prohibited from doing so.
   *
   * @param ncl The {@code NodeChangeListener} to add.
   * @throws NullPointerException if {@code ncl} is null.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @see #removeNodeChangeListener(NodeChangeListener)
   *     #removeNodeChangeListener(NodeChangeListener)
   * @see #addPreferenceChangeListener(PreferenceChangeListener)
   *     #addPreferenceChangeListener(PreferenceChangeListener)
   */
  public void addNodeChangeListener(final NodeChangeListener ncl) {
    preferences.addNodeChangeListener(ncl);
  }

  /**
   * Removes the specified {@code NodeChangeListener}, so it no longer receives change events.
   *
   * @param ncl The {@code NodeChangeListener} to remove.
   * @throws IllegalArgumentException if {@code ncl} was not a registered {@code NodeChangeListener}
   *     on this node.
   * @throws IllegalStateException if this node (or an ancestor) has been removed with the {@link
   *     #removeNode()} method.
   * @see #addNodeChangeListener(NodeChangeListener) #addNodeChangeListener(NodeChangeListener)
   */
  public void removeNodeChangeListener(final NodeChangeListener ncl) {
    preferences.removeNodeChangeListener(ncl);
  }
}
