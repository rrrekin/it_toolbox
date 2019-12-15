package net.in.rrrekin.ittoolbox.infrastructure

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author michal.rudewicz@gmail.com
 */
@Ignore("Manual experiments only")
class BlockingApplicationEventsHandlerTest extends Specification {


  def "mock test"() {
    expect:
    true
  }

  def "should displayAlert"() {
    setup:
    Platform.startup({ println 'Starting' })

    when:
    Platform.runLater({
      def result = BlockingApplicationEventsHandler.showAlert(Alert.AlertType.CONFIRMATION, "msg", "tit")
      if(result.orElse(ButtonType.CANCEL).buttonData == ButtonBar.ButtonData.OK_DONE) {
        println 'OK'
      }else{
        println "CANCEL"
      }
      println result
    })
    sleep 10000
// Optional[ButtonType [text=OK, buttonData=OK_DONE]]
    // Optional[ButtonType [text=Cancel, buttonData=CANCEL_CLOSE]]
    // Optional.empty
    then:
    true
  }
}
