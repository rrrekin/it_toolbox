package net.in.rrrekin.ittoolbox.configuration.exceptions

import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class MissingConfigurationExceptionTest extends Specification {

    def "should create proper exception"() {
        given:
        Locale.setDefault(Locale.forLanguageTag('pl'))
        def cause = new FileNotFoundException("Brak pliku")

        when:
        def exception = new MissingConfigurationException('MISSING_CFG_FILE', cause, 'sample_file.txt' as File)

        then:
        exception.getMessage() == 'Missing configuration file (sample_file.txt)'
        exception.getLocalizedMessage() == 'Brak pliku konfiguracyjnego (sample_file.txt)'
        exception.cause == cause
    }
}
