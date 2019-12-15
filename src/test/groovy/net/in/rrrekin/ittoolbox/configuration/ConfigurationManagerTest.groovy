//package net.in.rrrekin.ittoolbox.configuration
//
//import com.google.common.eventbus.EventBus
//import net.in.rrrekin.ittoolbox.configuration.exceptions.FailedConfigurationSaveException
//import net.in.rrrekin.ittoolbox.configuration.exceptions.InvalidConfigurationException
//import net.in.rrrekin.ittoolbox.configuration.exceptions.MissingConfigurationException
//import net.in.rrrekin.ittoolbox.configuration.nodes.GenericNode
//import net.in.rrrekin.ittoolbox.configuration.nodes.Server
//import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent
//import net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent
//import net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent
//import net.in.rrrekin.ittoolbox.utilities.ErrorCode
//import org.junit.Rule
//import org.junit.rules.TemporaryFolder
//import spock.lang.Specification
//
//import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_SERVICES_SECTION
//import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.INVALID_SERVICE_CONFIGURATION
//import static net.in.rrrekin.ittoolbox.events.ConfigurationFileSyncEvent.Code.*
//
///**
// * @author michal.rudewicz@gmail.com
// */
//class ConfigurationManagerTest extends Specification {
//
//    static final CONFIG = new Configuration([new Server('s1'), new GenericNode('g1'), new Server('s2')], [a: [b: 'c']])
//    static final CONFIG_2 = new Configuration([new Server('s3'), new GenericNode('g2'), new Server('s4')], [d: [e: 'f']])
//    static final ERROR_1 = new ConfigurationErrorEvent(INVALID_SERVICES_SECTION, 'err')
//    static final ERROR_2 = new ConfigurationErrorEvent(INVALID_SERVICE_CONFIGURATION, 'err2')
//
//    @Rule
//    TemporaryFolder temporaryFolder
//
//    File appDirectory
//    File configFile
//    EventBus eventBus = Mock()
//    ConfigurationPersistenceService persistenceService = Mock()
//
//    ConfigurationManager instance
//
//    void setup() {
//        appDirectory = temporaryFolder.root
//        instance = new ConfigurationManager(eventBus, persistenceService, appDirectory)
//        configFile = new File(appDirectory, 'it_toolbox-config.yml')
//        instance.dirty = true
//    }
//
//    void cleanup() {
//        instance.shutdown()
//    }
//
//    def "instance is properly build"() {
//        expect:
//        instance.@eventBus.is eventBus
//        instance.@persistenceService.is persistenceService
//        instance.configurationFile == configFile
//    }
//
//    def "should verify constructor arguments"() {
//        when:
//        new ConfigurationManager(null, persistenceService, appDirectory)
//        then:
//        thrown NullPointerException
//
//        when:
//        new ConfigurationManager(eventBus, null, appDirectory)
//        then:
//        thrown NullPointerException
//
//        when:
//        new ConfigurationManager(eventBus, persistenceService, null)
//        then:
//        thrown NullPointerException
//    }
//
//    def "should be properly initialized and terminated"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.init()
//
//        then:
//        instance.isActive()
//        instance.@configChangeTimer.queue.size() == 1
//        instance.@configChangeTimer.queue.getMin() instanceof ConfigSyncTask
//        instance.@configChangeTimer.thread.newTasksMayBeScheduled
//        1 * eventBus.register(instance)
//        1 * persistenceService.load(configFile) >> CONFIG
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == OK })
//        0 * _._
//
//        when:
//        instance.shutdown()
//
//        then:
//        !instance.isActive()
//        instance.@configChangeTimer.@queue.size() == 0
//        !instance.@configChangeTimer.@thread.newTasksMayBeScheduled
//        1 * eventBus.unregister(instance)
//        0 * _._
//    }
//
//    def "should record incoming config file errors"() {
//        expect:
//        instance.loadErrors.isEmpty()
//
//        when:
//        instance.handleConfigurationReadErrors(ERROR_1)
//        instance.handleConfigurationReadErrors(ERROR_2)
//
//        then:
//        instance.loadErrors == [ERROR_1, ERROR_2]
//        0 * _._
//    }
//
//    def "should not load configuration if file do not exist"() {
//        setup:
//        instance.init()
//        instance.handleConfigurationReadErrors(ERROR_2)
//
//        when:
//        instance.load()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.loadErrors == [ERROR_2]
//        instance.dirty
//        0 * _._
//
//        when:
//        instance.loadIfChanged()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.loadErrors == [ERROR_2]
//        instance.dirty
//        0 * _._
//    }
//
//    def "should load configuration and not reload if not newer"() {
//        setup:
//        instance.init()
//        instance.handleConfigurationReadErrors(ERROR_2)
//        configFile.text = '--- '
//        configFile.setLastModified(System.currentTimeMillis() - 500000)
//
//        expect:
//        instance.lastLoadedChangeTs == 0
//
//        when: "loaded"
//        instance.load()
//
//        then:
//        instance.config.is CONFIG
//        instance.lastLoadedChangeTs == configFile.lastModified()
//        !instance.dirty
//        1 * persistenceService.load(configFile) >> CONFIG
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == OK })
//        0 * _._
//
//        when: "not re-loaded"
//        instance.dirty = true
//        instance.loadIfChanged()
//
//        then:
//        instance.config.is CONFIG
//        instance.lastLoadedChangeTs == configFile.lastModified()
//        instance.dirty
//        0 * _._
//
//        when: "loaded anyway"
//        instance.load()
//
//        then:
//        instance.config.is CONFIG_2
//        instance.lastLoadedChangeTs == configFile.lastModified()
//        !instance.dirty
//        1 * persistenceService.load(configFile) >> CONFIG_2
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == OK })
//        0 * _._
//
//        when: "not reloaded"
//        instance.dirty = true
//        instance.loadIfChanged()
//
//        then:
//        instance.config.is CONFIG_2
//        instance.dirty
//        0 * _._
//
//        when: "reloaded"
//        configFile.setLastModified(System.currentTimeMillis())
//        instance.loadIfChanged()
//
//        then:
//        instance.config.is CONFIG
//        instance.lastLoadedChangeTs == configFile.lastModified()
//        !instance.dirty
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == OK })
//        1 * persistenceService.load(configFile) >> CONFIG
//        0 * _._
//
//        when: "not reloaded"
//        instance.dirty = true
//        instance.loadIfChanged()
//
//        then:
//        instance.config.is CONFIG
//        instance.dirty
//        0 * _._
//    }
//
//    def "should handle minor errors on load"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.load()
//
//        then:
//        instance.config.is CONFIG
//        instance.lastLoadedChangeTs == configFile.lastModified()
//        !instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            instance.handleConfigurationReadErrors(ERROR_1)
//            CONFIG
//        }
//        1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.LOAD_ERROR && !error.fatal } as BlockingApplicationErrorEvent)
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == OK } as ConfigurationFileSyncEvent)
//        0 * _._
//    }
//
//    def "should handle minor errors on load - reject by user"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.load()
//
//        then:
//        thrown IllegalStateException
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs == 0L
//        instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            instance.handleConfigurationReadErrors(ERROR_1)
//            CONFIG
//        }
//        1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.LOAD_ERROR && !error.fatal }) >> {
//            throw new IllegalStateException()
//        }
//        0 * _._
//    }
//
//    def "should handle major errors on load"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.load()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs != 0L
//        !instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            throw new InvalidConfigurationException('EX_UNREADABLE_CFG_FILE')
//        }
//        1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.LOAD_ERROR && !error.fatal } as BlockingApplicationErrorEvent)
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == NEW } as ConfigurationFileSyncEvent)
//        0 * _._
//    }
//
//    def "should handle major errors on load - reject by user"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.load()
//
//        then:
//        thrown IllegalStateException
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs != 0L
//        instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            throw new InvalidConfigurationException('EX_UNREADABLE_CFG_FILE')
//        }
//        1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.LOAD_ERROR && !error.fatal }) >> {
//            throw new IllegalStateException()
//        }
//        0 * _._
//    }
//
//    def "should ignore missing configuration file on load"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.load()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs == 0L
//        !instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            throw new MissingConfigurationException('EX_MISSING_CFG_FILE', new IllegalStateException(), configFile)
//        }
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == NEW })
//        0 * _._
//    }
//
//    def "should not reload if not active"() {
//        setup:
//        configFile.text = '--- '
//
//        when:
//        instance.loadIfChanged()
//
//        then:
//        instance.lastLoadedChangeTs == 0L
//        instance.dirty
//        0 * _._
//    }
//
//    def "should handle minor errors on reload"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//
//        when:
//        instance.loadIfChanged()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs == 0L
//        instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            instance.handleConfigurationReadErrors(ERROR_1)
//            CONFIG
//        }
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == FAILED })
//        0 * _._
//    }
//
//    def "should handle major errors on reload"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//
//        when:
//        instance.loadIfChanged()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs != 0L
//        instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            throw new InvalidConfigurationException('EX_UNREADABLE_CFG_FILE')
//        }
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == FAILED })
//        0 * _._
//    }
//
//    def "should handle missing config on reload"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//
//        when:
//        instance.loadIfChanged()
//
//        then:
//        instance.config.modules.isEmpty()
//        instance.config.networkNodes.isEmpty()
//        instance.lastLoadedChangeTs == 0L
//        instance.dirty
//        1 * persistenceService.load(configFile) >> {
//            throw new MissingConfigurationException('EX_MISSING_CFG_FILE', new IllegalStateException(), configFile)
//        }
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == MISSING })
//        0 * _._
//    }
//
//
//    def "should not save when not active"() {
//        when:
//        instance.saveIfDirty()
//
//        then:
//        instance.dirty
//        0 * _._
//    }
//
//    def "should not save when not dirty and config exist"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//        instance.dirty = false
//
//        when:
//        instance.saveIfDirty()
//
//        then:
//        !instance.dirty
//        0 * _._
//    }
//
//    def "should save when config missing"() {
//        setup:
//        instance.init()
//        instance.dirty = false
//
//        when:
//        instance.saveIfDirty()
//
//        then:
//        !instance.dirty
//        1 * persistenceService.save(configFile, instance.config)
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == SAVED })
//        0 * _._
//    }
//
//    def "should save when dirty"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//
//        when:
//        instance.saveIfDirty()
//
//        then:
//        !instance.dirty
//        1 * persistenceService.save(configFile, instance.config)
//        1 * eventBus.post({ ConfigurationFileSyncEvent ev -> ev.code == SAVED })
//        0 * _._
//    }
//
//    def "should handle error on save"() {
//        setup:
//        instance.init()
//        configFile.text = '--- '
//        def e = new IOException()
//
//        when:
//        instance.saveIfDirty()
//
//        then:
//        instance.dirty
//        1 * persistenceService.save(configFile, instance.config) >> {
//            throw new FailedConfigurationSaveException("EX_CONFIG_SAVE_ERROR", e, configFile, e.getLocalizedMessage())
//        }
//        1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.SAVE_ERROR && !error.fatal })
//        0 * _._
//    }
//
//}
