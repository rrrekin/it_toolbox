package net.in.rrrekin.ittoolbox.gui.commands

import javafx.scene.control.TreeItem
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class DeleteNodesCommandTest extends Specification {


  def "should properly sort ItemInfo lists First shortest paths, on the same level lower index first"() {
    setup:
    List<DeleteNodesCommand.ItemInfo> itemInfod = [
      new DeleteNodesCommand.ItemInfo([5] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([3, 5] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([3, 3] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([2, 4, 5] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([3, 1, 4] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([3, 1, 2, 6] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([1] as int[], Stub(TreeItem.class)),
      new DeleteNodesCommand.ItemInfo([3, 4, 2] as int[], Stub(TreeItem.class)),
    ]

    expect:
    itemInfod.toString() == '[5, 3/5, 3/3, 2/4/5, 3/1/4, 3/1/2/6, 1, 3/4/2]'

    when:
    itemInfod.sort(null)

    then:
    itemInfod.toString() == '[1, 5, 3/3, 3/5, 2/4/5, 3/1/4, 3/4/2, 3/1/2/6]'

  }
}
