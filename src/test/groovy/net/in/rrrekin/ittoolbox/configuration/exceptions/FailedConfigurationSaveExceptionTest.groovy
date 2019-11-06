package net.in.rrrekin.ittoolbox.configuration.exceptions

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import spock.lang.Specification

import java.text.MessageFormat
/**
 * @author michal.rudewicz@gmail.com
 */
class FailedConfigurationSaveExceptionTest extends Specification {

    static final CAUSE = new FileNotFoundException("Brak pliku")
    static final CODE = 'CONFIG_SAVE_ERROR'
    static final FILENAME = 'sample_file.pdf'
    static final ERROR = 'buuuu'
    static final MSG = ResourceBundle.getBundle(LocaleUtil.MESSAGES_PROPERTY_BUNDLE, Locale.forLanguageTag('')).getString(CODE)
    static final LOC_MSG = ResourceBundle.getBundle(LocaleUtil.MESSAGES_PROPERTY_BUNDLE, Locale.forLanguageTag('pl')).getString(CODE)
    static final MSG_ARGS = MessageFormat.format(MSG, FILENAME, ERROR)
    static final LOC_MSG_ARGS = MessageFormat.format(LOC_MSG, FILENAME, ERROR)

    void setup() {
        LocaleUtil.setLocale(Locale.forLanguageTag('pl'))
    }

    void cleanup() {
        LocaleUtil.setLocale(null)
    }

    def "should create exception with message, args and cause"() {
        when:
        def exception = new FailedConfigurationSaveException(CODE, CAUSE, FILENAME, ERROR)

        then:
        exception.message == MSG_ARGS
        exception.localizedMessage == LOC_MSG_ARGS
        exception.cause == CAUSE
    }
}
