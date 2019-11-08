package net.in.rrrekin.ittoolbox.configuration

import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class ConfigSyncTaskTest extends Specification {

    ConfigurationManager configurationManager = Mock()

    def instance = new ConfigSyncTask(configurationManager)


    def "should validate constructor arguments"() {
        when:
        new ConfigSyncTask(null)

        then:
        thrown NullPointerException
    }

    def "should perform sync"() {
        when:
        instance.run()

        then: "order is important - app changes wins over file changes"
        1 * configurationManager.saveIfDirty()
        then:
        1 * configurationManager.loadIfChanged()
        0 * _._
    }

    def "should not forward exceptions"() {
        when:
        instance.run()
        then:
        1 * configurationManager.saveIfDirty() >> {throw new RuntimeException()}

        when:
        instance.run()
        then:
        1 * configurationManager.loadIfChanged() >> {throw new RuntimeException()}

    }
}
