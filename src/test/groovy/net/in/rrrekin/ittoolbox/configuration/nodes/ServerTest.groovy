package net.in.rrrekin.ittoolbox.configuration.nodes

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons
import jiconfont.swing.IconFontSwing
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class ServerTest extends Specification implements NetworkNodeCompareTrait {

  static final NAME = 'Main server'
  static final ADDRESS = 'example.com'
  static final DESCRIPTION = 'Main server example.com'
  static final PROPERTIES = [profile: 'production', cpuCount: '8']
  static final SERVICES = ['ssh', 'http']

  def sampleDto = [
    (NetworkNode.TYPE_PROPERTY)       : NodeType.SERVER.typeName,
    (NetworkNode.NAME_PROPERTY)       : NAME,
    (NetworkNode.ADDRESS_PROEPRTY)    : ADDRESS,
    (NetworkNode.DESCRIPTION_PROPERTY): DESCRIPTION,
    (NetworkNode.SERVICES_PROPERTY)   : SERVICES,
    _profile                          : 'production',
    _cpuCount                         : '8',
  ]

  void setupSpec() {
    IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
  }

  def "should require constructor parameters"() {
    when:
    new Server(null as String)
    then:
    thrown NullPointerException

    when:
    new Server(null)
    then:
    thrown NullPointerException

    when:
    new Server(null, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)
    then:
    thrown NullPointerException

    when:
    new Server(NAME, null, DESCRIPTION, PROPERTIES, SERVICES)
    then:
    thrown NullPointerException

    when:
    new Server(NAME, ADDRESS, null, PROPERTIES, SERVICES)
    then:
    thrown NullPointerException

    when:
    new Server(NAME, ADDRESS, DESCRIPTION, null, SERVICES)
    then:
    thrown NullPointerException

    when:
    new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, null)
    then:
    thrown NullPointerException
  }

  def "should create instance for given address"() {
    when:
    def instance = new Server(ADDRESS)

    then:
    instance.name == ADDRESS
    instance.address == ADDRESS
    instance.description == ADDRESS
    instance.properties == [:]
    instance.serviceDescriptors == []
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }


  def "should create instance complete definition"() {
    when:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)

    then:
    instance.name == NAME
    instance.address == ADDRESS
    instance.description == DESCRIPTION
    instance.properties == PROPERTIES
    instance.serviceDescriptors == SERVICES
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }


  def "should create object from minimal dto"() {
    when:
    def instance = new Server([type: 'server'])

    then:
    instance.name == ''
    instance.address == ''
    instance.description == ''
    instance.properties == [:]
    instance.serviceDescriptors == []
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }

  def "should create immutable data copy"() {
    when:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)

    then:
    instance.immutableDataCopy() == new Server.Data(NAME, ADDRESS, DESCRIPTION, PROPERTIES)
  }

  def "immutable data copy should validate arguments"() {
    when:
    new Server.Data(null, ADDRESS, DESCRIPTION, PROPERTIES)
    then:
    thrown NullPointerException

    when:
    new Server.Data(NAME, null, DESCRIPTION, PROPERTIES)
    then:
    thrown NullPointerException

    when:
    new Server.Data(NAME, ADDRESS, null, PROPERTIES)
    then:
    thrown NullPointerException

    when:
    new Server.Data(NAME, ADDRESS, DESCRIPTION, null)
    then:
    thrown NullPointerException
  }

  def "should validate dto"() {
    when:
    def instance = new Server([type: 'x'])

    then:
    thrown IllegalArgumentException
  }

  def "should serialize and deserialize"() {
    given:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)

    when:
    def dto = instance.dtoProperties

    then:
    dto == sampleDto

    when:
    def newInstance = new Server(dto)

    then:
    equal(newInstance, instance)

    when:
    dto.type = NodeType.SERVER.typeName.toUpperCase()
    newInstance = new Server(dto)

    then:
    equal(newInstance, instance)

    when:
    dto[null] = 'NULL'
    newInstance = new Server(dto)

    then:
    equal(newInstance, instance)
  }

  def "should handle invalid dto"() {
    given:
    sampleDto[NetworkNode.SERVICES_PROPERTY] = 'not a list'

    when:
    def newInstance = new Server(sampleDto)

    then:
    equal(newInstance, new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, []))
  }

  def "hashcode should not change when content changed"() {
    given:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)
    def oldHashCode = instance.hashCode()

    when:
    instance.setName("new name")
    instance.setAddress("new address")
    instance.setDescription("new description")
    instance.getProperties().put('abc', 'def')
    instance.getServiceDescriptors().add("cmd: restart all")

    then:
    instance.hashCode() == oldHashCode
  }

  def "should lazily generate icon"() {
    given:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)

    expect:
    instance.@icon == null

    when:
    def icon = instance.getIcon()

    then:
    icon == instance.@icon

    and:
    icon.is instance.getIcon()
  }

  def "should return HTML description of the node"() {
    given:
    def instance = new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, SERVICES)

    expect:
    instance.toHtml().contains(NAME)
    instance.toHtml().contains(ADDRESS)
    instance.toHtml().contains(DESCRIPTION)
  }

}
