package net.in.rrrekin.ittoolbox.configuration;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static net.in.rrrekin.ittoolbox.gui.services.CommonResources.FONT_AWESOME;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Icon descriptor.
 *
 * @author michal.rudewicz @gmail.com
 */
public class IconDescriptor implements Serializable {

  private static final @NotNull FontAwesome.Glyph DEFAULT_GLYPH = FontAwesome.Glyph.QUESTION_CIRCLE;
  private static final @NotNull Color DEFAULT_COLOR = Color.BLUE;
  @Inject private static AppPreferences appPreferences;

  private @NotNull FontAwesome.Glyph glyph;
  private @NotNull Color color;
  private boolean gradient;

  private static final @NotNull Map<String, String> colorNameMapping = buildReverseColorMapping();

  @SuppressWarnings("HardCodedStringLiteral")
  private static @NotNull Map<String, String> buildReverseColorMapping() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    builder.put("0xf0f8ffff", "aliceblue");
    builder.put("0xfaebd7ff", "antiquewhite");
    builder.put("0x7fffd4ff", "aquamarine");
    builder.put("0xf0ffffff", "azure");
    builder.put("0xf5f5dcff", "beige");
    builder.put("0xffe4c4ff", "bisque");
    builder.put("0x000000ff", "black");
    builder.put("0xffebcdff", "blanchedalmond");
    builder.put("0x0000ffff", "blue");
    builder.put("0x8a2be2ff", "blueviolet");
    builder.put("0xa52a2aff", "brown");
    builder.put("0xdeb887ff", "burlywood");
    builder.put("0x5f9ea0ff", "cadetblue");
    builder.put("0x7fff00ff", "chartreuse");
    builder.put("0xd2691eff", "chocolate");
    builder.put("0xff7f50ff", "coral");
    builder.put("0x6495edff", "cornflowerblue");
    builder.put("0xfff8dcff", "cornsilk");
    builder.put("0xdc143cff", "crimson");
    builder.put("0x00ffffff", "cyan");
    builder.put("0x00008bff", "darkblue");
    builder.put("0x008b8bff", "darkcyan");
    builder.put("0xb8860bff", "darkgoldenrod");
    builder.put("0x006400ff", "darkgreen");
    builder.put("0xa9a9a9ff", "darkgrey");
    builder.put("0xbdb76bff", "darkkhaki");
    builder.put("0x8b008bff", "darkmagenta");
    builder.put("0x556b2fff", "darkolivegreen");
    builder.put("0xff8c00ff", "darkorange");
    builder.put("0x9932ccff", "darkorchid");
    builder.put("0x8b0000ff", "darkred");
    builder.put("0xe9967aff", "darksalmon");
    builder.put("0x8fbc8fff", "darkseagreen");
    builder.put("0x483d8bff", "darkslateblue");
    builder.put("0x2f4f4fff", "darkslategrey");
    builder.put("0x00ced1ff", "darkturquoise");
    builder.put("0x9400d3ff", "darkviolet");
    builder.put("0xff1493ff", "deeppink");
    builder.put("0x00bfffff", "deepskyblue");
    builder.put("0x696969ff", "dimgrey");
    builder.put("0x1e90ffff", "dodgerblue");
    builder.put("0xb22222ff", "firebrick");
    builder.put("0xfffaf0ff", "floralwhite");
    builder.put("0x228b22ff", "forestgreen");
    builder.put("0xdcdcdcff", "gainsboro");
    builder.put("0xf8f8ffff", "ghostwhite");
    builder.put("0xffd700ff", "gold");
    builder.put("0xdaa520ff", "goldenrod");
    builder.put("0x008000ff", "green");
    builder.put("0xadff2fff", "greenyellow");
    builder.put("0x808080ff", "grey");
    builder.put("0xf0fff0ff", "honeydew");
    builder.put("0xff69b4ff", "hotpink");
    builder.put("0xcd5c5cff", "indianred");
    builder.put("0x4b0082ff", "indigo");
    builder.put("0xfffff0ff", "ivory");
    builder.put("0xf0e68cff", "khaki");
    builder.put("0xe6e6faff", "lavender");
    builder.put("0xfff0f5ff", "lavenderblush");
    builder.put("0x7cfc00ff", "lawngreen");
    builder.put("0xfffacdff", "lemonchiffon");
    builder.put("0xadd8e6ff", "lightblue");
    builder.put("0xf08080ff", "lightcoral");
    builder.put("0xe0ffffff", "lightcyan");
    builder.put("0xfafad2ff", "lightgoldenrodyellow");
    builder.put("0x90ee90ff", "lightgreen");
    builder.put("0xd3d3d3ff", "lightgrey");
    builder.put("0xffb6c1ff", "lightpink");
    builder.put("0xffa07aff", "lightsalmon");
    builder.put("0x20b2aaff", "lightseagreen");
    builder.put("0x87cefaff", "lightskyblue");
    builder.put("0x778899ff", "lightslategrey");
    builder.put("0xb0c4deff", "lightsteelblue");
    builder.put("0xffffe0ff", "lightyellow");
    builder.put("0x00ff00ff", "lime");
    builder.put("0x32cd32ff", "limegreen");
    builder.put("0xfaf0e6ff", "linen");
    builder.put("0xff00ffff", "magenta");
    builder.put("0x800000ff", "maroon");
    builder.put("0x66cdaaff", "mediumaquamarine");
    builder.put("0x0000cdff", "mediumblue");
    builder.put("0xba55d3ff", "mediumorchid");
    builder.put("0x9370dbff", "mediumpurple");
    builder.put("0x3cb371ff", "mediumseagreen");
    builder.put("0x7b68eeff", "mediumslateblue");
    builder.put("0x00fa9aff", "mediumspringgreen");
    builder.put("0x48d1ccff", "mediumturquoise");
    builder.put("0xc71585ff", "mediumvioletred");
    builder.put("0x191970ff", "midnightblue");
    builder.put("0xf5fffaff", "mintcream");
    builder.put("0xffe4e1ff", "mistyrose");
    builder.put("0xffe4b5ff", "moccasin");
    builder.put("0xffdeadff", "navajowhite");
    builder.put("0x000080ff", "navy");
    builder.put("0xfdf5e6ff", "oldlace");
    builder.put("0x808000ff", "olive");
    builder.put("0x6b8e23ff", "olivedrab");
    builder.put("0xffa500ff", "orange");
    builder.put("0xff4500ff", "orangered");
    builder.put("0xda70d6ff", "orchid");
    builder.put("0xeee8aaff", "palegoldenrod");
    builder.put("0x98fb98ff", "palegreen");
    builder.put("0xafeeeeff", "paleturquoise");
    builder.put("0xdb7093ff", "palevioletred");
    builder.put("0xffefd5ff", "papayawhip");
    builder.put("0xffdab9ff", "peachpuff");
    builder.put("0xcd853fff", "peru");
    builder.put("0xffc0cbff", "pink");
    builder.put("0xdda0ddff", "plum");
    builder.put("0xb0e0e6ff", "powderblue");
    builder.put("0x800080ff", "purple");
    builder.put("0xff0000ff", "red");
    builder.put("0xbc8f8fff", "rosybrown");
    builder.put("0x4169e1ff", "royalblue");
    builder.put("0x8b4513ff", "saddlebrown");
    builder.put("0xfa8072ff", "salmon");
    builder.put("0xf4a460ff", "sandybrown");
    builder.put("0x2e8b57ff", "seagreen");
    builder.put("0xfff5eeff", "seashell");
    builder.put("0xa0522dff", "sienna");
    builder.put("0xc0c0c0ff", "silver");
    builder.put("0x87ceebff", "skyblue");
    builder.put("0x6a5acdff", "slateblue");
    builder.put("0x708090ff", "slategrey");
    builder.put("0xfffafaff", "snow");
    builder.put("0x00ff7fff", "springgreen");
    builder.put("0x4682b4ff", "steelblue");
    builder.put("0xd2b48cff", "tan");
    builder.put("0x008080ff", "teal");
    builder.put("0xd8bfd8ff", "thistle");
    builder.put("0xff6347ff", "tomato");
    builder.put("0x00000000", "transparent");
    builder.put("0x40e0d0ff", "turquoise");
    builder.put("0xee82eeff", "violet");
    builder.put("0xf5deb3ff", "wheat");
    builder.put("0xffffffff", "white");
    builder.put("0xf5f5f5ff", "whitesmoke");
    builder.put("0xffff00ff", "yellow");
    builder.put("0x9acd32ff", "yellowgreen");
    return builder.build();
  }

  /**
   * Instantiates a new Icon descriptor.
   *
   * @param glyph the glyph
   * @param color the color
   * @param gradient the gradient
   */
  public IconDescriptor(
      final @NotNull FontAwesome.Glyph glyph, final @NotNull Color color, final boolean gradient) {
    this.glyph = requireNonNull(glyph, "Glyph must not be null");
    this.color = requireNonNull(color, "Color must not be null");
    this.gradient = gradient;
  }

  /**
   * Gets icon.
   *
   * @return the icon
   */
  public Glyph getIcon() {
    checkState(appPreferences != null, "AppPreferencess must not be null");
    if (gradient) {
      return FONT_AWESOME
          .create(glyph)
          .color(color)
          .useGradientEffect()
          .size(appPreferences.getIconSize());
    } else {
      return FONT_AWESOME.create(glyph).color(color).size(appPreferences.getIconSize());
    }
  }

  /**
   * Of icon descriptor.
   *
   * @param descriptor the descriptor
   * @return the icon descriptor
   */
  public static @Nullable IconDescriptor of(final @Nullable String descriptor) {
    IconDescriptor iconDescriptor = null;
    if (descriptor != null) {
      final String[] components = descriptor.split(":");
      if (components.length == 3) {
        FontAwesome.Glyph glyph;
        try {
          glyph =
              components.length > 0
                  ? FontAwesome.Glyph.valueOf(components[0].toUpperCase())
                  : DEFAULT_GLYPH;
        } catch (final RuntimeException e) {
          glyph = DEFAULT_GLYPH;
        }

        Color color;
        try {
          color = components.length > 1 ? Color.web(components[1]) : DEFAULT_COLOR;
        } catch (final RuntimeException e) {
          color = DEFAULT_COLOR;
        }
        if (color == null) {
          color = DEFAULT_COLOR;
        }

        final boolean gradient = components.length <= 2 || Boolean.parseBoolean(components[2]);
        iconDescriptor = new IconDescriptor(glyph, color, gradient);
      }
    }
    return iconDescriptor;
  }

  @NonNls
  @Override
  public String toString() {
    return glyph.name()
        + ':'
        + colorNameMapping.getOrDefault(color.toString(), color.toString())
        + ':'
        + gradient;
  }

  /**
   * Gets glyph.
   *
   * @return the glyph
   */
  public @NotNull FontAwesome.Glyph getGlyph() {
    return glyph;
  }

  /**
   * Gets color.
   *
   * @return the color
   */
  public @NotNull Color getColor() {
    return color;
  }

  /**
   * Is gradient boolean.
   *
   * @return the boolean
   */
  public boolean isGradient() {
    return gradient;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final IconDescriptor that = (IconDescriptor) o;
    return gradient == that.gradient
        && glyph == that.glyph
        && color.toString().equals(that.color.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(glyph, color.toString(), gradient);
  }

  private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
    final IconDescriptor newIconDescriptor = of(stream.readUTF());
    glyph = newIconDescriptor.glyph;
    color = newIconDescriptor.color;
    gradient = newIconDescriptor.gradient;
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.writeUTF(toString());
  }
}
