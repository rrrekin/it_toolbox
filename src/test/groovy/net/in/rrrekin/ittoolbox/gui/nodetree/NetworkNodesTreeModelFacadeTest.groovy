//package net.in.rrrekin.ittoolbox.gui.nodetree
//
//import com.google.common.eventbus.EventBus
//import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons
//import jiconfont.swing.IconFontSwing
//import net.in.rrrekin.ittoolbox.configuration.Configuration
//import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager
//import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode
//import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode
//import net.in.rrrekin.ittoolbox.configuration.nodes.Server
//import net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent
//import net.in.rrrekin.ittoolbox.gui.GuiInvokeService
//import spock.lang.Specification
//import spock.lang.Stepwise
//import spock.lang.Unroll
//
//import javax.swing.event.TreeModelEvent
//import javax.swing.event.TreeModelListener
//import javax.swing.tree.TreePath
//
//import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.*
//
///**
// * @author michal.rudewicz@gmail.com
// */
//
//@Stepwise
//class NetworkNodesTreeModelFacadeTest extends Specification {
//
//  static final SAMPLE_NODES = [
//    new Server('s1', 'a1', '2018年1月1日 星期一 下午03时20分34秒', [:], []),
//    new GenericNode('s2', 'd2', [p1: 'vvv1'], ['vlan:1001']),
//    new Server('s3', 'a3', 'd3', [:], ['ssh', 'telnet', 'ftp', 'http', 'https', 'actuator']),
//    new GroupingNode('g4', 'zażółć gęślą jaźń', [
//      new Server('s5', 'a5', 'd5', [:], ['ssh']),
//      new GroupingNode('g8', 'd8', [
//        new Server('s9', 'a9', 'd9', [:], ['https']),
//        new GenericNode('s10', 'd10', [p2: 'vvv2', p3: 'vvv4'], ['cmd:cluster-restart']),
//        new Server('s11', 'a11', 'd11', [:], ['rest:8080:GET:/api/v1/abc']),
//      ], []),
//      new GenericNode('s6', 'd6', [:], []),
//      new Server('s7', 'a7', 'd7', [:], ['ftp']),
//    ], ['docker-compose:/path/to/dir']),
//    new Server('s12', 'a12', 'd12', [:], ['https:9000']),
//    new GenericNode('s13', 'd13', [:], ['http:8080']),
//    new Server('s14', 'a14', 'd14', [:], ['ftp', 'telnet']),
//  ]
//  static final CONFIGURATION = new Configuration(SAMPLE_NODES, [:])
//
//  ConfigurationManager configurationManager = Mock() {
//    getConfig() >> CONFIGURATION
//  }
//  EventBus eventBus = Mock()
//  GuiInvokeService invokeService = Mock() {
//    runInGui(_) >> { args -> args[0].run() }
//    runInEdtAndWait(_) >> { args -> args[0].run() }
//  }
//  TreeModelListener listener = Mock()
//  TreeModelListener listener2 = Mock()
//  TreeModelListener listener3 = Mock()
//
//  def instance = new NetworkNodesTreeModelFacade(configurationManager, eventBus, invokeService)
//
//  void setupSpec() {
//    IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
//  }
//
//  def "should properly build instance"() {
//    expect:
//    instance.@configurationManager.is configurationManager
//    instance.@eventBus.is eventBus
//
//    when:
//    def root = instance.getRoot()
//
//    then:
//    root instanceof GroupingNode
//    (root as GroupingNode).childNodes.isEmpty()
//  }
//
//  def "should validate constructor arguments"() {
//    when:
//    new NetworkNodesTreeModelFacade(null, eventBus, invokeService)
//    then:
//    thrown NullPointerException
//
//    when:
//    new NetworkNodesTreeModelFacade(configurationManager, null, invokeService)
//    then:
//    thrown NullPointerException
//    when:
//    new NetworkNodesTreeModelFacade(configurationManager, eventBus, null)
//    then:
//    thrown NullPointerException
//  }
//
//  def "should initialize"() {
//    when:
//    instance.init()
//
//    then:
//    1 * eventBus.register(instance)
//    0 * _._
//  }
//
//  @Unroll
//  def "should populate model on config load notification and notify listeners in reversed registration order"() {
//    setup:
//    instance.addTreeModelListener(listener)
//    instance.addTreeModelListener(listener2)
//    instance.addTreeModelListener(listener3)
//    def rootPath = new TreePath(instance.getRoot())
//
//    when:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(code, ''))
//
//    then:
//    1 * listener3.treeStructureChanged({ TreeModelEvent event -> event.source == instance && event.treePath == rootPath && event.childIndices.length == 0 && event.children == null || event.children.length == 0 })
//    then:
//    1 * listener2.treeStructureChanged({ TreeModelEvent event -> event.source == instance && event.treePath == rootPath && event.childIndices.length == 0 && event.children == null || event.children.length == 0 })
//    then:
//    1 * listener.treeStructureChanged({ TreeModelEvent event -> event.source == instance && event.treePath == rootPath && event.childIndices.length == 0 && event.children == null || event.children.length == 0 })
//    0 * listener._
//    0 * listener2._
//    0 * listener3._
//    and:
//    instance.getRoot().getChildNodes() == SAMPLE_NODES
//
//    where:
//    code | _
//    NEW  | _
//    OK   | _
//  }
//
//  @Unroll
//  def "should notify changes in particular node"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//    instance.addTreeModelListener(listener)
//    path.add(0, instance.getRoot())
//    def effectivePath = new TreePath(path as Object[])
//
//    when:
//    instance.nodeChanged(child)
//
//    then:
//    1 * configurationManager.setDirty(true)
//    1 * listener.treeNodesChanged({ TreeModelEvent event -> event.source == instance && event.treePath == effectivePath && event.childIndices == ([childIndex] as int[]) && event.children == ([child] as Object[]) })
//    0 * _._
//
//    where:
//    path                                             | childIndex || child
//    []                                               | 0          || SAMPLE_NODES[0]
//    []                                               | 3          || SAMPLE_NODES[3]
//    [SAMPLE_NODES[3]]                                | 2          || SAMPLE_NODES[3].childNodes[2]
//    [SAMPLE_NODES[3], SAMPLE_NODES[3].childNodes[1]] | 1          || SAMPLE_NODES[3].childNodes[1].childNodes[1]
//  }
//
//  def "should not notify for invalid nodes"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//    instance.addTreeModelListener(listener)
//
//    when:
//    instance.nodeChanged(new Server('sss'))
//
//    then:
//    0 * _._
//  }
//
//  def "should not notify for invalid paths"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//    instance.addTreeModelListener(listener)
//
//    when:
//    instance.nodeChanged(new TreePath(['', new Server('sss')] as Object[]))
//
//    then:
//    0 * _._
//
//    when:
//    instance.nodeChanged(new TreePath(['', ''] as Object[]))
//
//    then:
//    0 * _._
//
//    when:
//    instance.nodeChanged(new TreePath([new Server('sss'), ''] as Object[]))
//
//    then:
//    0 * _._
//  }
//
//  def "should notify for rootPath"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//    instance.addTreeModelListener(listener)
//
//    when:
//    instance.nodeChanged(new TreePath(instance.getRoot()))
//
//    then:
//    1 * configurationManager.setDirty(true)
//    1 * listener.treeNodesChanged({ TreeModelEvent event -> event.source == instance && event.treePath == null && event.childIndices == ([0] as int[]) && event.children == ([instance.getRoot()] as Object[]) })
//      0 * _._
//  }
//
//  def "should add and remove listeners"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//
//    when:
//    instance.nodeChanged(SAMPLE_NODES[2])
//    then:
//    1 * configurationManager.setDirty(true)
//    0 * _._
//
//    when: "add listeners"
//    instance.addTreeModelListener(listener)
//    instance.addTreeModelListener(listener2)
//    instance.addTreeModelListener(listener3)
//    instance.nodeChanged(SAMPLE_NODES[2])
//    then:
//    1 * configurationManager.setDirty(true)
//    1 * listener3.treeNodesChanged(_)
//    then:
//    1 * listener2.treeNodesChanged(_)
//    then:
//    1 * listener.treeNodesChanged(_)
//    0 * _._
//
//    when: "removed listener from the middle"
//    instance.removeTreeModelListener(listener2)
//    instance.nodeChanged(SAMPLE_NODES[2])
//    then:
//    1 * configurationManager.setDirty(true)
//    1 * listener3.treeNodesChanged(_)
//    then:
//    1 * listener.treeNodesChanged(_)
//    0 * _._
//
//    when: "removed rest of listeners"
//    instance.removeTreeModelListener(listener)
//    instance.removeTreeModelListener(listener3)
//    instance.nodeChanged(SAMPLE_NODES[2])
//    then:
//    1 * configurationManager.setDirty(true)
//    0 * _._
//  }
//
//  @Unroll
//  def "should do nothing on failure notification"() {
//    setup:
//    instance.addTreeModelListener(listener)
//
//    when:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(code, ''))
//
//    then:
//    instance.getRoot().getChildNodes().isEmpty()
//    0 * _._
//
//    where:
//    code    | _
//    MISSING | _
//    FAILED  | _
//    SAVED   | _
//  }
//
//  @Unroll
//  def "should return child node"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//
//    expect:
//    instance.getChild(parent == null ? instance.getRoot() : parent, index) == child
//
//    where:
//    parent                        | index || child
//    null                          | 0      | SAMPLE_NODES[0]
//    null                          | 3      | SAMPLE_NODES[3]
//    SAMPLE_NODES[3]               | 2      | SAMPLE_NODES[3].childNodes[2]
//    SAMPLE_NODES[3]               | -2     | '?'
//    SAMPLE_NODES[3]               | 7      | '?'
//    SAMPLE_NODES[3].childNodes[1] | 1      | SAMPLE_NODES[3].childNodes[1].childNodes[1]
//    SAMPLE_NODES[3].childNodes[1] | 3      | '?'
//    SAMPLE_NODES[2]               | 0      | '?'
//    'SAMPLE_NODES[3]'             | 2      | '?'
//  }
//
//  @Unroll
//  def "should return node child count and leaf status"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//
//    expect:
//    instance.getChildCount(parent == null ? instance.getRoot() : parent) == count
//    instance.isLeaf(parent == null ? instance.getRoot() : parent) == isLeaf
//
//    where:
//    parent                        | count | isLeaf
//    null                          | 7     | false
//    SAMPLE_NODES[3]               | 4     | false
//    SAMPLE_NODES[3].childNodes[1] | 3     | false
//    SAMPLE_NODES[2]               | 0     | true
//    SAMPLE_NODES[1]               | 0     | true
//    SAMPLE_NODES[0]               | 0     | true
//    'SAMPLE_NODES[3]'             | 0     | true
//  }
//
//  def "should find child index"() {
//    setup:
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//
//    expect:
//    instance.getIndexOfChild(instance.getRoot(), SAMPLE_NODES[3]) == 3
//    instance.getIndexOfChild(instance.getRoot(), SAMPLE_NODES[1]) == 1
//    instance.getIndexOfChild(instance.getRoot(), SAMPLE_NODES[3].childNodes[1]) == -1
//    instance.getIndexOfChild(instance.getRoot(), SAMPLE_NODES[3].childNodes[1].childNodes[2]) == -1
//    instance.getIndexOfChild(SAMPLE_NODES[3], SAMPLE_NODES[3]) == -1
//    instance.getIndexOfChild(SAMPLE_NODES[3], SAMPLE_NODES[3].childNodes[1]) == 1
//    instance.getIndexOfChild(SAMPLE_NODES[3], SAMPLE_NODES[3].childNodes[1].childNodes[2]) == -1
//    instance.getIndexOfChild(SAMPLE_NODES[3].childNodes[1], SAMPLE_NODES[3].childNodes[1].childNodes[2]) == 2
//    instance.getIndexOfChild('aa', SAMPLE_NODES[3].childNodes[1].childNodes[2]) == -1
//    instance.getIndexOfChild('aa', 'ss') == -1
//    instance.getIndexOfChild(SAMPLE_NODES[3], 'ss') == -1
//  }
//
//  def "should not notify if changed object is not a NetworkNode"() {
//    setup:
//    instance.addTreeModelListener(listener)
//
//    when:
//    instance.valueForPathChanged(new TreePath([instance.getRoot(), 'not a node'] as Object[]), 'val')
//    then:
//    0 * _._
//  }
//
//  // Keep this test at the end, as it modifies common configuration
//  @Unroll
//  def "should notify on node value change"() {
//    setup:
//    instance.addTreeModelListener(listener)
//    instance.addTreeModelListener(listener2)
//    instance.addTreeModelListener(listener3)
//    instance.handleFileSynchronizationEvents(new ConfigurationFileSyncEvent(NEW, ''))
//    def fullPath = parentPath as List
//    fullPath.add(0, instance.getRoot())
//    def effectiveParentPath = new TreePath(fullPath as Object[])
//    def newNodeName = 'NEW NODE NAME'
//    def child = effectiveParentPath.lastPathComponent.childNodes[childIndex]
//
//    expect:
//    child.name != newNodeName
//
//    when:
//    instance.valueForPathChanged(effectiveParentPath.pathByAddingChild(child), newNodeName)
//
//    then:
//    1 * listener3.treeNodesChanged({ TreeModelEvent event -> event.source == instance && event.treePath == effectiveParentPath && event.childIndices == ([childIndex] as int[]) && event.children == ([child] as Object[]) })
//    then:
//    1 * listener2.treeNodesChanged({ TreeModelEvent event -> event.source == instance && event.treePath == effectiveParentPath && event.childIndices == ([childIndex] as int[]) && event.children == ([child] as Object[]) })
//    then:
//    1 * listener.treeNodesChanged({ TreeModelEvent event -> event.source == instance && event.treePath == effectiveParentPath && event.childIndices == ([childIndex] as int[]) && event.children == ([child] as Object[]) })
//    0 * listener._
//    0 * listener2._
//    0 * listener3._
//    and:
//    child.name == newNodeName
//
//    where:
//    parentPath                                       | childIndex
//    []                                               | 3
//    []                                               | 0
//    []                                               | 1
//    []                                               | 5
//    [SAMPLE_NODES[3]]                                | 1
//    [SAMPLE_NODES[3]]                                | 2
//    [SAMPLE_NODES[3], SAMPLE_NODES[3].childNodes[1]] | 2
//  }
//}
