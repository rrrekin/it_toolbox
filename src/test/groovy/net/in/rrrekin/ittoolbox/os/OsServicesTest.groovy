package net.in.rrrekin.ittoolbox.os


import spock.lang.Specification
/**
 * @author michal.rudewicz@gmail.com
 */
class OsServicesTest extends Specification {

    def "all constants should be defined"() {
        expect:
        println "OS_NAME: $OsServices.OS_NAME"
        println "OS_VERSION: $OsServices.OS_VERSION"
        println "OS_ARCH: $OsServices.OS_ARCH"
        println "USER_NAME: $OsServices.USER_NAME"
        println "USER_HOME: $OsServices.USER_HOME"

        OsServices.OS_NAME != null
        OsServices.OS_VERSION != null
        OsServices.OS_ARCH != null
        OsServices.USER_NAME != null
        OsServices.USER_HOME != null

        OsServices.OS_NAME == System.getProperty("os.name")
        OsServices.OS_VERSION == System.getProperty("os.version")
        OsServices.OS_ARCH == System.getProperty("os.arch")
        OsServices.USER_HOME == System.getProperty("user.home")
        OsServices.USER_NAME == System.getProperty("user.name")
    }
}
