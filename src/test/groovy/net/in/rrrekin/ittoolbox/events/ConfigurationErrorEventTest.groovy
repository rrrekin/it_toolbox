package net.in.rrrekin.ittoolbox.events

import spock.lang.Specification
import spock.lang.Unroll

import static net.in.rrrekin.ittoolbox.events.ConfigurationErrorEvent.Code.*

/**
 * @author michal.rudewicz@gmail.com
 */
class ConfigurationErrorEventTest extends Specification {

    @Unroll
    def "should properly generate single line representation for #event"() {
        expect:
        event.singleLineError() == expectedError

        where:
        event                                                                                               || expectedError
        new ConfigurationErrorEvent(SERVER_LIST_UNREADABLE, '')                                             || ''
        new ConfigurationErrorEvent(CANNOT_CREATE_NETWORK_NODE, 'txt')                                      || 'txt'
        new ConfigurationErrorEvent(INVALID_OBJECT_ON_DTO_LIST, '\n\r\nsdadasf asdfasd afsdfa\r\nsdfasd\t') || '; sdadasf asdfasd afsdfa; sdfasd'
        new ConfigurationErrorEvent(INVALID_MODULE_OPTIONS, '\rabc\ndef')                                   || '; abc; def'
    }
}
