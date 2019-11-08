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
        node.clazz == clazz
        node.typeName == typeName
        NodeType.of(clazz) == node
        NodeType.of(typeName) == node

        where:
        node                  | clazz        | typeName
        NodeType.GROUP        | GroupingNode | 'Group'
        NodeType.SERVER       | Server       | 'Server'
        NodeType.GENERIC_NODE | GenericNode  | 'GenericNode'
    }

    def "should return null for unknown class and typeName"() {
        expect:
        NodeType.of(String) == null
        NodeType.of('Bomb') == null
        NodeType.class.getMethod('of', String).invoke(null, [null] as Object[]) == null
        NodeType.class.getMethod('of', Class).invoke(null, [null] as Object[]) == null
    }
}
