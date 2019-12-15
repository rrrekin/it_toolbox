package net.in.rrrekin.ittoolbox.configuration

import javafx.scene.control.TreeItem
import javafx.scene.paint.Color
import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode
import net.in.rrrekin.ittoolbox.configuration.nodes.NetworkNode
import net.in.rrrekin.ittoolbox.configuration.nodes.Server
import org.controlsfx.glyphfont.FontAwesome

/**
 * @author michal.rudewicz@gmail.com
 */
class SampleTestData {

  static final ICON1 = new IconDescriptor(FontAwesome.Glyph.GROUP, Color.BEIGE, true)
  static final ICON2 = new IconDescriptor(FontAwesome.Glyph.SERVER, Color.BLUE, true)
  static final ICON3 = new IconDescriptor(FontAwesome.Glyph.FOLDER, Color.RED, false)
  static final TreeItem<NetworkNode> SAMPLE_NODES = new TreeItem<>(new GroupingNode('root')).with {
    it.children.addAll([
      new TreeItem<>(new Server('s1', 'a1', '2018年1月1日 星期一 下午03时20分34秒', ICON2, [:], [])),
      new TreeItem<>(new GenericNode('s2', 'd2', ICON1, [p1: 'vvv1'], ['vlan:1001'])),
      new TreeItem<>(new Server('s3', 'a3', 'd3', ICON2, [:], ['ssh', 'telnet', 'ftp', 'http', 'https', 'actuator'])),
      new TreeItem<>(new GroupingNode('g4', 'zażółć gęślą jaźń', ICON3, ['exec:report ${name}', 'docker-compose:/path/to/dir'])).with {
        it.children.addAll([
          new TreeItem<>(new Server('s5', 'a5', 'd5', ICON2, [:], ['ssh'])),
          new TreeItem<>(new GroupingNode('g8', 'd8', ICON3, [])).with {
            it.children.addAll([
              new TreeItem<>(new Server('s9', 'a9', 'd9', ICON2, [:], ['https'])),
              new TreeItem<>(new GenericNode('s10', 'd10', ICON3, [p2: 'vvv2', p3: 'vvv4'], ['cmd:cluster-restart'])),
              new TreeItem<>(new Server('s11', 'a11', 'd11', ICON2, [:], ['rest:8080:GET:/api/v1/abc'])),
            ])
            it
          },
          new TreeItem<>(new GenericNode('s6', 'd6', ICON2, [:], [])),
          new TreeItem<>(new Server('s7', 'a7', 'd7', ICON1, [:], ['ftp'])),
        ])
        it
      },
      new TreeItem<>(new Server('s12', 'a12', 'd12', ICON1, [:], ['https:9000'])),
      new TreeItem<>(new GenericNode('s13', 'd13', ICON1, [:], ['http:8080'])),
      new TreeItem<>(new Server('s14', 'a14', 'd14', ICON2, [p1: 'asdfasd,', p2: '54235'], ['ftp', 'telnet'])),
    ])
    it
  }

  static final SAMPLE_NODES_DTO = [
    [type: 'Server', name: 's1', address: 'a1', description: '2018年1月1日 星期一 下午03时20分34秒', services: []],
    [type: 'GenericNode', name: 's2', description: 'd2', services: ['vlan:1001'], _p1: 'vvv1'],
    [type: 'Server', name: 's3', address: 'a3', description: 'd3', services: ['ssh', 'telnet', 'ftp', 'http', 'https', 'actuator']],
    [type      : 'Group', name: 'g4', description: 'zażółć gęślą jaźń', children: [
      [type: 'Server', name: 's5', address: 'a5', description: 'd5', services: ['ssh']],
      [type      : 'Group', name: 'g8', description: 'd8', children: [
        [type: 'Server', name: 's9', address: 'a9', description: 'd9', services: ['https']],
        [type: 'GenericNode', name: 's10', description: 'd10', services: ['cmd:cluster-restart'], '_p2': 'vvv2', '_p3': 'vvv4'],
        [type: 'Server', name: 's11', address: 'a11', description: 'd11', services: ['rest:8080:GET:/api/v1/abc']]
      ], services: []],
      [type: 'GenericNode', name: 's6', description: 'd6', services: []],
      [type: 'Server', name: 's7', address: 'a7', description: 'd7', services: ['ftp']]
    ], services: ['docker-compose:/path/to/dir']],
    [type: 'Server', name: 's12', address: 'a12', description: 'd12', services: ['https:9000']],
    [type: 'GenericNode', name: 's13', description: 'd13', services: ['http:8080']],
    [type: 'Server', name: 's14', address: 'a14', description: 'd14', services: ['ftp', 'telnet']]
  ]
  static final Map<String, Map<String, Object>> SAMPLE_MODULES = [ssh: [terminal: 'true', command: 'ssh'] as HashMap, ping: [command: 'ping'] as HashMap]

}
