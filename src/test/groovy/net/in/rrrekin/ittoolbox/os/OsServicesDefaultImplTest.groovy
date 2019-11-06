package net.in.rrrekin.ittoolbox.os

import net.in.rrrekin.ittoolbox.configuration.nodes.Server
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static net.in.rrrekin.ittoolbox.utilities.StringUtils.applyTemplate
import static org.apache.commons.text.StringEscapeUtils.escapeJava;

/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesDefaultImplTest extends Specification {

    @Shared
    def terminator = '/usr/bin/terminator' as File
    @Shared
    def konsole = '/usr/bin/konsole' as File
    @Shared
    def xterm = '/usr/bin/xterm' as File
    @Shared
    def gnomeTerm = '/usr/bin/gnome-terminal' as File
    @Shared
    def traceroute = '/usr/bin/traceroute' as File
    @Shared
    def mtr = '/usr/bin/mtr' as File
    @Shared
    def nslookup = '/usr/bin/nslookup' as File
    @Shared
    def dig = '/usr/bin/dig' as File
    @Shared
    def rdesktop = '/usr/bin/rdesktop' as File
    @Shared
    def xfreerdp = '/usr/bin/xfreerdp' as File

    ProgramLocationService locationService = Mock()

    def instance = new OsServicesDefaultImpl(locationService)
    def server = new Server('Google', 'google.com', 'Google server', [property1: 'value1', property2: 'value2'], []).immutableDataCopy()


    @Unroll
    def "should provide terminal command list proper for given machine"() {
        given:
        locationService.getProgramBinary("terminator") >> { hasTerminator ? terminator : null }
        locationService.getProgramBinary("konsole") >> { hasKonsole ? konsole : null }
        locationService.getProgramBinary("xterm") >> { hasXterm ? xterm : null }
        locationService.getProgramBinary("gnome-terminal") >> { hasGnomeTerm ? gnomeTerm : null }

        when:
        def list = instance.getPossibleTerminalCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultTerminalCommand()

        then:
        defaultCommand == list[0]

        where:
        hasTerminator | hasKonsole | hasXterm | hasGnomeTerm || expectedList
        false         | false      | false    | false        || ['xterm -hold -e $command']
        true          | true       | true     | true         || ["${escapeJava(terminator.absolutePath)} -x \$command ';' sleep 10d", "${escapeJava(konsole.absolutePath)} --hold -e \$command", "${escapeJava(xterm.absolutePath)} -hold -e \$command", "${escapeJava(gnomeTerm.absolutePath)} -- \$command"]
        true          | false      | false    | false        || ["${escapeJava(terminator.absolutePath)} -x \$command ';' sleep 10d"]
        false         | true       | false    | false        || ["${escapeJava(konsole.absolutePath)} --hold -e \$command"]
        false         | false      | true     | false        || ["${escapeJava(xterm.absolutePath)} -hold -e \$command"]
        false         | false      | false    | true         || ["${escapeJava(gnomeTerm.absolutePath)} -- \$command"]
        true          | false      | true     | true         || ["${escapeJava(terminator.absolutePath)} -x \$command ';' sleep 10d", "${escapeJava(xterm.absolutePath)} -hold -e \$command", "${escapeJava(gnomeTerm.absolutePath)} -- \$command"]
        false         | true       | true     | false        || ["${escapeJava(konsole.absolutePath)} --hold -e \$command", "${escapeJava(xterm.absolutePath)} -hold -e \$command"]
    }

    @Unroll
    def "default terminal command should properly evaluate for #command"() {
        given:
        locationService.getProgramBinary(command) >> commandPath

        expect:
        applyTemplate(instance.getDefaultTerminalCommand(), [command: 'mtr -n google.com']) == response

        where:
        command          | commandPath | response
        'none'           | null        | 'xterm -hold -e mtr -n google.com'
        'konsole'        | konsole     | "$konsole.absolutePath --hold -e mtr -n google.com"
        'terminator'     | terminator  | "$terminator.absolutePath -x mtr -n google.com ';' sleep 10d"
        'xterm'          | xterm       | "$xterm.absolutePath -hold -e mtr -n google.com"
        'gnome-terminal' | gnomeTerm   | "$gnomeTerm.absolutePath -- mtr -n google.com"
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
        locationService.getProgramBinary("traceroute") >> { hasTraceroute ? traceroute : null }
        locationService.getProgramBinary("mtr") >> { hasMtr ? mtr : null }

        when:
        def list = instance.getPossibleTracerouteCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultTracerouteCommand()

        then:
        defaultCommand == list[0]

        where:
        hasTraceroute | hasMtr || expectedList
        false         | false  || ["traceroute\${options.trim()?' '+options:''} \${server.address}"]
        true          | true   || ["${escapeJava(mtr.absolutePath)}\${options.trim()?' '+options:''} \${server.address}", "${escapeJava(traceroute.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
        false         | true   || ["${escapeJava(mtr.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
        true          | false  || ["${escapeJava(traceroute.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
    }

    @Unroll
    def "default traceroute command should properly evaluate"() {
        given:
        locationService.getProgramBinary(command) >> commandPath

        expect:
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '']) == response
        applyTemplate(instance.getDefaultTracerouteCommand(), [server: server, options: '-c 3']) == response2

        where:
        command      | commandPath | response                              | response2
        'none'       | null        | 'traceroute google.com'               | 'traceroute -c 3 google.com'
        'traceroute' | traceroute  | "$traceroute.absolutePath google.com" | "$traceroute.absolutePath -c 3 google.com"
        'mtr'        | mtr         | "$mtr.absolutePath google.com"        | "$mtr.absolutePath -c 3 google.com"
    }

    @Unroll
    def "should provide nslookup command list proper for given machine"() {
        given:
        locationService.getProgramBinary("nslookup") >> { hasNslookup ? nslookup : null }
        locationService.getProgramBinary("dig") >> { hasDig ? dig : null }

        when:
        def list = instance.getPossibleNslookupCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultNslookupCommand()

        then:
        defaultCommand == list[0]

        where:
        hasNslookup | hasDig || expectedList
        false       | false  || ["nslookup\${options.trim()?' '+options:''} \${server.address}"]
        true        | true   || ["${escapeJava(nslookup.absolutePath)}\${options.trim()?' '+options:''} \${server.address}", "${escapeJava(dig.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
        false       | true   || ["${escapeJava(dig.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
        true        | false  || ["${escapeJava(nslookup.absolutePath)}\${options.trim()?' '+options:''} \${server.address}"]
    }

    @Unroll
    def "default nslookup command should properly evaluate"() {
        given:
        locationService.getProgramBinary(command) >> commandPath

        expect:
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '']) == response
        applyTemplate(instance.getDefaultNslookupCommand(), [server: server, options: '-c 3']) == response2

        where:
        command    | commandPath | response                            | response2
        'none'     | null        | 'nslookup google.com'               | 'nslookup -c 3 google.com'
        'nslookup' | nslookup    | "$nslookup.absolutePath google.com" | "$nslookup.absolutePath -c 3 google.com"
        'dig'      | dig         | "$dig.absolutePath google.com"      | "$dig.absolutePath -c 3 google.com"
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
        locationService.getProgramBinary("rdesktop") >> { hasRdesktop ? rdesktop : null }
        locationService.getProgramBinary("xfreerdp") >> { hasXfreerdp ? xfreerdp : null }

        when:
        def list = instance.getPossibleRdpCommands()

        then:
        list == expectedList

        when:
        def defaultCommand = instance.getDefaultRdpCommand()

        then:
        defaultCommand == list[0]

        where:
        hasRdesktop | hasXfreerdp || expectedList
        false       | false       || ["rdesktop\${user.trim()?' -u \"'+user+'\"':''}\${password.trim()?' -p \"'+password+'\"':''}\${options.trim()?' '+options:''} \${server.address}\${port>0?':'+port:''}"]
        true        | true        || ["${escapeJava(rdesktop.absolutePath)}\${user.trim()?' -u \"'+user+'\"':''}\${password.trim()?' -p \"'+password+'\"':''}\${options.trim()?' '+options:''} \${server.address}\${port>0?':'+port:''}", "${escapeJava(xfreerdp.absolutePath)}\${user.trim()?' /u:\"'+user+'\"':''}\${password.trim()?' /p:\"'+password+'\"':''}\${options.trim()?' '+options:''} /v:\${server.address}\${port>0?':'+port:''}"]
        false       | true        || ["${escapeJava(xfreerdp.absolutePath)}\${user.trim()?' /u:\"'+user+'\"':''}\${password.trim()?' /p:\"'+password+'\"':''}\${options.trim()?' '+options:''} /v:\${server.address}\${port>0?':'+port:''}"]
        true        | false       || ["${escapeJava(rdesktop.absolutePath)}\${user.trim()?' -u \"'+user+'\"':''}\${password.trim()?' -p \"'+password+'\"':''}\${options.trim()?' '+options:''} \${server.address}\${port>0?':'+port:''}"]
    }

    @Unroll
    def "default rdp command should properly evaluate for #command / '#user' / #password / #port / '#options'"() {
        given:
        locationService.getProgramBinary('rdesktop') >> { command == 'rdesktop' ? rdesktop : null }
        locationService.getProgramBinary('xfreerdp') >> { command == 'xfreerdp' ? xfreerdp : null }

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
        'rdesktop' | ''          | ''         | 0    | ''                          || "$rdesktop.absolutePath google.com"
        'rdesktop' | '\t '       | ' '        | 0    | ' \t \t '                   || "$rdesktop.absolutePath google.com"
        'rdesktop' | 'user.name' | ' '        | 0    | ' '                         || "$rdesktop.absolutePath -u \"user.name\" google.com"
        'rdesktop' | ''          | 'password' | 0    | ' '                         || "$rdesktop.absolutePath -p \"password\" google.com"
        'rdesktop' | ''          | ''         | 1022 | ''                          || "$rdesktop.absolutePath google.com:1022"
        'rdesktop' | ''          | ''         | 0    | '-D -L 1234:localhost:4321' || "$rdesktop.absolutePath -D -L 1234:localhost:4321 google.com"
        'rdesktop' | 'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || "$rdesktop.absolutePath -u \"user.name\" -p \"password\" -D -L 1234:localhost:4321 google.com:1222"
        'xfreerdp' | ''          | ''         | 0    | ''                          || "$xfreerdp.absolutePath /v:google.com"
        'xfreerdp' | '\t '       | ' '        | 0    | ' \t \t '                   || "$xfreerdp.absolutePath /v:google.com"
        'xfreerdp' | 'user.name' | ' '        | 0    | ' '                         || "$xfreerdp.absolutePath /u:\"user.name\" /v:google.com"
        'xfreerdp' | ''          | 'password' | 0    | ' '                         || "$xfreerdp.absolutePath /p:\"password\" /v:google.com"
        'xfreerdp' | ''          | ''         | 1022 | ''                          || "$xfreerdp.absolutePath /v:google.com:1022"
        'xfreerdp' | ''          | ''         | 0    | '-D -L 1234:localhost:4321' || "$xfreerdp.absolutePath -D -L 1234:localhost:4321 /v:google.com"
        'xfreerdp' | 'user.name' | 'password' | 1222 | '-D -L 1234:localhost:4321' || "$xfreerdp.absolutePath /u:\"user.name\" /p:\"password\" -D -L 1234:localhost:4321 /v:google.com:1222"
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
