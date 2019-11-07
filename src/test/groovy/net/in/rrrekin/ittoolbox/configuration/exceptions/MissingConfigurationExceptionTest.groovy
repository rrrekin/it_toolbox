package net.in.rrrekin.ittoolbox.configuration.exceptions

import net.in.rrrekin.ittoolbox.utilities.LocaleUtil
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class MissingConfigurationExceptionTest extends Specification {

    def "should create proper exception"() {
        given:
        LocaleUtil.setLocale(Locale.forLanguageTag('pl'))
        def cause = new FileNotFoundException("Brak pliku")

        when:
        def exception = new MissingConfigurationException('EX_MISSING_CFG_FILE', cause, 'sample_file.txt' as File)

        then:
        exception.getMessage() == 'Missing configuration file (sample_file.txt)'
        exception.getLocalizedMessage() == 'Brak pliku konfiguracyjnego (sample_file.txt)'
        exception.cause == cause

        cleanup:
        LocaleUtil.setLocale(null)
    }
}
