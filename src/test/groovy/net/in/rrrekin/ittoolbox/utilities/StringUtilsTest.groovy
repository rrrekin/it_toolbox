package net.in.rrrekin.ittoolbox.utilities

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class StringUtilsTest extends Specification {

    @Unroll
    def "should return string representation or empty string for null"() {
        expect:
        StringUtils.toStringOrEmpty(object) == value

        where:
        object                   | value
        42                       | '42'
        'Don\'t panic'           | 'Don\'t panic'
        ['Arthur Dent', 'towel'] | '[Arthur Dent, towel]'
        [Zaphod: 'Beeblebrox']   | '{Zaphod=Beeblebrox}'
        null                     | ''
    }

    @Unroll
    def "should return string representation or default string for null"() {
        expect:
        StringUtils.toStringOrDefault(object, 'defaultValue') == value

        where:
        object                   | value
        42                       | '42'
        'Don\'t panic'           | 'Don\'t panic'
        ['Arthur Dent', 'towel'] | '[Arthur Dent, towel]'
        [Zaphod: 'Beeblebrox']   | '{Zaphod=Beeblebrox}'
        null                     | 'defaultValue'
    }

    def "should reject null default value"() {
        when:
        StringUtils.toStringOrDefault('a', null)

        then:
        thrown NullPointerException
    }
}
