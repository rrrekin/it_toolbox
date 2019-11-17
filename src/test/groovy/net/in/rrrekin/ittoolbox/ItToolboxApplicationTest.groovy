package net.in.rrrekin.ittoolbox

import com.google.common.eventbus.EventBus
import net.in.rrrekin.ittoolbox.configuration.ConfigurationManager
import net.in.rrrekin.ittoolbox.events.BlockingApplicationErrorEvent
import net.in.rrrekin.ittoolbox.gui.MainWindow
import net.in.rrrekin.ittoolbox.gui.nodetree.NetworkNodesTreeModelFacade
import net.in.rrrekin.ittoolbox.infrastructure.BlockingApplicationEventsHandler
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper
import net.in.rrrekin.ittoolbox.infrastructure.UnhandledMessagesLogger
import net.in.rrrekin.ittoolbox.utilities.ErrorCode
import org.slf4j.LoggerFactory
import spock.lang.*

/**
 * @author michal.rudewicz@gmail.com
 */
class ItToolboxApplicationTest extends Specification {

  UnhandledMessagesLogger unhandledMessagesLogger = Mock()
  EventBus eventBus = Mock()
  BlockingApplicationEventsHandler blockingApplicationEventsHandler = Mock()
  ConfigurationManager configurationManager = Mock()
  NetworkNodesTreeModelFacade treeModelFacade = Mock()
  SystemWrapper system = Mock()
  MainWindow mainWindow = Mock()

  def instance = new ItToolboxApplication(unhandledMessagesLogger, eventBus, blockingApplicationEventsHandler, treeModelFacade, configurationManager, mainWindow)

  void setupSpec() {
    ItToolboxApplication.log = LoggerFactory.getLogger(ItToolboxApplication)
  }

  def "should properly build instance"() {
    expect:
    instance.@unhandledMessagesLogger.is unhandledMessagesLogger
    instance.@eventBus.is eventBus
    instance.@blockingApplicationEventsHandler.is blockingApplicationEventsHandler
    instance.@configurationManager.is configurationManager
    instance.@mainWindow.is mainWindow
  }

  def "should validate constructor arguments"() {
    when:
    new ItToolboxApplication(null, eventBus, blockingApplicationEventsHandler, treeModelFacade, configurationManager, mainWindow)
    then:
    thrown NullPointerException

    when:
    new ItToolboxApplication(unhandledMessagesLogger, null, blockingApplicationEventsHandler, treeModelFacade, configurationManager, mainWindow)
    then:
    thrown NullPointerException

    when:
    new ItToolboxApplication(unhandledMessagesLogger, eventBus, null, treeModelFacade, configurationManager, mainWindow)
    then:
    thrown NullPointerException

    when:
    new ItToolboxApplication(unhandledMessagesLogger, eventBus, blockingApplicationEventsHandler, null, configurationManager, mainWindow)
    then:
    thrown NullPointerException

    when:
    new ItToolboxApplication(unhandledMessagesLogger, eventBus, blockingApplicationEventsHandler, treeModelFacade, null, mainWindow)
    then:
    thrown NullPointerException

    when:
    new ItToolboxApplication(unhandledMessagesLogger, eventBus, blockingApplicationEventsHandler, treeModelFacade, configurationManager, null)
    then:
    thrown NullPointerException
  }

  def "should validate calculateAppDirectory argument"() {
    when:
    ItToolboxApplication.calculateAppDirectory(null)
    then:
    thrown NullPointerException
  }

  @Unroll
  @IgnoreIf({ os.windows })
  def "should determine configuration directory for #os / #userHome"() {
    when:
    def dir = ItToolboxApplication.calculateAppDirectory(system)

    then:
    dir == expectedDir as File
    1 * system.getProperty('os.name') >> os
    1 * system.getProperty('user.home') >> userHome

    where:
    os         | userHome        || expectedDir
    'Linux'    | '/home/username' | '/home/username/.local/share/rrrekin/it_toolbox'
    'Linux'    | null             | 'it_toolbox'
    'Linux'    | ''               | 'it_toolbox'
    'Mac OS X' | '/home/username' | '/home/username/Library/Application Support/rrrekin/it_toolbox'
    'Mac OS'   | null             | 'it_toolbox'
    'Mac OS X' | ''               | 'it_toolbox'
    'SunOS'    | '/home/username' | '/home/username/.it_toolbox'
    'SunOS'    | null             | 'it_toolbox'
    'SunOS'    | ''               | 'it_toolbox'
    'HP-UX'    | '/home/username' | '/home/username/.it_toolbox'
    'HP-UX'    | null             | 'it_toolbox'
    'HP-UX'    | ''               | 'it_toolbox'
    'AIX'      | '/home/username' | '/home/username/.it_toolbox'
    'AIX'      | null             | 'it_toolbox'
    'AIX'      | ''               | 'it_toolbox'
  }

  @Unroll
  @Requires({ os.windows })
  def "should determine configuration directory for Microsoft #os / #userHome"() {
    when:
    def dir = ItToolboxApplication.calculateAppDirectory(system)

    then:
    dir == expectedDir as File
    1 * system.getProperty('os.name') >> os
    1 * system.getenv('APPDATA') >> appData
    (0..1) * system.getProperty('user.home') >> userHome

    where:
    os             | appData                                | userHome             || expectedDir
    'Windows XP'   | 'C:\\Users\\username\\ApplicationData' | 'C:\\Users\\username' | 'C:\\Users\\username\\ApplicationData\\rrrekin\\it_toolbox'
    'Windows 2003' | ''                                     | 'C:\\Users\\username' | 'C:\\Users\\username\\AppData\\rrrekin\\it_toolbox'
    'Windows 10'   | null                                   | 'C:\\Users\\username' | 'C:\\Users\\username\\AppData\\rrrekin\\it_toolbox'
    'Windows 8'    | 'C:\\Users\\username\\ApplicationData' | ''                    | 'C:\\Users\\username\\ApplicationData\\rrrekin\\it_toolbox'
    'Windows 2012' | 'C:\\Users\\username\\ApplicationData' | null                  | 'C:\\Users\\username\\ApplicationData\\rrrekin\\it_toolbox'
    'Windows 2012' | null                                   | null                  | 'it_toolbox'
  }

  def "should initialize application"() {
    when:
    instance.init()

    then:
    1 * treeModelFacade.init()
    1 * unhandledMessagesLogger.init()
    1 * blockingApplicationEventsHandler.init()
    then:
    1 * configurationManager.init()
    0 * _._
  }

  def "should terminate when get exception during initialization"() {
    when:
    instance.init()

    then:
    1 * treeModelFacade.init() >> { throw new NullPointerException() }
    1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.INITIALIZATION_ERROR && error.fatal })

    when:
    instance.init()

    then:
    1 * unhandledMessagesLogger.init() >> { throw new NullPointerException() }
    1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.INITIALIZATION_ERROR && error.fatal })

    when:
    instance.init()

    then:
    1 * blockingApplicationEventsHandler.init() >> { throw new NullPointerException() }
    1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.INITIALIZATION_ERROR && error.fatal })

    when:
    instance.init()

    then:
    1 * configurationManager.init() >> { throw new NullPointerException() }
    1 * eventBus.post({ BlockingApplicationErrorEvent error -> error.errorCode == ErrorCode.INITIALIZATION_ERROR && error.fatal })
  }

  @Ignore("Not implemented yet")
  def "should start application"() {
    expect:
    false
  }

  def "should terminate application"() {
    when:
    instance.shutdown()

    then:
    1 * configurationManager.shutdown()
    0 * _._
  }

  def "should ignore errors on application shutdown"() {
    when:
    instance.shutdown()

    then:
    1 * configurationManager.shutdown() >> { throw new NullPointerException() }
    0 * _._
    notThrown()
  }

}
