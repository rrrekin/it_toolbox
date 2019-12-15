package net.in.rrrekin.ittoolbox.infrastructure

import spock.lang.Specification

import java.nio.file.Path

/**
 * @author michal.rudewicz@gmail.com
 */
class UserPreferencesFactoryTest extends Specification {

  def factory = new UserPreferencesFactory()

  def "should create instance of preferences"() {

    when:
    def prefs = factory.create(this.class, Path.of('/usr/bin'))

    then:
    prefs.absolutePath() == '/net/in/rrrekin/ittoolbox/infrastructure/UserPreferencesFactoryTest/33629c0e654360bfd8ab5089302a26e27dce5044d624b22f51fcbc66f1e0d19c'

    when:
    def prefs2 = factory.create(this.class)

    then:
    prefs2.absolutePath() == '/net/in/rrrekin/ittoolbox/infrastructure/UserPreferencesFactoryTest'

    cleanup:
    prefs.removeNode()
    prefs2.removeNode()
  }
}
