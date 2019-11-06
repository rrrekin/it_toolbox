package net.in.rrrekin.ittoolbox.os

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesFactoryTest extends Specification {

    @Unroll
    def "should create proper implementation for #os operating system"() {
        expect:
        OsServicesFactory.create(os).class == clazz

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
