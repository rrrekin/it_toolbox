package net.in.rrrekin.ittoolbox.utilities

import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
class LocaleUtilTest extends Specification {

  void setup() {
    LocaleUtil.setLocale(Locale.forLanguageTag('pl'))
  }

  void cleanup() {
    // Reset locale to default
    LocaleUtil.setLocale(null)
  }

  def "should set and reset locale"() {
    given:
    LocaleUtil.setLocale(null)

    expect:
    LocaleUtil.localeCode == null
    Locale.getDefault() == LocaleUtil.@systemLocale

    when:
    LocaleUtil.setLocale(Locale.GERMAN)
    then:
    LocaleUtil.localeCode == 'de'
    Locale.getDefault() == Locale.GERMAN

    when:
    LocaleUtil.setLocale(null)
    then:
    LocaleUtil.localeCode == null
    Locale.getDefault() == LocaleUtil.@systemLocale
  }

  def "should return resource bundles for local and english messages"() {
    given:
    LocaleUtil.setLocale(Locale.forLanguageTag('pl'))
    expect:
    LocaleUtil.messages.locale == Locale.forLanguageTag('pl')
    LocaleUtil.enMessages.locale == Locale.forLanguageTag('en')
  }

  def "should return defined language list"() {
    expect:
    LocaleUtil.getSupportedLanguages() == ['en', 'pl'] as String[]
  }

  def "should return localized messages"() {
    given:
    def arg = 'abc'
    def code = 'EX_MISSING_CFG_FILE'
    def nonExisting = 'NON_EXISTING_TEXT_MESSAGE'

    expect:
    LocaleUtil.localMessage(code) == 'Brak pliku konfiguracyjnego ({0})'
    LocaleUtil.localMessage(nonExisting) == nonExisting
    LocaleUtil.localMessage(code, arg) == 'Brak pliku konfiguracyjnego (abc)'
    LocaleUtil.localMessage(nonExisting, arg) == nonExisting
  }

  def "should return english messages"() {
    given:
    def arg = 'abc'
    def code = 'EX_MISSING_CFG_FILE'
    def nonExisting = 'NON_EXISTING_TEXT_MESSAGE'

    expect:
    LocaleUtil.enMessage(code) == 'Missing configuration file ({0})'
    LocaleUtil.enMessage(nonExisting) == nonExisting
    LocaleUtil.enMessage(code, arg) == 'Missing configuration file (abc)'
    LocaleUtil.enMessage(nonExisting, arg) == nonExisting
  }

  def "should return localized single character properties"() {
    given:
    def code = 'EX_MISSING_CFG_FILE'
    def nonExisting = 'NON_EXISTING_TEXT_MESSAGE'

    expect:
    LocaleUtil.localCharacter(code) == 'B' as Character
    LocaleUtil.localCharacter(nonExisting) == null
  }
}
