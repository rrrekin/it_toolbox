package net.in.rrrekin.ittoolbox.configuration.nodes

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class NodeTypeTest extends Specification {

    @Unroll
    def "should define #node"() {
        expect:
        NodeType.values().size() == 3

        and:
        node.typeName == typeName
        NodeType.of(typeName) == node

        and: "creates type instance"
        node.create([type: typeName], '', Stub(NodeFactory)).class == clazz

        where:
        node                  | clazz        | typeName
        NodeType.GROUP        | GroupingNode | 'Group'
        NodeType.SERVER       | Server       | 'Server'
        NodeType.GENERIC_NODE | GenericNode  | 'GenericNode'
    }

    def "should return null for unknown class and typeName"() {
        expect:
        NodeType.of('Bomb') == null
        NodeType.of(null) == null
    }
}
