package net.in.rrrekin.ittoolbox.configuration

import javafx.scene.control.TreeItem
import spock.lang.Specification
/**
 * @author michal.rudewicz@gmail.com
 */
class ConfigurationTest extends Specification {

    def "should validate arguments"() {
        when:
        new Configuration(Stub(TreeItem), null)
        then:
        thrown NullPointerException

        when:
        new Configuration(null, [:])
        then:
        thrown NullPointerException
    }

}
