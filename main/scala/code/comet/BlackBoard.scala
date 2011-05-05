package code.comet

import net.liftweb._
import http._
import js.JE.{JsObj, JsArray, JsRaw}
import json.DefaultFormats
import net.liftweb.http.js.JsCmds.OnLoad
import net.liftweb.json.Serialization.write

/**
 * Created by IntelliJ IDEA.
 * User: dfernandez
 * Date: 05/05/2011
 * Time: 15:16:51
 * To change this template use File | Settings | File Templates.
 */

/**
 * The screen real estate on the browser will be represented
 * by this component.  When the component changes on the server
 * the changes are automatically reflected in the browser.
 */
class BlackBoard extends CometActor with CometListener {

  /**
   * When the component is instantiated, register as
   * a listener with the ChatServer
   */
  def registerWith = BlackBoardServer

  def render = <br/>

  implicit val formats = DefaultFormats

  /**
   * The CometActor is an Actor, so it processes messages.
   * In this case, we're listening for Vector[String],
   * and when we get one, update our private state
   * and reRender() the component.  reRender() will
   * cause changes to be sent to the browser.
   */
  override def highPriority = {
    case drawPointAction: DrawPointAction =>{
      val sessionId = S.session.open_!.uniqueId
      if(sessionId != drawPointAction.sessionId)
        this.partialUpdate(OnLoad(JsRaw("updatepoints(" + write(drawPointAction.pbUpdate.updates)  + ")").cmd))
    }
    case cursorMovedAction: CursorMovedAction =>{
      val sessionId = S.session.open_!.uniqueId
      if(sessionId != cursorMovedAction.sessionId)
        this.partialUpdate(OnLoad(JsRaw("movecursor(" + write(new Cursor(cursorMovedAction.sessionId, cursorMovedAction.cursorUpdate.x, cursorMovedAction.cursorUpdate.y)) + ")").cmd))
    }
    case reloadImageAction: ReloadImageAction =>{
      this.partialUpdate(OnLoad(JsRaw("reloadimage(" + write(reloadImageAction)  + ")").cmd))
    }
    case clear:Clear  => {
      this.partialUpdate(OnLoad(JsRaw("clearblackboard()").cmd))
    }
  }

}