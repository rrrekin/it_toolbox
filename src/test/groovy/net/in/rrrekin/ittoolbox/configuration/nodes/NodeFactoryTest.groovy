package net.in.rrrekin.ittoolbox.configuration.nodes

import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class NodeFactoryTest extends Specification {

    static final CONFIG = new Yaml().load(('src/test/resources/sample_config.yml' as File).text)
    static final SAMPLE_NODES = [
            new Server('s1', 'a1', '2018年1月1日 星期一 下午03时20分34秒', [:], []),
            new GenericNode('s2', 'd2', [p1: 'vvv1'], ['vlan:1001']),
            new Server('s3', 'a3', 'd3', [:], ['ssh', 'telnet', 'ftp', 'http', 'https', 'actuator']),
            new GroupingNode('g4', 'zażółć gęślą jaźń', [
                    new Server('s5', 'a5', 'd5', [:], ['ssh']),
                    new GroupingNode('g8', 'd8', [
                            new Server('s9', 'a9', 'd9', [:], ['https']),
                            new GenericNode('s10', 'd10', [p2: 'vvv2', p3: 'vvv4'], ['cmd:cluster-restart']),
                            new Server('s11', 'a11', 'd11', [:], ['rest:8080:GET:/api/v1/abc']),
                    ], []),
                    new GenericNode('s6', 'd6', [:], []),
                    new Server('s7', 'a7', 'd7', [:], ['ftp']),
            ], ['docker-compose:/path/to/dir']),
            new Server('s12', 'a12', 'd12', [:], ['https:9000']),
            new GenericNode('s13', 'd13', [:], ['http:8080']),
            new Server('s14', 'a14', 'd14', [:], ['ftp', 'telnet']),
    ]

    def instance = new NodeFactory()

    def "should create nodes"() {
        when:
        def nodes = instance.createFrom(CONFIG.servers)

        then:
        nodes == SAMPLE_NODES
    }

    def "should handle invalid elements on node list"() {
        when:
        def nodes = instance.createFrom([2, new Server('42').dtoProperties, "abc", [abc: 67]])

        then:
        nodes == [new Server('42')]
    }

    def "should handle invalid type"() {
        when:
        def nodes = instance.createFrom([type:'abc'])

        then:
        thrown InvalidConfigurationException
    }

}
