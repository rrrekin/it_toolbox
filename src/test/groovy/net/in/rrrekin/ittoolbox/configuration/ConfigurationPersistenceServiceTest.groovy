package net.in.rrrekin.ittoolbox.configuration

import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException
import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode
import net.in.rrrekin.ittoolbox.configuration.nodes.GroupingNode
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeFactory
import net.in.rrrekin.ittoolbox.configuration.nodes.Server
import net.in.rrrekin.ittoolbox.services.ServiceDefinition
import net.in.rrrekin.ittoolbox.services.ServiceRegistry
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

import java.util.stream.Stream

/**
 * @author michal.rudewicz@gmail.com
 */
class ConfigurationPersistenceServiceTest extends Specification {
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
    static final SAMPLE_NODES_DTO = [
            [type: 'Server', name: 's1', address: 'a1', description: '2018年1月1日 星期一 下午03时20分34秒', services: []],
            [type: 'GenericNode', name: 's2', description: 'd2', services: ['vlan:1001'], _p1: 'vvv1'],
            [type: 'Server', name: 's3', address: 'a3', description: 'd3', services: ['ssh', 'telnet', 'ftp', 'http', 'https', 'actuator']],
            [type: 'Group', name: 'g4', description: 'zażółć gęślą jaźń', children: [
                    [type: 'Server', name: 's5', address: 'a5', description: 'd5', services: ['ssh']],
                    [type: 'Group', name: 'g8', description: 'd8', children: [
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
    static final SAMPLE_MODULES = [ssh: [terminal: 'true', command: 'ssh'] as HashMap, ping: [command: 'ping'] as HashMap]
    static final CONFIGURATION = new Configuration(SAMPLE_NODES, SAMPLE_MODULES)

    ServiceDefinition service1 = Mock() {
        getId() >> 'service1'; getConfiguration() >> 'service1 configuration'
    }
    ServiceDefinition service2 = Stub() {
        getId() >> 'service2'; getConfiguration() >> 'service2 configuration'
    }

    static final SAMPLE_CONFIG_FILE = 'src/test/resources/sample_config.yml' as File

    def ServiceRegistry serviceRegistry = Mock()
    def NodeFactory nodeFactory = Mock()

    def service = new ConfigurationPersistenceService(serviceRegistry, nodeFactory)

    void cleanup() {
        // Reset locale to default
        LocaleUtil.setLocale(null)
    }

    def "should validate constructor arguments"() {
        when:
        new ConfigurationPersistenceService(null, nodeFactory)
        then:
        thrown NullPointerException

        when:
        new ConfigurationPersistenceService(serviceRegistry, null)
        then:
        thrown NullPointerException
    }

    def "should validate load arguments"() {
        when:
        service.load(null, true)
        then:
        thrown NullPointerException
    }

    def "should validate save arguments"() {
        when:
        service.save(null, CONFIGURATION)
        then:
        thrown NullPointerException

        when:
        service.save('a' as File, null)
        then:
        thrown NullPointerException
    }

    def "should serialize and deserialize configuration"() {
        given:
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        def yaml = new Yaml()

        when:
        service.save(file, CONFIGURATION)
//        service.save('build/config_sample.yml' as File, CONFIGURATION)

        then:
        yaml.load(file.text) == yaml.load(SAMPLE_CONFIG_FILE.text)
        1 * serviceRegistry.stream() >> Stream.of(service1, service2)
        0 * serviceRegistry._
        0 * nodeFactory._

        when:
        def configuration = service.load(file, false)

        then:
        1 * nodeFactory.createFrom(SAMPLE_NODES_DTO) >> SAMPLE_NODES
        1 * serviceRegistry.configureService('service1', 'service1 configuration')
        1 * serviceRegistry.configureService('service2', 'service2 configuration')
        0 * serviceRegistry._
        0 * nodeFactory._
        configuration.networkNodes == SAMPLE_NODES
        configuration.modules == SAMPLE_MODULES
        configuration == CONFIGURATION

        cleanup:
        file.delete()
    }

    def "should handle non existing file"() {
        when:
        service.load('non-existing-file.yml' as File, false)

        then:
        thrown MissingConfigurationException
    }

    def "should handle not-yaml file"() {
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        file.text = 'dsrhjj cn34i542ku v342utsx54c 45p8954cnf25t8948t25vn458}}}\n\n}}*#@'

        when:
        service.load(file, false)

        then:
        thrown InvalidConfigurationException

        cleanup:
        file.delete()
    }

    def "should handle directory instead of file"() {
        File file = File.createTempDir(this.class.simpleName + '_', '.yml')
        file.mkdirs()

        when:
        service.load(file, false)

        then:
        thrown MissingConfigurationException

        cleanup:
        file.deleteDir()
    }

    def "should reject unknown configuration version"() {
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        file.text = 'version: 0.3\n'

        when:
        service.load(file, false)

        then:
        thrown InvalidConfigurationException

        cleanup:
        file.delete()
    }

    def "should reject empty file"() {
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        file.text = ''

        when:
        service.load(file, false)

        then:
        thrown InvalidConfigurationException

        cleanup:
        file.delete()
    }

    def "should reject server list that is not a list"() {
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        file.text = 'version: "1.0"\nservers: abc'

        when:
        def config = service.load(file, false)

        then:
        config.getNetworkNodes().isEmpty()

        when:
        file.text = 'version: "1.0"\nservers:\n  abc:  def\n'
        config = service.load(file, false)

        then:
        config.getNetworkNodes().isEmpty()

        cleanup:
        file.delete()
    }


    def "should reject invalid module config"() {
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')
        file.text = 'version: "1.0"\nmodules:\n  not a map\n'

        when:
        def config = service.load(file, false)

        then:
        config.modules.isEmpty()

        when:
        file.text = 'version: "1.0"\nmodules:\n  ssh: not a map\n'
        config = service.load(file, false)

        then:
        config.modules == [ssh:[:]]

        cleanup:
        file.delete()
    }



    def "should handle write errors"() {
        File file = File.createTempDir(this.class.simpleName + '_', '.yml')
        file.mkdirs()

        when:
        service.save(file, CONFIGURATION)

        then:
        thrown FailedConfigurationSaveException

        cleanup:
        file.deleteDir()
    }

    def "should save and read locale"() {
        given:
        LocaleUtil.setLocale(Locale.CHINESE)
        File file = File.createTempFile(this.class.simpleName + '_', '.yml')

        expect:
        LocaleUtil.localeCode == 'zh'
        Locale.getDefault() == Locale.CHINESE

        when:
        service.save(file, CONFIGURATION)
        LocaleUtil.setLocale(null)

        then:
        1 * serviceRegistry.stream() >> Stream.of(service1, service2)
        LocaleUtil.localeCode == null
        Locale.getDefault() == LocaleUtil.@systemLocale

        when: "read config file"
        def newConfig = service.load(file, false)

        then:
        1 * nodeFactory.createFrom(SAMPLE_NODES_DTO) >> SAMPLE_NODES
        LocaleUtil.localeCode == 'zh'
        Locale.getDefault() == Locale.CHINESE
        new Yaml().load(file.text).locale == 'zh'

        cleanup:
        file.delete()
    }
}
