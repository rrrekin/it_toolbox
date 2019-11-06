package net.in.rrrekin.ittoolbox.os

import net.in.rrrekin.ittoolbox.configuration.nodes.Server
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService
import spock.lang.Specification
import spock.lang.Unroll

import static net.in.rrrekin.ittoolbox.utilities.StringUtils.applyTemplate

/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesDefaultImplTest extends Specification {

    ProgramLocationService locationService = Mock()

    def instance = new OsServicesDefaultImpl(locationService)
    def server = new Server('Google', 'google.com', 'Google server', [property1: 'value1', property2: 'value2'], []).immutableDataCopy()


    @Unroll
    def "should provide terminal command list proper for given machine"() {
        given:
        locationService.getProgramBinary("terminator") >> { terminatorCommand as File }
        locationService.getProgramBinary("konsole") >> { konsoleCommand as File }
        locationService.getProgramBinary("xterm") >> { xtermCommand as File }
        locationService.getProgramBinary("gnome-terminal") >> { gnomeTermCommand as File }

        when:
        def list = instance.getPossibleTerminalCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultTerminalCommand()

        then:
        defaultCommand == list[0]

        where:
        terminatorCommand     | konsoleCommand     | xtermCommand     | gnomeTermCommand          || expectedList
        null                  | null               | null             | null                      || ['xterm -hold -e $command']
        '/usr/bin/terminator' | '/usr/bin/konsole' | '/usr/bin/xterm' | '/usr/bin/gnome-terminal' || ['/usr/bin/terminator -x $command \';\' sleep 10d', '/usr/bin/konsole --hold -e $command', '/usr/bin/xterm -hold -e $command', '/usr/bin/gnome-terminal -- $command']
        '/usr/bin/terminator' | null               | null             | null                      || ['/usr/bin/terminator -x $command \';\' sleep 10d']
        null                  | '/usr/bin/konsole' | null             | null                      || ['/usr/bin/konsole --hold -e $command']
        null                  | null               | '/usr/bin/xterm' | null                      || ['/usr/bin/xterm -hold -e $command']
        null                  | null               | null             | '/usr/bin/gnome-terminal' || ['/usr/bin/gnome-terminal -- $command']
        '/usr/bin/terminator' | null               | '/usr/bin/xterm' | '/usr/bin/gnome-terminal' || ['/usr/bin/terminator -x $command \';\' sleep 10d', '/usr/bin/xterm -hold -e $command', '/usr/bin/gnome-terminal -- $command']
        null                  | '/usr/bin/konsole' | '/usr/bin/xterm' | null                      || ['/usr/bin/konsole --hold -e $command', '/usr/bin/xterm -hold -e $command']
    }

    @Unroll
    def "default terminal command should properly evaluate for #command"() {
        given:
        locationService.getProgramBinary(command) >> { commandPath as File }

        expect:
        applyTemplate(instance.getDefaultTerminalCommand(), [command: 'mtr -n google.com']) == response

        where:
        command          | commandPath               | response
        'none'           | ''                        | 'xterm -hold -e mtr -n google.com'
        'konsole'        | '/usr/bin/konsole'        | '/usr/bin/konsole --hold -e mtr -n google.com'
        'terminator'     | '/usr/bin/terminator'     | '/usr/bin/terminator -x mtr -n google.com \';\' sleep 10d'
        'xterm'          | '/usr/bin/xterm'          | '/usr/bin/xterm -hold -e mtr -n google.com'
        'gnome-terminal' | '/usr/bin/gnome-terminal' | '/usr/bin/gnome-terminal -- mtr -n google.com'
    }


    def "default ping command should properly evaluate"() {
        expect:
        instance.getDefaultPingCommand() == instance.getPossiblePingCommands()[0]
        instance.getPossiblePingCommands() == ['ping${options.trim()?\' \'+options:\'\'} ${server.address}']
        applyTemplate(instance.getDefaultPingCommand(), [server: server, options: '']) == 'ping google.com'
        applyTemplate(instance.getDefaultPingCommand(), [server: server, options: '-c 3']) == 'ping -c 3 google.com'
    }

    @Unroll
    def "should provide traceroute command list proper for given machine"() {
        given:
        locationService.getProgramBinary("traceroute") >> { tracerouteCommand as File }
        locationService.getProgramBinary("mtr") >> { mtrCommand as File }

        when:
        def list = instance.getPossibleTracerouteCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultTracerouteCommand()

        then:
        defaultCommand == list[0]

        where:
        tracerouteCommand     | mtrCommand     || expectedList
        null                  | null           || ['traceroute${options.trim()?\' \'+options:\'\'} ${server.address}']
        '/usr/bin/traceroute' | '/usr/bin/mtr' || ['/usr/bin/mtr${options.trim()?\' \'+options:\'\'} ${server.address}', '/usr/bin/traceroute${options.trim()?\' \'+options:\'\'} ${server.address}']
        null                  | '/usr/bin/mtr' || ['/usr/bin/mtr${options.trim()?\' \'+options:\'\'} ${server.address}']
        '/usr/bin/traceroute' | null           || ['/usr/bin/traceroute${options.trim()?\' \'+options:\'\'} ${server.address}']
    }

    @Unroll
    def "default traceroute command should properly evaluate"() {
        given:
        locationService.getProgramBinary(command) >> { commandPath as File }

        expect:
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '']) == response
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '-c 3']) == response2

        where:
        command      | commandPath           | response                         | response2
        'none'       | ''                    | 'traceroute google.com'          | 'traceroute -c 3 google.com'
        'traceroute' | '/usr/bin/traceroute' | '/usr/bin/traceroute google.com' | '/usr/bin/traceroute -c 3 google.com'
        'mtr'        | '/usr/bin/mtr'        | '/usr/bin/mtr google.com'        | '/usr/bin/mtr -c 3 google.com'
    }

    @Unroll
    def "should provide nslookup command list proper for given machine"() {
        given:
        locationService.getProgramBinary("nslookup") >> { nslookupCommand as File }
        locationService.getProgramBinary("dig") >> { digCommand as File }

        when:
        def list = instance.getPossibleNslookupCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultNslookupCommand()

        then:
        defaultCommand == list[0]

        where:
        nslookupCommand     | digCommand     || expectedList
        null                | null           || ['nslookup${options.trim()?\' \'+options:\'\'} ${server.address}']
        '/usr/bin/nslookup' | '/usr/bin/dig' || ['/usr/bin/nslookup${options.trim()?\' \'+options:\'\'} ${server.address}', '/usr/bin/dig${options.trim()?\' \'+options:\'\'} ${server.address}']
        null                | '/usr/bin/dig' || ['/usr/bin/dig${options.trim()?\' \'+options:\'\'} ${server.address}']
        '/usr/bin/nslookup' | null           || ['/usr/bin/nslookup${options.trim()?\' \'+options:\'\'} ${server.address}']
    }

    @Unroll
    def "default nslookup command should properly evaluate"() {
        given:
        locationService.getProgramBinary(command) >> { commandPath as File }

        expect:
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '']) == response
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '-c 3']) == response2

        where:
        command    | commandPath         | response                       | response2
        'none'     | ''                  | 'nslookup google.com'          | 'nslookup -c 3 google.com'
        'nslookup' | '/usr/bin/nslookup' | '/usr/bin/nslookup google.com' | '/usr/bin/nslookup -c 3 google.com'
        'dig'      | '/usr/bin/dig'      | '/usr/bin/dig google.com'      | '/usr/bin/dig -c 3 google.com'
    }

    def "should provide ssh command list proper for given machine"() {
        when:
        def list = instance.getPossibleNslookupCommands()

        then:
        list == ['nslookup${options.trim()?\' \'+options:\'\'} ${server.address}']

        when:
        def defaultCommand = instance.getDefaultNslookupCommand()

        then:
        defaultCommand == list[0]
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
    def "should provide rdp command list proper for given machine"() {
        given:
        locationService.getProgramBinary("rdesktop") >> { rdesktopCommand as File }
        locationService.getProgramBinary("xfreerdp") >> { xfreerdpCommand as File }

        when:
        def list = instance.getPossibleRdpCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultRdpCommand()

        then:
        defaultCommand == list[0]

        where:
        rdesktopCommand     | xfreerdpCommand     || expectedList
        null                | null                || ['rdesktop${user.trim()?\' -u "\'+user+\'"\':\'\'}${password.trim()?\' -p "\'+password+\'"\':\'\'}${options.trim()?\' \'+options:\'\'} ${server.address}${port>0?\':\'+port:\'\'}']
        '/usr/bin/rdesktop' | '/usr/bin/xfreerdp' || ['/usr/bin/rdesktop${user.trim()?\' -u "\'+user+\'"\':\'\'}${password.trim()?\' -p "\'+password+\'"\':\'\'}${options.trim()?\' \'+options:\'\'} ${server.address}${port>0?\':\'+port:\'\'}', '/usr/bin/xfreerdp${user.trim()?\' /u:"\'+user+\'"\':\'\'}${password.trim()?\' /p:"\'+password+\'"\':\'\'}${options.trim()?\' \'+options:\'\'} /v:${server.address}${port>0?\':\'+port:\'\'}']
        null                | '/usr/bin/xfreerdp' || ['/usr/bin/xfreerdp${user.trim()?\' /u:"\'+user+\'"\':\'\'}${password.trim()?\' /p:"\'+password+\'"\':\'\'}${options.trim()?\' \'+options:\'\'} /v:${server.address}${port>0?\':\'+port:\'\'}']
        '/usr/bin/rdesktop' | null                || ['/usr/bin/rdesktop${user.trim()?\' -u "\'+user+\'"\':\'\'}${password.trim()?\' -p "\'+password+\'"\':\'\'}${options.trim()?\' \'+options:\'\'} ${server.address}${port>0?\':\'+port:\'\'}']
    }

    @Unroll
    def "default rdp command should properly evaluate for #command / '#user' / #password / #port / '#options'"() {
        given:
        locationService.getProgramBinary(command) >> {
            command == 'none' ? null : "/usr/bin/$command" as File
        }

        expect:
        applyTemplate(instance.getDefaultRdpCommand(), [server: server, user: user, password: password, port: port, options: options]) == response

        where:
        command    | user        | password   | port | options                     || response
        'none'     | ''          | ''         | 0    | ''                          || 'rdesktop google.com'
        'none'     | '\t '       | ' '        | 0    | ' \t \t '                   || 'rdesktop google.com'
        'none'     | 'user.name' | ' '        | 0    | ' '                         || 'rdesktop -u "user.name" google.com'
        'none'     | ''          | 'password' | 0    | ' '                         || 'rdesktop -p "password" google.com'
        'none'     | ''          | ''         | 1022 | ''                          || 'rdesktop google.com:1022'
        'none'     | ''          | ''         | 0    | '-D -L 1234:localhost:4321' || 'rdesktop -D -L 1234:localhost:4321 google.com'
        'none'     | 'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || 'rdesktop -u "user.name" -p "password" -D -L 1234:localhost:4321 google.com:1222'
        'rdesktop' | ''          | ''         | 0    | ''                          || '/usr/bin/rdesktop google.com'
        'rdesktop' | '\t '       | ' '        | 0    | ' \t \t '                   || '/usr/bin/rdesktop google.com'
        'rdesktop' | 'user.name' | ' '        | 0    | ' '                         || '/usr/bin/rdesktop -u "user.name" google.com'
        'rdesktop' | ''          | 'password' | 0    | ' '                         || '/usr/bin/rdesktop -p "password" google.com'
        'rdesktop' | ''          | ''         | 1022 | ''                          || '/usr/bin/rdesktop google.com:1022'
        'rdesktop' | ''          | ''         | 0    | '-D -L 1234:localhost:4321' || '/usr/bin/rdesktop -D -L 1234:localhost:4321 google.com'
        'rdesktop' | 'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || '/usr/bin/rdesktop -u "user.name" -p "password" -D -L 1234:localhost:4321 google.com:1222'
        'xfreerdp' | ''          | ''         | 0    | ''                          || '/usr/bin/xfreerdp /v:google.com'
        'xfreerdp' | '\t '       | ' '        | 0    | ' \t \t '                   || '/usr/bin/xfreerdp /v:google.com'
        'xfreerdp' | 'user.name' | ' '        | 0    | ' '                         || '/usr/bin/xfreerdp /u:"user.name" /v:google.com'
        'xfreerdp' | ''          | 'password' | 0    | ' '                         || '/usr/bin/xfreerdp /p:"password" /v:google.com'
        'xfreerdp' | ''          | ''         | 1022 | ''                          || '/usr/bin/xfreerdp /v:google.com:1022'
        'xfreerdp' | ''          | ''         | 0    | '-D -L 1234:localhost:4321' || '/usr/bin/xfreerdp -D -L 1234:localhost:4321 /v:google.com'
        'xfreerdp' | 'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || '/usr/bin/xfreerdp /u:"user.name" /p:"password" -D -L 1234:localhost:4321 /v:google.com:1222'
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
        shells.each { println it }

        then:
        !shells.empty
    }


}
