package net.in.rrrekin.ittoolbox.configuration

import groovy.util.logging.Slf4j
import javafx.scene.control.TreeItem
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode

/**
 * @author michal.rudewicz@gmail.com
 */
@Slf4j
class TreeModelHelper {

  static boolean isEqual(TreeItem<NetworkNode> item1, TreeItem<NetworkNode> item2) {
    if (item1.value != item2.value) {
      log.info("$item1.value != $item2.value")
      return false
    }
    if (item1.children.size() != item2.children.size()) {
      log.info("List size ${item1.children.size()} != ${item2.children.size()}")
      return false
    }
    for (int i = 0; i < item1.children.size(); i++) {
      if (!isEqual(item1.children[i], item2.children[i])) return false
    }
    return true
  }
}
