package net.in.rrrekin.ittoolbox.infrastructure

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

/**
 * @author michal.rudewicz@gmail.com
 */
class UserPreferencesTest extends Specification {

  static final KEY = 'a_key'
  @Shared
    sampleObject = new Sample('Arthur Dent', 42, [
      new Sample('Zaphod Beeblebrox', 7, []),
      new Sample('Ford Prefect', 1, null),
      new Sample('Trillian', 3, []),
    ])
  @Shared List<Sample> sampleList=[null,sampleObject,new Sample('Slartibartfast', Integer.MAX_VALUE, [])]

  def prefs = new UserPreferences(this.class)

  void setupSpec() {
    new UserPreferences(this.class).removeNode()
  }

  void cleanup() {
    prefs.removeNode()
  }

  def "create instance"() {
    expect:
    prefs.absolutePath() == '/net/in/rrrekin/ittoolbox/infrastructure/UserPreferencesTest'

    when:
    def other = new UserPreferences(this.class, Path.of('/usr/bin/bash'))

    then:
    other.absolutePath() == '/net/in/rrrekin/ittoolbox/infrastructure/UserPreferencesTest/82c0aad1330a0c428ee3f9b7ff0d5ba7e5238c550c58c17aa4ed27382902e002'

    cleanup:
    other.removeNode()
  }

  def "should serialize and deserialize objects"() {
    expect:
    prefs.keys().length == 0
    prefs.get(KEY, 'default') == 'default'
    prefs.getObject(KEY, sampleObject, Sample) == sampleObject
    prefs.getObject(KEY, null, Sample) == null

    when:
    prefs.putObject(KEY, sampleObject)

    then:
    prefs.keys().length == 1
    prefs.keys()[0] == KEY
    prefs.get(KEY, 'default') == '{"key":"Arthur Dent","value":42,"children":[{"key":"Zaphod Beeblebrox","value":7,"children":[]},{"key":"Ford Prefect","value":1},{"key":"Trillian","value":3,"children":[]}]}'
    prefs.getObject(KEY, sampleObject, Sample) == sampleObject
    prefs.getObject(KEY, null, Sample) == sampleObject
  }

  def "should serialize and deserialize lists"() {
    expect:
    prefs.keys().length == 0
    prefs.get(KEY, 'default') == 'default'
    prefs.getList(KEY, sampleList, Sample) == sampleList
    prefs.getList(KEY, null, Sample) == null

    when:
    prefs.putList(KEY, sampleList)

    then:
    prefs.keys().length == 1
    prefs.keys()[0] == KEY
    prefs.get(KEY, 'default') == '[null,{"key":"Arthur Dent","value":42,"children":[{"key":"Zaphod Beeblebrox","value":7,"children":[]},{"key":"Ford Prefect","value":1},{"key":"Trillian","value":3,"children":[]}]},{"key":"Slartibartfast","value":2147483647,"children":[]}]'
    prefs.getList(KEY, sampleList, Sample) == sampleList
    prefs.getList(KEY, null, Sample) == sampleList
  }


  @TupleConstructor
  @EqualsAndHashCode
  @ToString
  static class Sample {
    String key
    int value
    List<Sample> children
  }
}
