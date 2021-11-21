package eu.joaocosta.minart.extra

import eu.joaocosta.minart.graphics.Color
import eu.joaocosta.minart.graphics.pure._

object Geom {
  def renderRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    val xRange = x1 to x2
    val yRange = y1 to y2
    val pixels = () => for {
      x <- xRange.iterator
      y <- yRange.iterator
    } yield (x, y)
    CanvasIO.foreach(pixels) { case (x, y) => CanvasIO.putPixel(x, y, color) }
  }

  def renderRect_(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    val xRange = x1 to x2
    val yRange = y1 to y2
    val pixels = () => for {
      x <- xRange.iterator
      y <- yRange.iterator
    } yield (x, y)
    CanvasIO.accessCanvas { canvas =>
      pixels().foreach { case (x, y) => canvas.putPixel(x, y, color) }
    }
  }

  def renderCircle(cx: Int, cy: Int, innerRadius: Int, outerRadius: Int, color: Color): CanvasIO[Unit] = {
    val sqIn = innerRadius * innerRadius
    val sqOut = outerRadius * outerRadius
    val xRange = (cx - outerRadius) to (cx + outerRadius)
    val yRange = (cy - outerRadius) to (cy + outerRadius)
    val pixels = () => for {
      x <- xRange.iterator
      y <- yRange.iterator
      sqDist = math.pow(x - cx, 2) + math.pow(y - cy, 2)
      if (sqDist >= sqIn && sqDist < sqOut)
    } yield (x, y)
    CanvasIO.foreach(pixels) { case (x, y) => CanvasIO.putPixel(x, y, color) }
  }

  def renderCircle_(cx: Int, cy: Int, innerRadius: Int, outerRadius: Int, color: Color): CanvasIO[Unit] = {
    val sqIn = innerRadius * innerRadius
    val sqOut = outerRadius * outerRadius
    val xRange = (cx - outerRadius) to (cx + outerRadius)
    val yRange = (cy - outerRadius) to (cy + outerRadius)
    val pixels = () => for {
      x <- xRange.iterator
      y <- yRange.iterator
      sqDist = math.pow(x - cx, 2) + math.pow(y - cy, 2)
      if (sqDist >= sqIn && sqDist < sqOut)
    } yield (x, y)
    CanvasIO.accessCanvas { canvas =>
      pixels().foreach { case (x, y) => canvas.putPixel(x, y, color) }
    }
  }

  def renderLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    if (x1 == x2) CanvasIO.foreach(y1 to y2)(y => CanvasIO.putPixel(x1, y, color))
    else if (y1 == y2) CanvasIO.foreach(x1 to x2)(x => CanvasIO.putPixel(x, y2, color))
    else {
      val dx = x2 - x1
      val dy = y2 - y1
      lazy val dxStep = if (dx >= 0) 1 else -1
      lazy val dyStep = if (dy >= 0) 1 else -1
      if (math.abs(dx) >= math.abs(dy))
        CanvasIO.foreach(x1 to x2 by dxStep)(x => CanvasIO.putPixel(x, y1 + dy * (x - x1) / dx, color))
      else
        CanvasIO.foreach(y1 to y2 by dyStep)(y => CanvasIO.putPixel(x1 + dx * (y - y1) / dy, y, color))
    }
  }

  def renderLine_(x1: Int, y1: Int, x2: Int, y2: Int, color: Color): CanvasIO[Unit] = {
    if (x1 == x2) CanvasIO.accessCanvas { canvas =>
      (y1 to y2).foreach(y => canvas.putPixel(x1, y, color))
    }
    else if (y1 == y2) CanvasIO.accessCanvas { canvas =>
      (x1 to x2).foreach(x => canvas.putPixel(x, y2, color))
    }
    else {
      val dx = x2 - x1
      val dy = y2 - y1
      lazy val dxStep = if (dx >= 0) 1 else -1
      lazy val dyStep = if (dy >= 0) 1 else -1
      if (math.abs(dx) >= math.abs(dy))
        CanvasIO.accessCanvas { canvas =>
          (x1 to x2 by dxStep).foreach(x => canvas.putPixel(x, y1 + dy * (x - x1) / dx, color))
        }
      else
        CanvasIO.accessCanvas { canvas => (y1 to y2 by dyStep).foreach(y => canvas.putPixel(x1 + dx * (y - y1) / dy, y, color)) }
    }
  }

  def renderStroke(x1: Int, y1: Int, x2: Int, y2: Int, stroke: Int, color: Color): CanvasIO[Unit] = {
    val dx = math.abs(x2 - x1)
    val dy = math.abs(y2 - y1)
    if (dx <= dy) CanvasIO.foreach((-stroke / 2) to (stroke / 2))(d => renderLine(x1 + d, y1, x2 + d, y2, color))
    else CanvasIO.foreach((-stroke / 2) to (stroke / 2))(d => renderLine(x1, y1 + d, x2, y2 + d, color))
  }

  def renderStroke_(x1: Int, y1: Int, x2: Int, y2: Int, stroke: Int, color: Color): CanvasIO[Unit] = {
    val dx = math.abs(x2 - x1)
    val dy = math.abs(y2 - y1)
    if (dx <= dy) CanvasIO.foreach((-stroke / 2) to (stroke / 2))(d => renderLine_(x1 + d, y1, x2 + d, y2, color))
    else CanvasIO.foreach((-stroke / 2) to (stroke / 2))(d => renderLine_(x1, y1 + d, x2, y2 + d, color))
  }
}
