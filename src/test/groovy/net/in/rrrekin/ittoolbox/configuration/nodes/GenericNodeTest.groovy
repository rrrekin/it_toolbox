package net.in.rrrekin.ittoolbox.configuration.nodes

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons
import jiconfont.swing.IconFontSwing
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class GenericNodeTest extends Specification implements NetworkNodeCompareTrait {

  static final NAME = 'Main server'
  static final DESCRIPTION = 'Main server example.com'
  static final PROPERTIES = [profile: 'production', cpuCount: '8']
  static final SERVICES = ['ssh', 'http']

  def sampleDto = [
    (NetworkNode.TYPE_PROPERTY)       : NodeType.GENERIC_NODE.typeName,
    (NetworkNode.NAME_PROPERTY)       : NAME,
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
    new GenericNode(null as String)
    then:
    thrown NullPointerException

    when:
    new GenericNode(null as Map<String, Object>)
    then:
    thrown NullPointerException

    when:
    new GenericNode(null)
    then:
    thrown NullPointerException

    when:
    new GenericNode(null, DESCRIPTION, PROPERTIES, SERVICES)
    then:
    thrown NullPointerException

    when:
    new GenericNode(NAME, null, PROPERTIES, SERVICES)
    then:
    thrown NullPointerException

    when:
    new GenericNode(NAME, DESCRIPTION, null, SERVICES)
    then:
    thrown NullPointerException

    when:
    new GenericNode(NAME, DESCRIPTION, PROPERTIES, null)
    then:
    thrown NullPointerException
  }

  def "should create instance for given address"() {
    when:
    def instance = new GenericNode(NAME)

    then:
    instance.name == NAME
    instance.description == NAME
    instance.properties == [:]
    instance.serviceDescriptors == []
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }


  def "should create instance complete definition"() {
    when:
    def instance = new GenericNode(NAME, DESCRIPTION, PROPERTIES, SERVICES)

    then:
    instance.name == NAME
    instance.description == DESCRIPTION
    instance.properties == PROPERTIES
    instance.serviceDescriptors == SERVICES
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }

  def "should create object from minimal dto"() {
    when:
    def instance = new GenericNode([type: 'genericnode'])

    then:
    instance.name == ''
    instance.description == ''
    instance.properties == [:]
    instance.serviceDescriptors == []
    instance.childNodes == []
    instance.isLeaf()
    //instance.icon
  }

  def "should validate dto"() {
    when:
    def instance = new GenericNode([type: 'x'])

    then:
    thrown IllegalArgumentException
  }


  def "should serialize and deserialize"() {
    given:
    def instance = new GenericNode(NAME, DESCRIPTION, PROPERTIES, SERVICES)

    when:
    def dto = instance.dtoProperties

    then:
    dto == sampleDto

    when:
    def newInstance = new GenericNode(dto)

    then:
    equal(newInstance, instance)

    when:
    dto.type = NodeType.GENERIC_NODE.typeName.toUpperCase()
    newInstance = new GenericNode(dto)

    then:
    equal(newInstance, instance)

    when:
    dto[null] = 'NULL'
    newInstance = new GenericNode(dto)

    then:
    equal(newInstance, instance)
  }

  def "should handle invalid dto"() {
    given:
    sampleDto[NetworkNode.SERVICES_PROPERTY] = 'not a list'

    when:
    def newInstance = new GenericNode(sampleDto)

    then:
    equal(newInstance, new GenericNode(NAME, DESCRIPTION, PROPERTIES, []))
  }


  def "hashcode should not change when content changed"() {
    given:
    def instance = new GenericNode(NAME, DESCRIPTION, PROPERTIES, SERVICES)
    def oldHashCode = instance.hashCode()

    when:
    instance.setName("new name")
    instance.setDescription("new description")
    instance.getProperties().put('abc', 'def')
    instance.getServiceDescriptors().add("cmd: restart all")

    then:
    instance.hashCode() == oldHashCode
  }

  def "should lazily generate icon"() {
    given:
    def instance = new GenericNode(NAME, DESCRIPTION, PROPERTIES, SERVICES)

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
    def instance = new GenericNode(NAME, DESCRIPTION, PROPERTIES, SERVICES)

    expect:
    instance.toHtml().contains(NAME)
    instance.toHtml().contains(DESCRIPTION)
  }

}
