package eu.joaocosta.minart.extra

import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._

object Geom {
  def renderRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    val ops = for {
      x <- x1 to x2
      y <- y1 to y2
    } yield CanvasIO.putPixel(x, y, color)
    CanvasIO.sequence_(ops)
  }

  def renderCircle(cx: Int, cy: Int, innerRadius: Int, outerRadius: Int, color: Color): CanvasIO[Unit] = {
    val sqIn = innerRadius * innerRadius
    val sqOut = outerRadius * outerRadius
    val ops = for {
      x <- (cx - outerRadius) to (cx + outerRadius)
      y <- (cy - outerRadius) to (cy + outerRadius)
      sqDist = math.pow(x - cx, 2) + math.pow(y - cy, 2)
      if (sqDist >= sqIn && sqDist < sqOut)
    } yield CanvasIO.putPixel(x, y, color)
    CanvasIO.sequence_(ops)
  }

  def renderLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    if (x1 == x2) CanvasIO.sequence_((y1 to y2).map(y => CanvasIO.putPixel(x1, y, color)))
    else if (y1 == y2) CanvasIO.sequence_((x1 to x2).map(x => CanvasIO.putPixel(x, y2, color)))
    else {
      val dx = x2 - x1
      val dy = y2 - y1
      lazy val dxStep = if (dx >= 0) 1 else -1
      lazy val dyStep = if (dy >= 0) 1 else -1
      if (math.abs(dx) >= math.abs(dy))
        CanvasIO.sequence_(
          (x1 to x2 by dxStep).map(x => CanvasIO.putPixel(x, y1 + dy * (x - x1) / dx, color)))
      else
        CanvasIO.sequence_(
          (y1 to y2 by dyStep).map(y => CanvasIO.putPixel(x1 + dx * (y - y1) / dy, y, color)))
    }
  }

  def renderStroke(x1: Int, y1: Int, x2: Int, y2: Int, stroke: Int, color: Color): CanvasIO[Unit] = {
    val dx = math.abs(x2 - x1)
    val dy = math.abs(y2 - y1)
    CanvasIO.sequence_(((-stroke / 2) to (stroke / 2)).map(d =>
      if (dx <= dy) renderLine(x1 + d, y1, x2 + d, y2, color)
      else renderLine(x1, y1 + d, x2, y2 + d, color)))
  }
}
