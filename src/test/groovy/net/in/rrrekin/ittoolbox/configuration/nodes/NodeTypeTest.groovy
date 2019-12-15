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
    NodeType.of(ccTypeName) == node

    where:
    node                  | typeName      | ccTypeName
    NodeType.GROUP        | 'Group'       | 'GrOUp'
    NodeType.SERVER       | 'Server'      | 'server'
    NodeType.GENERIC_NODE | 'GenericNode' | 'GeneRicNode'
  }

  def "should return null for unknown class and typeName"() {
    expect:
    NodeType.of('Bomb') == null
    NodeType.of(null) == null
  }
}
