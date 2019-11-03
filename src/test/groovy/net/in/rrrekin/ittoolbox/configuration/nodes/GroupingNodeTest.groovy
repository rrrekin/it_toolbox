package net.in.rrrekin.ittoolbox.configuration.nodes

import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class GroupingNodeTest extends Specification {
    static final NAME = 'Main group'
    static final DESCRIPTION = 'Main server example.com'
    static final CHILD_NODES = [new Server('abc'), new GenericNode('asdfaasdf'), new Server('adsfdsf')]
    static final SERVICES = ['ssh', 'http']

    def sampleDto = [
            (NetworkNode.TYPE_PROPERTY)       : NodeType.GROUP.typeName,
            (NetworkNode.NAME_PROPERTY)       : NAME,
            (NetworkNode.DESCRIPTION_PROPERTY): DESCRIPTION,
            (NetworkNode.CHILD_NODES_PROPERTY): [CHILD_NODES[0].dtoProperties, CHILD_NODES[1].dtoProperties, CHILD_NODES[2].dtoProperties],
            (NetworkNode.SERVICES_PROPERTY)   : SERVICES,
    ]

    def "should require constructor parameters"() {
        when:
        new GroupingNode(null as String)
        then:
        thrown NullPointerException

        when:
        new GroupingNode(null)
        then:
        thrown NullPointerException

        when:
        new GroupingNode(null, DESCRIPTION, CHILD_NODES, SERVICES)
        then:
        thrown NullPointerException

        when:
        new GroupingNode(NAME, null, CHILD_NODES, SERVICES)
        then:
        thrown NullPointerException

        when:
        new GroupingNode(NAME, DESCRIPTION, null, SERVICES)
        then:
        thrown NullPointerException

        when:
        new GroupingNode(NAME, DESCRIPTION, CHILD_NODES, null)
        then:
        thrown NullPointerException
    }

    def "should create instance for given address"() {
        when:
        def instance = new GroupingNode(NAME)

        then:
        instance.name == NAME
        instance.description == NAME
        instance.childNodes == []
        instance.properties == null
        instance.serviceDescriptors == []
        !instance.isLeaf()
        //instance.icon
    }


    def "should create instance complete definition"() {
        when:
        def instance = new GroupingNode(NAME, DESCRIPTION, CHILD_NODES, SERVICES)

        then:
        instance.name == NAME
        instance.description == DESCRIPTION
        instance.childNodes == CHILD_NODES
        instance.properties == null
        instance.serviceDescriptors == SERVICES
        !instance.isLeaf()
        //instance.icon
    }

    def "should create object from minimal dto"() {
        when:
        def instance = new GroupingNode([type: 'group'], new NodeFactory())

        then:
        instance.name == ''
        instance.description == ''
        instance.childNodes == []
        instance.serviceDescriptors == []
        instance.properties == null
        !instance.isLeaf()
        //instance.icon
    }

    def "should validate dto"() {
        when:
        def instance = new GroupingNode([type: 'x'], new NodeFactory())

        then:
        thrown IllegalArgumentException
    }

    def "should serialize and deserialize"() {
        given:
        def instance = new GroupingNode(NAME, DESCRIPTION, CHILD_NODES, SERVICES)

        when:
        def dto = instance.dtoProperties

        then:
        dto == sampleDto

        when:
        def newInstance = new GroupingNode(dto, new NodeFactory())

        then:
        newInstance == instance

        when:
        dto.type=NodeType.GROUP.typeName.toUpperCase()
        newInstance = new GroupingNode(dto, new NodeFactory())

        then:
        newInstance
    }

    def "should handle invalid services in dto"() {
        given:
        sampleDto[NetworkNode.SERVICES_PROPERTY] = 'not a list'

        when:
        def newInstance = new GroupingNode(sampleDto, new NodeFactory())

        then:
        newInstance == new GroupingNode(NAME, DESCRIPTION, CHILD_NODES, [])
    }

    def "should handle invalid chidlnodes in dto"() {
        given:
        sampleDto[NetworkNode.CHILD_NODES_PROPERTY] = [null, 'abc', 7, new Server('asdfasd').dtoProperties, 42]
        when:
        def newInstance = new GroupingNode(sampleDto, new NodeFactory())

        then:
        newInstance == new GroupingNode(NAME, DESCRIPTION, [new Server('asdfasd')], SERVICES)

        when:
        sampleDto[NetworkNode.CHILD_NODES_PROPERTY] = 7
        newInstance = new GroupingNode(sampleDto, new NodeFactory())

        then:
        newInstance == new GroupingNode(NAME, DESCRIPTION, [], SERVICES)
    }

}
