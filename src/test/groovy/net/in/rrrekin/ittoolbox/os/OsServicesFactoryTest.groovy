package net.in.rrrekin.ittoolbox.os

import net.in.rrrekin.ittoolbox.infrastructure.SystemWrapper
import net.in.rrrekin.ittoolbox.utilities.ProgramLocationService
import spock.lang.Specification
import spock.lang.Unroll
/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesFactoryTest extends Specification {

    ProgramLocationService locationService = Stub()
    SystemWrapper system = Mock()
    def instance = new OsServicesFactory(locationService, system)

    def "should validate constructor arguments"() {
        when:
        new OsServicesFactory(null, system)
        then:
        thrown NullPointerException

        when:
        new OsServicesFactory(locationService, null)
        then:
        thrown NullPointerException
    }

    def "should validate create() argument"() {
        when:
        instance.create(null)
        then:
        thrown NullPointerException
    }

    @Unroll
    def "should create proper implementation for #os operating system"() {
        expect:
        instance.create(os).class == clazz

        when:
        def instance = instance.create()

        then:
        1 * system.getProperty('os.name') >> os
        instance.class == clazz

        where:
        os              | clazz
        'Linux'         | OsServicesLinuxImpl
        'Windows 95'    | OsServicesWindowsImpl
        'Windows 98'    | OsServicesWindowsImpl
        'Windows Me'    | OsServicesWindowsImpl
        'Windows NT'    | OsServicesWindowsImpl
        'Windows 2000'  | OsServicesWindowsImpl
        'Windows XP'    | OsServicesWindowsImpl
        'Windows 2003'  | OsServicesWindowsImpl
        'Windows Vista' | OsServicesWindowsImpl
        'Windows 7'     | OsServicesWindowsImpl
        'Windows 8'     | OsServicesWindowsImpl
        'Windows 10'    | OsServicesWindowsImpl
        'Mac OS'        | OsServicesMacOsImpl
        'Mac OS X'      | OsServicesMacOsImpl
        'Solaris'       | OsServicesDefaultImpl
        'SunOS'         | OsServicesDefaultImpl
        'HP-UX'         | OsServicesDefaultImpl
        'AIX'           | OsServicesDefaultImpl
        'FreeBSD'       | OsServicesDefaultImpl
        'OS/390'        | OsServicesDefaultImpl
    }
}
