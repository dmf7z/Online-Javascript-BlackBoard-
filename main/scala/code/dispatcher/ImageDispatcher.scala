package code.dispatcher

import _root_.code.comet.BlackBoardServer
import _root_.code.util.ImageHelper.{ImageHelper, ImageOutFormat}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.common.{Empty, Box, Full}
import java.awt.image.BufferedImage
import java.util.Calendar

/**
 * Created by IntelliJ IDEA.
 * User: dfernandez
 * Date: 05/05/2011
 * Time: 15:33:26
 * To change this template use File | Settings | File Templates.
 */

class ImageDispatcher

object ImageDispatcher {

  object DBImage {
    def unapply(id: String): Option[BufferedImage] =
      BlackBoardServer.getImage(id);
  }

  def matcher: LiftRules.DispatchPF = {
    case req @ Req("image" :: DBImage(img) :: Nil, _, GetRequest) =>
      () => serveImage(img, req)
  }

  def serveImage(img: BufferedImage, req: Req) : Box[LiftResponse] = {
    //TODO: see how to avoid serving image twice
      val c = Calendar.getInstance();
      val imageBytes = ImageHelper.imageToByteArray(ImageOutFormat.png, img);
      Full(InMemoryResponse(
        imageBytes,
        List("Last-Modified" -> toInternetDate(c.getTime()),
          "Content-Type" -> "image/png",
          "Content-Length" -> imageBytes.length.toString),
        Nil /*cookies*/,
        200)
        )
  }

}