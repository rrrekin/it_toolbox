package net.in.rrrekin.ittoolbox.configuration.nodes

import javafx.scene.paint.Color
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import org.controlsfx.glyphfont.FontAwesome
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class ServerTest extends Specification {

  static final NAME = 'Main server'
  static final ADDRESS = 'example.com'
  static final ICON = new IconDescriptor(FontAwesome.Glyph.HOME, Color.VIOLET, true)
  static final DESCRIPTION = 'Main server example.com'
  static final PROPERTIES = [profile: 'production', cpuCount: '8']
  static final SERVICES = ['ssh', 'http']
  static final DEFAULT_NAME = 'New server'
  static final DEFAULT_ICON = new IconDescriptor(FontAwesome.Glyph.SERVER, Color.BLUE, true)

  void setup() {
    LocaleUtil.setLocale(Locale.ENGLISH)
  }

  void cleanup() {
    LocaleUtil.setLocale(null)
  }

  def "should construct node with defaults"() {
    expect:
    new Server(null) == new Server(DEFAULT_NAME, DEFAULT_NAME, DEFAULT_NAME, DEFAULT_ICON, [:], [])
    new Server(NAME) == new Server(NAME, NAME, NAME, DEFAULT_ICON, [:], [])
    new Server(null, null, null, null, null, null) == new Server(DEFAULT_NAME, DEFAULT_NAME, DEFAULT_NAME, DEFAULT_ICON, [:], [])
    new Server(NAME, null, null, null, null, null) == new Server(NAME, NAME, NAME, DEFAULT_ICON, [:], [])
    new Server(null, null, DESCRIPTION, null, null, null) == new Server(DEFAULT_NAME, DEFAULT_NAME, DESCRIPTION, DEFAULT_ICON, [:], [])
    new Server(null, ADDRESS, null, null, null, null) == new Server(DEFAULT_NAME, ADDRESS, DEFAULT_NAME, DEFAULT_ICON, [:], [])
  }

  def "should construct node"() {
    when:
    def node = new Server(NAME, ADDRESS, DESCRIPTION, ICON, PROPERTIES, SERVICES)

    then:
    with(node) {
      name == NAME
      address == ADDRESS
      description == DESCRIPTION
      iconDescriptor == ICON
      properties == PROPERTIES
      serviceDescriptors == SERVICES
    }
  }
}
