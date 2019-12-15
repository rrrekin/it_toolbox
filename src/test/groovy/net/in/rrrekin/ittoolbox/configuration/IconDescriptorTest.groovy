package net.in.rrrekin.ittoolbox.configuration

import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class IconDescriptorTest extends Specification {

  static final GLYPH = FontAwesome.Glyph.USER_SECRET
  static final COLOR = new Color(0.42, 0.05, 0.07, 0.88)

  void setupSpec() {
    IconDescriptor.@appPreferences = new AppPreferences()
  }

  @Unroll
  def "should serialize and deserialize #icon"() {
    expect:
    IconDescriptor.of(serialized) == icon
    icon.toString() == reserialized

    where:
    icon                                                            | serialized                    | reserialized
    new IconDescriptor(GLYPH, COLOR, true)                          | 'USER_SECRET:0x6b0d12e0:true' | 'USER_SECRET:0x6b0d12e0:true'
    new IconDescriptor(FontAwesome.Glyph.TREE, Color.ORANGE, false) | 'TREE:orange:false'           | 'TREE:orange:false'
    new IconDescriptor(FontAwesome.Glyph.TREE, Color.ORANGE, false) | 'TREE:ORANGE:false'           | 'TREE:orange:false'
    new IconDescriptor(FontAwesome.Glyph.TREE, Color.ORANGE, false) | 'TREE:0xffa500ff:false'       | 'TREE:orange:false'
  }
}
