package net.in.rrrekin.ittoolbox.configuration.nodes

import javafx.scene.paint.Color
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import org.controlsfx.glyphfont.FontAwesome
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class GenericNodeTest extends Specification {

  static final NAME = 'Main server'
  static final DESCRIPTION = 'Main server example.com'
  static final ICON = new IconDescriptor(FontAwesome.Glyph.SERVER, Color.VIOLET, true)
  static final PROPERTIES = [profile: 'production', cpuCount: '8']
  static final SERVICES = ['ssh', 'http']
  static final DEFAULT_NAME = 'New network element'
  static final DEFAULT_ICON = new IconDescriptor(FontAwesome.Glyph.CUBES, Color.BLUE, true)

  void setup() {
    LocaleUtil.setLocale(Locale.ENGLISH)
  }

  void cleanup() {
    LocaleUtil.setLocale(null)
  }

  def "should construct node with defaults"() {
    expect:
    new GenericNode(null) == new GenericNode(DEFAULT_NAME, DEFAULT_NAME, DEFAULT_ICON, [:], [])
    new GenericNode(NAME) == new GenericNode(NAME, NAME, DEFAULT_ICON, [:], [])
    new GenericNode(null, null, null, null, null) == new GenericNode(DEFAULT_NAME, DEFAULT_NAME, DEFAULT_ICON, [:], [])
    new GenericNode(NAME, null, null, null, null) == new GenericNode(NAME, NAME, DEFAULT_ICON, [:], [])
    new GenericNode(null, DESCRIPTION, null, null, null) == new GenericNode(DEFAULT_NAME, DESCRIPTION, DEFAULT_ICON, [:], [])
  }

  def "should construct node"() {
    when:
    def node = new GenericNode(NAME, DESCRIPTION, ICON, PROPERTIES, SERVICES)

    then:
    with(node) {
      name == NAME
      description == DESCRIPTION
      iconDescriptor == ICON
      properties == PROPERTIES
      serviceDescriptors == SERVICES
    }
  }
}
