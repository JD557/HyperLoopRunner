package eu.joaocosta.minart.extra

import scala.io.Source
import scala.util.Try

import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._

case class Image(pixels: Vector[Vector[Color]]) {

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, mask: Option[Color]): Unit =
    for {
      iy <- (0 until pixels.length)
      ix <- (0 until pixels(iy).length)
      if (x >= 0 && y >= 0)
    } {
      val pixel = pixels(iy)(ix)
      if (!mask.contains(pixel)) canvas.putPixel(x + ix, y + iy, pixel)
    }

  def renderUnsafe(canvas: Canvas, x: Int, y: Int): Unit = renderUnsafe(canvas, x, y, None)

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int, mask: Option[Color]): Unit =
    for {
      iy <- (0 until pixels.length)
      if iy >= cy && iy < cy + ch
      ix <- (0 until pixels(iy).length)
      if ix >= cx && ix < cx + cw
      if (x >= 0 && y >= 0)
    } {
      val pixel = pixels(iy)(ix)
      if (!mask.contains(pixel)) canvas.putPixel(x + ix - cx, y + iy - cy, pixel)
    }

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int): Unit =
    renderUnsafe(canvas, x, y, cx, cy, cw, ch, None)

  def render(x: Int, y: Int, mask: Option[Color]): CanvasIO[Unit] = {
    val ops = (for {
      (line, iy) <- pixels.zipWithIndex
      (pixel, ix) <- line.zipWithIndex
      if (x >= 0 && y >= 0)
      if !mask.contains(pixel)
    } yield CanvasIO.putPixel(x + ix, y + iy, pixel))
    CanvasIO.sequence_(ops)
  }

  def render(x: Int, y: Int): CanvasIO[Unit] = render(x, y, None)

  def render(x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int, mask: Option[Color]): CanvasIO[Unit] = {
    val ops = (for {
      (line, iy) <- pixels.zipWithIndex
      (pixel, ix) <- line.zipWithIndex
      if (x >= 0 && y >= 0)
      if ix >= cx && ix < cx + cw && iy >= cy && iy < cy + ch
      if !mask.contains(pixel)
    } yield CanvasIO.putPixel(x + ix - cx, y + iy - cy, pixel))
    CanvasIO.sequence_(ops)
  }

  def render(x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int): CanvasIO[Unit] =
    render(x, y, cx, cy, cw, ch, None)

  lazy val invert =
    Image(pixels.map(_.map(c => Color(255 - c.r, 255 - c.g, 255 - c.b))))

  lazy val flipH = Image(pixels.map(_.reverse))

  lazy val flipV = Image(pixels.reverse)
}

object Image {
  val empty: Image = Image(Vector.empty)

  def loadPpmImage(resource: Source): Try[Image] = Try {
    val it = resource.getLines().filterNot(_.startsWith("#")).flatMap(_.split(" "))
    val builder = Vector.newBuilder[Color]
    require(it.next() == "P3", "Invalid image header")
    val width = it.next().toInt
    val height = it.next().toInt
    require(it.next() == "255", "Invalid color range")
    (0 until (width * height)).foreach { _ =>
      val color = Color(it.next().toInt, it.next().toInt, it.next().toInt)
      builder += color
    }
    val pixels = builder.result().sliding(width, width).map(_.toVector).toVector
    Image(pixels)
  }
}
