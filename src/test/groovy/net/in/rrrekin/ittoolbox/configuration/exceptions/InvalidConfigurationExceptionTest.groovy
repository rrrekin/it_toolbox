package net.in.rrrekin.ittoolbox.configuration.exceptions

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import spock.lang.Specification

import java.text.MessageFormat
/**
 * @author michal.rudewicz@gmail.com
 */
class InvalidConfigurationExceptionTest extends Specification {

    static final CAUSE = new NullPointerException("NPE")
    static final CODE = 'UNKNOWN_VERSION'
    static final FILENAME = 'sample_file.pdf'
    static final VERSION = 3.14
    static final MSG = ResourceBundle.getBundle(LocaleUtil.MESSAGES_PROPERTY_BUNDLE, Locale.forLanguageTag('')).getString(CODE)
    static final LOC_MSG = ResourceBundle.getBundle(LocaleUtil.MESSAGES_PROPERTY_BUNDLE, Locale.forLanguageTag('pl')).getString(CODE)
    static final MSG_ARGS = MessageFormat.format(MSG, FILENAME, VERSION)
    static final LOC_MSG_ARGS = MessageFormat.format(LOC_MSG, FILENAME, VERSION)

    void setup() {
        Locale.setDefault(Locale.forLanguageTag('pl'))
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
