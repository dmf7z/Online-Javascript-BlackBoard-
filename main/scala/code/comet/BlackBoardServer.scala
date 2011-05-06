package code.comet


import net.liftweb._
import common.{Empty, Box, Full}
import http._
import actor._
import json.JsonAST.JObject
import net.liftweb.json._
import collection.mutable.{ListBuffer, HashSet, HashMap}
import java.awt.{AlphaComposite, Color}
import java.awt.image.{IndexColorModel, BufferedImage}
import code.util.ImageHelper.ImageHelper

/**
 * Created by IntelliJ IDEA.
 * User: dfernandez
 * Date: 05/05/2011
 * Time: 15:15:37
 * To change this template use File | Settings | File Templates.
 */


case class DrawPointAction(sessionId: String, pbUpdate: PbUpdate)
case class CursorMovedAction(sessionId: String, cursorUpdate: CursorUpdate)
case class ImageDrawPointAction(imgPbUpdate: ImgPbUpdate)
case class ReloadImageAction(id: String, ids: List[String])
case class Clear()

case class Point(x: Double, y: Double);
case class Update(part: String, points: List[Point])
case class PbUpdate(updates: List[Update])
case class CursorUpdate(x: Double, y: Double)
case class Cursor(id: String, x: Double, y: Double)

case class ImgPbUpdate(id: String, listx: Array[Int], listy: Array[Int])

object BlackBoardServer extends LiftActor with ListenerManager {

  private var action = new Object()

  private var bbwidth = 360;
  private var bbheight = 440;
  private var imgwidth = 40;
  private var imgheight = 40;

  //TODO: dejar Images?
  private var mainImage : BufferedImage = null;
  private var images = new HashMap[String, BufferedImage]()
  initImages();


  def createUpdate =  action

  def initImages(){
     mainImage = ImageHelper.imageToBufferedImage(ImageHelper.transformGrayToTransparency(new BufferedImage(bbwidth, bbheight, BufferedImage.TYPE_INT_RGB)), bbwidth, bbheight);
    val max_x = (bbwidth/imgwidth) - 1;
    val max_y = (bbheight/imgheight) - 1;
    for (x <- 0 to max_x){
      for (y <- 0 to max_y){
        val bimage = mainImage.getSubimage(x * imgwidth, y * imgheight, imgwidth, imgheight);
        images(x + "_" + y) = bimage;
      }
    }
  }

  override def highPriority = {
    case cursorMovedAction: CursorMovedAction =>{
      action = cursorMovedAction;
      updateListeners();
    }
    case ImageDrawPointAction(imgPbUpdate) =>{
      val gp = mainImage.createGraphics();
      gp.setBackground(Color.WHITE);

      val imagesProcessed = ListBuffer[String]()

      def addImageProcessed(x: Int, y: Int){
          val xpart = Math.ceil(x/imgwidth).toInt;
          val ypart = Math.ceil(y/imgheight).toInt;
          val id = xpart + "_" + ypart;
          if(!imagesProcessed.contains(id)){
            images(id) = mainImage.getSubimage(xpart * imgwidth, ypart * imgheight, imgwidth, imgheight);
            imagesProcessed.append(id);
        }
      }

      if(imgPbUpdate.listx.size == 1)   {
        val x = imgPbUpdate.listx(0);
        val y = imgPbUpdate.listy(0);
        gp.fillRect(x,y,2,2) ;
        addImageProcessed(x,y);
      }
      else{
        for (i <- 0 to imgPbUpdate.listx.size - 1){
          val x0 = imgPbUpdate.listx(i);
          val y0 = imgPbUpdate.listy(i);
          if(i<imgPbUpdate.listx.size - 1){
            val x1 = imgPbUpdate.listx(i+1);
            val y1 = imgPbUpdate.listy(i+1);
            ImageHelper.createBresenhamLine(gp, addImageProcessed, x0, y0, x1, y1);
          }
        }
      }
      action = new ReloadImageAction(imgPbUpdate.id, imagesProcessed.toList)
      updateListeners();

    }
    case clear:Clear  => {
      initImages();
      action = clear
      updateListeners();
    }
  }

  def getImage(id: String): Box[BufferedImage] = {
    if(images.contains(id))
      Full(images(id));
    else
      Empty;
  }

}