package net.in.rrrekin.ittoolbox.configuration.nodes

import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class ServerTest extends Specification {

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
        newInstance == instance

        when:
        dto.type = NodeType.SERVER.typeName.toUpperCase()
        newInstance = new Server(dto)

        then:
        newInstance
    }

    def "should handle invalid dto"() {
        given:
        sampleDto[NetworkNode.SERVICES_PROPERTY] = 'not a list'

        when:
        def newInstance = new Server(sampleDto)

        then:
        newInstance == new Server(NAME, ADDRESS, DESCRIPTION, PROPERTIES, [])
    }
}
