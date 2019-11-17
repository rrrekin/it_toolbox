package net.in.rrrekin.ittoolbox.configuration.nodes

/**
 * @author michal.rudewicz@gmail.com
 */
trait NetworkNodeCompareTrait {
  static boolean equal(Server s1, Server s2) {
    return s1.name == s2.name &&
      s1.address == s2.address &&
      s1.description == s2.description &&
      s1.properties == s2.properties &&
      s1.serviceDescriptors == s2.serviceDescriptors
  }

  static boolean equal(GenericNode g1, GenericNode g2) {
    return g1.name == g2.name &&
      g1.description == g2.description &&
      g1.properties == g2.properties &&
      g1.serviceDescriptors == g2.serviceDescriptors
  }

  static boolean equal(GroupingNode g1, GroupingNode g2) {
    return g1.name == g2.name &&
      g1.description == g2.description &&
      equal(g1.childNodes, g2.childNodes) &&
      g1.serviceDescriptors == g2.serviceDescriptors
  }

  static boolean equal(Collection<NetworkNode> c1, Collection<NetworkNode> c2) {
    if (c1.size() != c2.size()) return false
    for (int i = 0; i < c1.size(); i++) {
      def element1 = c1[i]
      def element2 = c2[i]
      if (element1.class != element2.class) return false

      if (element1.class == Server && !equal(element1 as Server, element2 as Server)) return false

      if (element1.class == GenericNode && !equal(element1 as GenericNode, element2 as GenericNode)) return false

      if (element1.class == GroupingNode && !equal(element1 as GroupingNode, element2 as GroupingNode)) return false
    }
    return true
  }

}
