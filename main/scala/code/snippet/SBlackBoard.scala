package code.snippet


import net.liftweb.util._
import Helpers._
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JsCmds.{Noop, Script, Function}
import net.liftweb.http.js.JE.{JsVar, JsRaw}
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.{JsCmd}
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.DefaultFormats
import net.liftweb.common.Full
import net.liftweb.json.JsonParser._
import xml.Text
import code.comet._

/**
 * Created by IntelliJ IDEA.
 * User: dfernandez
 * Date: 05/05/2011
 * Time: 15:14:08
 * To change this template use File | Settings | File Templates.
 */

class SBlackBoard {

  implicit val formats = DefaultFormats

  def registerUpdateListenersScript() = {

    def updateCursor(cursorupdate: String): JsCmd = {
      parse(cursorupdate) match {
        case jObject: JObject => {
          val sessionId = S.session.open_!.uniqueId
          val cursorUpdate = jObject.extract[CursorUpdate]
          BlackBoardServer ! new CursorMovedAction(sessionId, cursorUpdate)
        }
        case _ =>
      }
      Noop;
    }

    def updateImageListeners(imgpbupadteStr: String): JsCmd = {
      parse(imgpbupadteStr) match {
        case jObject: JObject => {
          val imgPbUpdate = jObject.extract[ImgPbUpdate]
          BlackBoardServer ! new ImageDrawPointAction(imgPbUpdate)
        }
        case _ =>
      }
      Noop;
    }

    val fn1 = Function("updateCursor", List("cursorupdate"), SHtml.ajaxCall(JsRaw("cursorupdate"), updateCursor)._2)
    val fn2 = Function("updateImageListeners", List("imgpbupadte"), SHtml.ajaxCall(JsRaw("imgpbupadte"), updateImageListeners)._2)

    "#sc" #> Script(fn1  & fn2)
  }

  def clearBtn() = {

    def clearBlackBoard(): JsCmd = {
      BlackBoardServer ! new Clear();
      Noop;
    }

    "#clearbtn *" #> SHtml.ajaxButton(Text("Clear BlackBoard"), clearBlackBoard _)
  }


}