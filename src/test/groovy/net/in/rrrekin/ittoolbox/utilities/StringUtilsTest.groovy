package net.in.rrrekin.ittoolbox.utilities

import net.in.rrrekin.ittoolbox.utilities.exceptions.TemplateException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class StringUtilsTest extends Specification {

    static final COMPLEX_VARS = [
            address: '1.2.3.4',
            user   : 'artur.dent',
            port   : 42,
            options: '-L 1234:localhost:4321 -D -f',
            list   : ['Don\'t Panic', 42, "Zephod"],
            map    : [a: 'AAA', b: 'BBB', c: [d: 'DDD', e: 'EEE']],
            url    : new URL('https://google.com/'),
            empty  : '',
            blank  : '   \t   ',
    ]


    @Unroll
    def "should evaluate template #template"() {
        expect:
        StringUtils.applyTemplate(template, variables as Map<String, Object>) == evaluated

        where:
        template                                                            | variables    | evaluated
        'plain text'                                                        | [:]          | 'plain text'
        'plain text'                                                        | COMPLEX_VARS | 'plain text'
        '$address # $user # $port # $options'                               | COMPLEX_VARS | '1.2.3.4 # artur.dent # 42 # -L 1234:localhost:4321 -D -f'
        '$list # $map # $url'                                               | COMPLEX_VARS | '[Don\'t Panic, 42, Zephod] # [a:AAA, b:BBB, c:[d:DDD, e:EEE]] # https://google.com/'
        '${user.capitalize()}'                                              | COMPLEX_VARS | 'Artur.dent'
        'empty: \'$empty\' or ${empty.isEmpty()?\'<empty>\':\'non-empty\'}' | COMPLEX_VARS | 'empty: \'\' or <empty>'
        'blank: ${blank?"is blank":"not blank"}'                            | COMPLEX_VARS | 'blank: is blank'
        '${list[2]} ${map["b"]} ${map["c"]["d"]} ${map.c.e}'                | COMPLEX_VARS | 'Zephod BBB DDD EEE'
        '${url.host}'                                                       | COMPLEX_VARS | 'google.com'
    }

    def "should fail for missing variable"() {
        when:
        StringUtils.applyTemplate('$missing', [:])
        then:
        def ex = thrown TemplateException
        ex.message.startsWith('Failed to evaluate template "$missing": No such property: missing')
    }

    def "should fail for invalid expression"() {
        when:
        StringUtils.applyTemplate('${missing  asdfa asfas $s', [:])
        then:
        def ex = thrown TemplateException
        ex.message.startsWith('Failed to evaluate template "${missing  asdfa asfas $s": Failed to parse template script')
    }

    def "should fail for internal exceptions"() {
        when:
        println StringUtils.applyTemplate('${def a=null;a.execute()}', [:])
        then:
        def ex = thrown TemplateException
        ex.message.startsWith('Failed to evaluate template "${def a=null;a.execute()}": Cannot invoke method execute() on null object')

        when:
        println StringUtils.applyTemplate('${url.nonExisting()}', COMPLEX_VARS)
        then:
        ex = thrown TemplateException
        ex.message.startsWith('Failed to evaluate template "${url.nonExisting()}": No signature of method: java.net.URL.nonExisting() is applicable for argument types: () values: []')
    }

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
