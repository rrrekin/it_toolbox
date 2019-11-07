package net.in.rrrekin.ittoolbox.configuration.exceptions

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import spock.lang.Specification
/**
 * @author michal.rudewicz@gmail.com
 */
class InvalidConfigurationExceptionTest extends Specification {

    static final CAUSE = new NullPointerException("NPE")
    static final CODE = 'EX_UNKNOWN_VERSION'
    static final FILENAME = 'sample_file.pdf'
    static final VERSION = 3.14
    static final MSG = 'Unable to read configuration file ({0}). Unsupported version: {1}.'
    static final LOC_MSG = 'Nie można odczytać pliku konfiguracyjnego ({0}). Nieobsługiwana wersja: {1}.'
    static final MSG_ARGS ='Unable to read configuration file (sample_file.pdf). Unsupported version: 3.14.'
    static final LOC_MSG_ARGS = 'Nie można odczytać pliku konfiguracyjnego (sample_file.pdf). Nieobsługiwana wersja: 3,14.'

    void setup() {
        LocaleUtil.setLocale(Locale.forLanguageTag('pl'))
    }

    void cleanup() {
        LocaleUtil.setLocale(null)
    }

    def "should create exception with message only"() {
        when:
        def exception = new InvalidConfigurationException(CODE)

        then:
        exception.message == MSG
        exception.localizedMessage == LOC_MSG
        exception.cause == null
    }

    def "should create exception with message and arguments"() {
        when:
        def exception = new InvalidConfigurationException(CODE, FILENAME, VERSION)

        then:
        exception.message == MSG_ARGS
        exception.localizedMessage == LOC_MSG_ARGS
        exception.cause == null
    }

    def "should create exception with message and cause"() {
        when:
        def exception = new InvalidConfigurationException(CODE, CAUSE)

        then:
        exception.message == MSG
        exception.localizedMessage == LOC_MSG
        exception.cause == CAUSE
    }

    def "should create exception with message, args and cause"() {
        when:
        def exception = new InvalidConfigurationException(CODE, CAUSE, FILENAME, VERSION)

        then:
        exception.message == MSG_ARGS
        exception.localizedMessage == LOC_MSG_ARGS
        exception.cause == CAUSE
    }

}
