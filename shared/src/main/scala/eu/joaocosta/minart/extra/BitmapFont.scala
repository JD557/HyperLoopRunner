package eu.joaocosta.minart.extra

import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._

case class BitmapFont(
  bitmap: Image,
  mask: Option[Color],
  charsPerLine: Int,
  charWidth: Int,
  charHeight: Int,
  startingChar: Char) {
  def renderChar(char: Char, x: Int, y: Int): CanvasIO[Unit] = {
    val offset = char - startingChar
    if (offset >= 0 && offset < (255 - startingChar)) {
      val offsetX = offset % charsPerLine
      val offsetY = offset / charsPerLine
      bitmap.render(
        x,
        y,
        offsetX * charWidth,
        offsetY * charHeight,
        charWidth,
        charHeight,
        mask)
    } else CanvasIO.noop
  }

  def renderText(str: String, x: Int, y: Int): CanvasIO[Unit] =
    if (str.isEmpty) CanvasIO.noop
    else renderChar(str.head, x, y).andThen(renderText(str.tail, x + charWidth, y))

  lazy val invert: BitmapFont =
    copy(bitmap = bitmap.invert, mask = mask.map(c => Color(255 - c.r, 255 - c.b, 255 - c.g)))
}

object BitmapFont {
  val empty: BitmapFont =
    BitmapFont(
      Image.empty,
      None,
      0,
      0,
      0,
      255)
}
