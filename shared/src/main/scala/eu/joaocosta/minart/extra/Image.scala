package eu.joaocosta.minart.extra

import scala.io.Source
import scala.util.Try

import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._

case class Image(pixels: Vector[Array[Int]]) {

  val width = pixels.headOption.map(_.size).getOrElse(0)
  val height = pixels.size

  private[this] val lines = (0 until height)
  private[this] val columns = (0 until width)

  def getPixel(x: Int, y: Int): Option[Color] = {
    if (x < 0 || y < 0 || x >= width || y >= height) None
    else Some(Color.fromRGB(pixels(y)(x)))
  }

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, mask: Option[Color]): Unit =
    for {
      iy <- lines
      ix <- columns
      if (x >= 0 && y >= 0)
    } {
      val pixel = Color.fromRGB(pixels(iy)(ix))
      if (!mask.contains(pixel)) canvas.putPixel(x + ix, y + iy, pixel)
    }

  def renderUnsafe(canvas: Canvas, x: Int, y: Int): Unit = renderUnsafe(canvas, x, y, None)

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int, mask: Option[Color]): Unit =
    if (x >= 0 && y >= 0) {
      for {
        iy <- lines
        if iy >= cy && iy < cy + ch
        ix <- columns
        if ix >= cx && ix < cx + cw
      } {
        val pixel = Color.fromRGB(pixels(iy)(ix))
        if (!mask.contains(pixel)) canvas.putPixel(x + ix - cx, y + iy - cy, pixel)
      }
    }

  def renderUnsafe(canvas: Canvas, x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int): Unit =
    renderUnsafe(canvas, x, y, cx, cy, cw, ch, None)

  def render(x: Int, y: Int, mask: Option[Color]): CanvasIO[Unit] = {
    if (x >= 0 && y >= 0) {
      val ops = (for {
        iy <- lines
        ix <- columns
        pixel = Color.fromRGB(pixels(iy)(ix))
        if !mask.contains(pixel)
      } yield CanvasIO.putPixel(x + ix, y + iy, pixel))
      CanvasIO.sequence_(ops)
    } else CanvasIO.noop
  }

  def render(x: Int, y: Int): CanvasIO[Unit] = render(x, y, None)

  def render(x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int, mask: Option[Color]): CanvasIO[Unit] = {
    if (x >= 0 && y >= 0) {
      val ops = (for {
        iy <- lines
        if iy >= cy && iy < cy + ch
        ix <- columns
        if ix >= cx && ix < cx + cw
        pixel = Color.fromRGB(pixels(iy)(ix))
        if !mask.contains(pixel)
      } yield CanvasIO.putPixel(x + ix - cx, y + iy - cy, pixel))
      CanvasIO.sequence_(ops)
    } else CanvasIO.noop
  }

  def render(x: Int, y: Int, cx: Int, cy: Int, cw: Int, ch: Int): CanvasIO[Unit] =
    render(x, y, cx, cy, cw, ch, None)

  lazy val invert =
    Image(pixels.map(_.map { rc =>
      val c = Color.fromRGB(rc)
      Color(255 - c.r, 255 - c.g, 255 - c.b).argb
    }))

  lazy val flipH = Image(pixels.map(_.reverse))

  lazy val flipV = Image(pixels.reverse)
}

object Image {
  val empty: Image = Image(Vector.empty)

  def loadPpmImage(resource: Source): Try[Image] = Try {
    println("Loading resource")
    val it = resource.getLines().filterNot(_.startsWith("#")).flatMap(_.split(" "))
    val builder = Array.newBuilder[Int]
    require(it.next() == "P3", "Invalid image header")
    val width = it.next().toInt
    val height = it.next().toInt
    require(it.next() == "255", "Invalid color range")
    println("Reading pixels")
    (0 until (width * height)).foreach { _ =>
      val color = Color(it.next().toInt, it.next().toInt, it.next().toInt).argb
      builder += color
    }
    resource.close()
    println("Formatting")
    val pixels = builder.result().sliding(width, width).map(_.toArray).toVector
    println("Done")
    Image(pixels)
  }
}
