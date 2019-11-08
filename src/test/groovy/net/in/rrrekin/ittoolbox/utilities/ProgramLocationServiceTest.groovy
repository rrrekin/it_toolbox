package net.in.rrrekin.ittoolbox.utilities

import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification

import java.nio.file.Paths
/**
 * @author michal.rudewicz@gmail.com
 */
class ProgramLocationServiceTest extends Specification {

    def "should accept null paths"() {
        when:
        def instance = new ProgramLocationService(null)

        then:
        instance.paths.isEmpty()
    }

    def "should validate arguments"() {
        given:
        def instance = new ProgramLocationService(null)

        when:
        instance.getProgramBinary(null)

        then:
        thrown NullPointerException
    }

    @IgnoreIf({ os.windows })
    def "should find applications"() {
        def instance = new ProgramLocationService("src/test/resources/fakeBin:/opt/Name With Space/::src/test/resources/local/fakeBin:src/test/resources/local/fake bin")

        expect:
        instance.paths == [Paths.get('src/test/resources/fakeBin'), Paths.get('/opt/Name With Space'), Paths.get('src/test/resources/local/fakeBin'), Paths.get('src/test/resources/local/fake bin')]
        instance.getProgramBinary('non_existent_program') == null
        instance.getProgramBinary('non_executable') == null
        instance.getProgramBinary('executable1') == 'src/test/resources/fakeBin/executable1' as File
        instance.getProgramBinary('executable2') == 'src/test/resources/local/fakeBin/executable2' as File
        instance.getProgramBinary('executable3') == 'src/test/resources/local/fake bin/executable3' as File
    }

    @Requires({ os.windows })
    def "should find applications on Windows"() {
        def instance = new ProgramLocationService("src\\test\\resources\\fakeBin;C:\\opt\\Name With Space\\;;src\\test\\resources\\local\\fakeBin;src\\test\\resources\\local\\fake bin")

        expect:
        instance.paths == [Paths.get('src/test/resources/fakeBin'), Paths.get('c:/opt/Name With Space'), Paths.get('src/test/resources/local/fakeBin'), Paths.get('src/test/resources/local/fake bin')]
        instance.getProgramBinary('non_existent_program') == null
        instance.getProgramBinary('non_executable') == 'src\\test\\resources\\local\\fakeBin\\non_executable' as File // Windows has no executable permission
        instance.getProgramBinary('executable1') == 'src/test/resources/fakeBin/executable1' as File
        instance.getProgramBinary('executable2') == 'src/test/resources/local/fakeBin/executable2' as File
        instance.getProgramBinary('executable3') == 'src/test/resources/local/fake bin/executable3' as File
    }
}
