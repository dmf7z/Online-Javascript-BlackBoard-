var paint = false;
var wbOffsetLeft;
var wbOffsetTop;
var lastx = -1;
var lasty = -1;
var _lastx = -1;
var _lasty = -1;
var imgpbupdate = {"id": "", "listx": [], "listy": [] }
var $blackboard;
var pvalue = 0;

function init(){
    $blackboard = $('#blackboard');
    $progressbar = $('#progressbar'); 
    wbOffsetLeft = $blackboard.offset().left;
    wbOffsetTop = $blackboard.offset().top;
    $blackboard.hide();
    $('#clearbtn').hide();
    $progressbar.progressbar({
        value: pvalue,
        complete: function(event, ui) { $(this).hide(); $blackboard.show(); $('#clearbtn').show(); }
    });
    var now = new Date();
    imgpbupdate.id =  now.getTime();
    $(window).resize(function(e) {
       wbOffsetLeft = $blackboard.offset().left;
       wbOffsetTop = $blackboard.offset().top;
    });
    var x,y;
    for (x=0;x<16;x++){
        for (y=0;y<8;y++){
           var id = x + '_' + y;
           var $partboard = $('<img id="img' + id + '" style="left: ' + (x*40) + 'px; top: ' + (y*40) + 'px;" class="imgpartboard" src="/image/' + id + '"/>')
           $partboard.load(function(){
                pvalue += (100/128);
                $progressbar.progressbar( "value" , pvalue )
           })
           $blackboard.append($partboard)
        }
    }
    loadblackboardevents();
}
function sendData(e){
    e.preventDefault();
    paint = false;
    if(imgpbupdate.listx.length>0)
    updateImageListeners(JSON.stringify(imgpbupdate));
    imgpbupdate.listx = [];
    imgpbupdate.listy = [];
    var now = new Date();
    imgpbupdate.id =  now.getTime();
    lastx = -1;
    lasty = -1;
}

function loadblackboardevents() {
     if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i)) || (navigator.userAgent.match(/iPad/i)))
        $blackboard.bind('touchmove', function(e) {
            e.preventDefault();
            var x = e.originalEvent.touches[0].pageX;
            var y = e.originalEvent.touches[0].pageY;
            if (x>0 && y>0 && x<($blackboard.width()+wbOffsetLeft) && y<($blackboard.height()+wbOffsetTop))
                paintboard(this, x, y);
            else
                sendData(e);
        }).bind('touchstart', function(e) {
            e.preventDefault();
            paint = true;
            paintboard(this, e.originalEvent.touches[0].pageX, e.originalEvent.touches[0].pageY);
        }).bind('touchend', function(e){
            sendData(e);
        }).mouseleave(function(e) {
           sendData(e);
        });
    else
        $blackboard.mousemove(function(e) {
           e.preventDefault();
           paintboard(this, e.pageX, e.pageY);
        }).mousedown(function(e) {
           e.preventDefault();
           paint = true;
           paintboard(this, e.pageX, e.pageY);
        }).mouseup(function(e) {
           sendData(e);
        }).mouseleave(function(e) {
           sendData(e);
        });
}
function paintboard(elem, x0, y0){
    if(paint){
        var x = x0 - wbOffsetLeft;
        var y = y0 - wbOffsetTop;
        if(x>0 && y>0){
            imgpbupdate.listx.push(x);
            imgpbupdate.listy.push(y);
        }
        var points = [];
        if(lastx!=-1 && lasty!=-1)
            points = lineBresenham(lastx, lasty, x, y);
        else
            points.push({"x": x, "y": y});
        drawpoints(points);
        lastx = x;
        lasty = y;
    }
}
function lineBresenham(x0, y0, x1, y1)
{
  var points = []
  var dy = y1 - y0;
  var dx = x1 - x0;
  var stepx, stepy;
  if (dy < 0)
  {
    dy = -dy;
    stepy = -1;
  }
  else
  {
    stepy = 1;
  }
  if (dx < 0)
  {
    dx = -dx;
    stepx = -1;
  }
  else
  {
    stepx = 1;
  }
  dy <<= 1;
  dx <<= 1;
  points.push({"x": x0, "y": y0});
  if (dx > dy)
  {
    fraction = dy - (dx >> 1);
    while (x0 != x1)
    {
      if (fraction >= 0)
      {
        y0 += stepy;
        fraction -= dx;
      }
      x0 += stepx;
      fraction += dy;
      if(isInBlackBoard(x0,y0))
        points.push({"x": x0, "y": y0});
      else
        return points;
    }
  }
  else
  {
    fraction = dx - (dy >> 1);
    while (y0 != y1)
    {
      if (fraction >= 0)
      {
        x0 += stepx;
        fraction -= dy;
      }
      y0 += stepy;
      fraction += dx;
      if(isInBlackBoard(x0, y0))
        points.push({"x": x0, "y": y0});
      else
        return points;
    }
  }
  return points;
}
function isInBlackBoard(x,y){
    var wbOffsetWidth = $blackboard.width();
    var wbOffsetHeight = $blackboard.height();
    return (x < wbOffsetWidth-1 && x > 1 && y < wbOffsetHeight-1 && y > 1);
}
function drawpoints(points){
    var divs = "";
    var i;
    for (i=0;i<points.length;i++){
        divs += '<div class="simple pt' + imgpbupdate.id  + '" style="left: ' + points[i].x + 'px; top: ' + points[i].y + 'px;"/>'
    }
    $blackboard.prepend(divs);
}
function createcolor(){
    return '#'+(0x1000000+(Math.random())*0xffffff).toString(16).substr(1,6);
}
function reloadimage(reload){
   var ids = reload.ids;
   var i;
   for (i=0;i<ids.length;i++){
   if( $('#img'+ids[i]).length>0) {
      var now = new Date();
      $('#img'+ ids[i]).attr('src', '/image/' + ids[i] + '?' + now.getTime());
    }
   }
   $('#img'+ ids[ids.length-1]).load(function(){
		$('.pt' + reload.id).remove();
	})
}
function clearblackboard(){
  $blackboard.children('img').attr('src', '/images/black.png');
}
