package net.in.rrrekin.ittoolbox.configuration

import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException
import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException
import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException
import net.in.rrrekin.ittoolbox.configuration.nodes.NodeConverter
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

  static final CONFIGURATION = new Configuration(SampleTestData.SAMPLE_NODES, SampleTestData.SAMPLE_MODULES)

  ServiceDefinition service1 = Mock() {
    getId() >> 'service1'; getConfiguration() >> 'service1 configuration'
  }
  ServiceDefinition service2 = Stub() {
    getId() >> 'service2'; getConfiguration() >> 'service2 configuration'
  }

  static final SAMPLE_CONFIG_FILE = './src/test/resources/sample_config.yml' as File

  ServiceRegistry serviceRegistry = Mock()
  NodeConverter nodeConverter = new NodeConverter(appPreferences)

  def service = new ConfigurationPersistenceService(serviceRegistry, nodeConverter)

  void setup() {
    LocaleUtil.setLocale(Locale.ENGLISH)
  }

  void cleanup() {
    // Reset locale to default
    LocaleUtil.setLocale(null)
  }

  def "should validate constructor arguments"() {
    when:
    new ConfigurationPersistenceService(null, nodeConverter)
    then:
    thrown NullPointerException

    when:
    new ConfigurationPersistenceService(serviceRegistry, null)
    then:
    thrown NullPointerException
  }

  def "should validate load arguments"() {
    when:
    service.load(null)
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

  def "should load file"() {
    when:
    def loadData = service.load(SAMPLE_CONFIG_FILE)

    then:
    1 * serviceRegistry.configureService('service1', 'service1 configuration')
    1 * serviceRegistry.configureService('service2', 'service2 configuration')
    0 * serviceRegistry._
    with(loadData) {
      configuration.modules == CONFIGURATION.modules
      configuration.filePath == SAMPLE_CONFIG_FILE.absoluteFile.toPath().normalize()
      TreeModelHelper.isEqual(configuration.networkNodes, CONFIGURATION.networkNodes)
      clean
      warnings.isEmpty()
    }

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
    1 * serviceRegistry.stream() >> { Stream.of(service1, service2) }
    0 * serviceRegistry._

    when:
    def loadData = service.load(file)

    then:
    1 * serviceRegistry.configureService('service1', 'service1 configuration')
    1 * serviceRegistry.configureService('service2', 'service2 configuration')
    0 * serviceRegistry._
    with(loadData) {
      configuration.modules == CONFIGURATION.modules
      configuration.filePath.toString() == file.toPath().toString()
      TreeModelHelper.isEqual(configuration.networkNodes, CONFIGURATION.networkNodes)
      clean
      warnings.isEmpty()
    }

    cleanup:
    file.delete()
  }

  def "should handle non existing file"() {
    when:
    service.load('non-existing-file.yml' as File)

    then:
    thrown MissingConfigurationException
  }

  def "should handle not-yaml file"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = 'dsrhjj cn34i542ku v342utsx54c 45p8954cnf25t8948t25vn458}}}\n\n}}*#@'

    when:
    service.load(file)

    then:
    thrown InvalidConfigurationException

    cleanup:
    file.delete()
  }

  def "should handle directory instead of file"() {
    File file = File.createTempDir(this.class.simpleName + '_', '.yml')
    file.mkdirs()

    when:
    service.load(file)

    then:
    thrown MissingConfigurationException

    cleanup:
    file.deleteDir()
  }

  def "should reject unknown configuration version"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = 'version: 0.3\n'

    when:
    service.load(file)

    then:
    thrown InvalidConfigurationException

    cleanup:
    file.delete()
  }

  def "should reject empty file"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = ''

    when:
    service.load(file)

    then:
    thrown InvalidConfigurationException

    cleanup:
    file.delete()
  }

  def "should reject server list that is not a list"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = 'version: "1.0"\nservers: abc'

    when:
    service.load(file)

    then:
    thrown InvalidConfigurationException

    when:
    file.text = 'version: "1.0"\nservers:\n  abc:  def\n'
    service.load(file)

    then:
    thrown InvalidConfigurationException

    cleanup:
    file.delete()
  }


  def "should reject invalid module config"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = 'version: "1.0"\nmodules:\n  not a map\n'

    when:
    def loadedData = service.load(file)

    then:
    with(loadedData) {
      configuration.modules.isEmpty()
      configuration.networkNodes.children.isEmpty()
      !clean
      warnings == ['Server list not present in configuration file.', 'Invalid configuration of modules.', 'Invalid services section in configuration file.']
    }

    when:
    file.text = 'version: "1.0"\nmodules:\n  ssh: not a map\n'
    loadedData = service.load(file)

    then:
    with(loadedData) {
      configuration.modules.isEmpty()
      configuration.networkNodes.children.isEmpty()
      !clean
      warnings == ['Server list not present in configuration file.', 'Invalid module configuration entry for module ssh: not a map.', 'Invalid services section in configuration file.']
    }

    cleanup:
    file.delete()
  }


  def "should reject invalid services config"() {
    File file = File.createTempFile(this.class.simpleName + '_', '.yml')
    file.text = 'version: "1.0"\nservices:\n  not a map\n'

    when:
    def loadedData = service.load(file)

    then:
    with(loadedData) {
      configuration.modules.isEmpty()
      configuration.networkNodes.children.isEmpty()
      !clean
      warnings == ['Server list not present in configuration file.', 'Invalid services section in configuration file.']
    }

    when:
    file.text = 'version: "1.0"\nservices:\n  ssh: options of the service\n  telnet: command:telnet\n'
    loadedData = service.load(file)

    then:
    1 * serviceRegistry.configureService('ssh', 'options of the service') >> { throw new IllegalArgumentException() }
    then:
    1 * serviceRegistry.configureService('telnet', 'command:telnet')
    0 * serviceRegistry._
    with(loadedData) {
      configuration.modules.isEmpty()
      configuration.networkNodes.children.isEmpty()
      !clean
      warnings == ['Server list not present in configuration file.', 'Invalid service ssh configuration: options of the service.']
    }

    cleanup:
    file.delete()
  }

  def "should handle various network node errors in file"() {
    when:
    def loadedData = service.load('src/test/resources/sample_config_with_errors.yml' as File)

    then:
    with(loadedData) {
      configuration.modules == CONFIGURATION.modules
      !configuration.networkNodes.children.isEmpty()
      !clean
      warnings == ['Invalid definition of node "children": not a list.', 'Invalid definition of node: Not a map.', 'Invalid definition of node: {type=Spaceship, name=s14, address=a1....']
    }
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
}
