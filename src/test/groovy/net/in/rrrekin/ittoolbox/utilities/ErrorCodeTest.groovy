package net.in.rrrekin.ittoolbox.utilities

import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class ErrorCodeTest extends Specification {

    SystemWrapper system = Mock()

    void setup() {
        ErrorCode.system = system
    }

    void cleanup() {
        ErrorCode.system = new SystemWrapper()
    }

    @Unroll
    def "should exit with proper code for #errorCode"(){
        when:
        errorCode.exit()

        then:
        1 * system.exit(errorCode.status)
        where:
        errorCode << ErrorCode.values()
    }

}
