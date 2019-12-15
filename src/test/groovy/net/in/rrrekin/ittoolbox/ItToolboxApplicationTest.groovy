package net.in.rrrekin.ittoolbox

import com.google.common.eventbus.EventBus
import com.google.inject.Injector
import net.in.rrrekin.ittoolbox.gui.services.CommonResources
import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper
import net.in.rrrekin.ittoolbox.infrastructure.UnhandledMessagesLogger
import org.slf4j.LoggerFactory
import spock.lang.*

/**
 * @author michal.rudewicz@gmail.com
 */
class ItToolboxApplicationTest extends Specification {

  UnhandledMessagesLogger unhandledMessagesLogger = Mock()
  CommonResources commonResources = Mock()
  SystemWrapper system = Mock()
  EventBus eventBus = Mock()

  Injector injector = Mock() {
    getInstance(UnhandledMessagesLogger) >> unhandledMessagesLogger
    getInstance(CommonResources) >> commonResources
    getInstance(EventBus) >> eventBus
  }

  def instance = new ItToolboxApplication()

  void setup() {
    ItToolboxApplication.@log = LoggerFactory.getLogger(ItToolboxApplication)
    ItToolboxApplication.@injector = injector
  }

  void cleanup() {
    ItToolboxApplication.@log = null
    ItToolboxApplication.@injector = null
  }

  def "should properly build instance"() {
    expect:
    true
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
    1 * unhandledMessagesLogger.init()
    and:
    instance.@eventBus.is eventBus
    instance.@commonResources.is commonResources
  }

  def "should terminate when get exception during initialization"() {
    when:
    instance.init()

    then:
    1 * unhandledMessagesLogger.init() >> { throw new NullPointerException() }
    1 * commonResources.fatalException(_,null,_)
  }

  @Ignore("Not implemented yet")
  def "should start application"() {
    expect:
    false
  }

  @Ignore("Nothing to test up to now")
  def "should terminate application"() {
    when:
    instance.stop()

    then:
    0 * _._
  }

}
