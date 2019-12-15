package net.in.rrrekin.ittoolbox.configuration.nodes


import javafx.scene.paint.Color
import net.in.rrrekin.ittoolbox.configuration.IconDescriptor
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import org.controlsfx.glyphfont.FontAwesome
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class GroupingNodeTest extends Specification {
  static final NAME = 'Main group'
  static final DESCRIPTION = 'Main server example.com'
  static final ICON = new IconDescriptor(FontAwesome.Glyph.SERVER, Color.VIOLET, true)
  static final SERVICES = ['ssh', 'http']
  static final DEFAULT_NAME = 'New group'
  static final DEFAULT_ICON = new IconDescriptor(FontAwesome.Glyph.FOLDER_OPEN, Color.BLUE, true)

  void setup() {
    LocaleUtil.setLocale(Locale.ENGLISH)
  }

  void cleanup() {
    LocaleUtil.setLocale(null)
  }

  def "should construct node with defaults"() {
    expect:
    new GroupingNode(null) == new GroupingNode(DEFAULT_NAME, DEFAULT_NAME, DEFAULT_ICON, [])
    new GroupingNode(NAME) == new GroupingNode(NAME, NAME, DEFAULT_ICON, [])
    new GroupingNode(NAME, null, null, null) == new GroupingNode(NAME,  NAME, DEFAULT_ICON, [])
    new GroupingNode(null, DESCRIPTION, null, null) == new GroupingNode(DEFAULT_NAME, DESCRIPTION, DEFAULT_ICON, [])
  }

  def "should construct node"() {
    when:
    def node = new GroupingNode(NAME, DESCRIPTION, ICON, SERVICES)

    then:
    with(node) {
      name == NAME
      description == DESCRIPTION
      iconDescriptor == ICON
      serviceDescriptors == SERVICES
    }
  }
}
