package net.in.rrrekin.ittoolbox.os

import net.in.rrrekin.ittoolbox.configuration.nodes.Server
import org.apache.commons.lang3.SystemUtils
import spock.lang.Specification
import spock.lang.Unroll

import static net.in.rrrekin.ittoolbox.utilities.StringUtils.applyTemplate

/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesDefaultImplTest extends Specification {

    def instance = new OsServicesDefaultImpl(locationService)
    def server = new Server('Google', 'google.com', 'Google server', [property1: 'value1', property2: 'value2'], []).immutableDataCopy()

    def "should return proper application directory"() {
        expect:
        instance.getAppDirectory() == new File(SystemUtils.USER_HOME, '.it_toolbox')
    }

    def "default terminal command should properly evaluate"() {
        expect:
        applyTemplate(instance.getDefaultTerminalCommand(), [command: 'bash -l']) == 'xterm -hold -e bash -l'
    }

    def "default ping command should properly evaluate"() {

        expect:
        applyTemplate(instance.getDefaultPingCommand(), [server: server, options: '']) == 'ping google.com'
        applyTemplate(instance.getDefaultPingCommand(), [server: server, options: '-c 3']) == 'ping -c 3 google.com'
    }

    def "default traceroute command should properly evaluate"() {
        expect:
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '']) == 'traceroute google.com'
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '-c 3']) == 'traceroute -c 3 google.com'
    }

    def "default nslookup command should properly evaluate"() {
        expect:
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '']) == 'nslookup google.com'
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '-c 3']) == 'nslookup -c 3 google.com'
    }

    @Unroll
    def "default ssh command should properly evaluate for '#user' / #port / '#options'"() {
        expect:
        applyTemplate(instance.getDefaultSshCommand(), [server: server, user: user, port: port, options: options]) == command

        where:
        user        | port | options                     || command
        ''          | 0    | ''                          || 'ssh google.com'
        ' \t '      | 0    | '\t \t'                     || 'ssh google.com'
        'user.name' | 0    | ''                          || 'ssh google.com -l "user.name"'
        ''          | 1022 | ''                          || 'ssh google.com -p 1022'
        ''          | 0    | '-D -L 1234:localhost:4321' || 'ssh google.com -D -L 1234:localhost:4321'
    }

    @Unroll
    def "default rdp command should properly evaluate for '#user' / #password / #port / '#options'"() {
        expect:
        applyTemplate(instance.getDefaultRdpCommand(), [server: server, user: user, password: password, port: port, options: options]) == command

        where:
        user        | password   | port | options                     || command
        ''          | ''         | 0    | ''                          || 'rdesktop google.com'
        ' \t '      | ''         | 0    | '\t \t'                     || 'rdesktop google.com'
        'user.name' | ''         | 0    | ''                          || 'rdesktop -u "user.name" google.com'
        ''          | 'password' | 0    | ''                          || 'rdesktop -p "password" google.com'
        ''          | ''         | 1022 | ''                          || 'rdesktop google.com:1022'
        ''          | ''         | 0    | '-D -L 1234:localhost:4321' || 'rdesktop -D -L 1234:localhost:4321 google.com'
        'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || 'rdesktop -u "user.name" -p "password" -D -L 1234:localhost:4321 google.com:1222'
    }

    @Unroll
    def "default vnc command should properly evaluate for #port / '#options'"() {
        expect:
        applyTemplate(instance.getDefaultVncCommand(), [server: server, port: port, options: options]) == command

        where:
        port | options                     || command
        0    | ''                          || 'xvncviewer google.com'
        0    | '\t \t'                     || 'xvncviewer google.com'
        1022 | ''                          || 'xvncviewer google.com:1022'
        0    | '-D -L 1234:localhost:4321' || 'xvncviewer google.com -D -L 1234:localhost:4321'
        1222 | '-D -L 1234:localhost:4321' || 'xvncviewer google.com:1222 -D -L 1234:localhost:4321'
    }

    def "default shell command should properly evaluate"() {
        expect:
        applyTemplate(instance.getDefaultShellCommand(), [server: server, options: '']) == '/bin/sh'
        applyTemplate(instance.getDefaultShellCommand(), [server: server, options: '-l']) == '/bin/sh -l'
    }

    def "should list available shells"() {
        when:
        def shells = instance.getPossibleShellCommands()
        shells.each {println it}

        expect:
        !shells.empty
    }


}
