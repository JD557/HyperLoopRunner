package eu.joaocosta.minart.extra

import eu.joaocosta.minart.graphics.Color
import eu.joaocosta.minart.graphics.RamSurface
import eu.joaocosta.minart.graphics.pure._

case class BitmapFont(
  bitmap: RamSurface,
  mask: Color,
  charsPerLine: Int,
  charWidth: Int,
  charHeight: Int,
  startingChar: Char) {

  private val maxOffest = 255 - startingChar
  def renderChar(char: Char, x: Int, y: Int): CanvasIO[Unit] = {
    val offset = char - startingChar
    if (offset >= 0 && offset < maxOffest) {
      val offsetX = offset % charsPerLine
      val offsetY = offset / charsPerLine
      CanvasIO.blitWithMask(bitmap, mask)(
        x,
        y,
        offsetX * charWidth,
        offsetY * charHeight,
        charWidth,
        charHeight)
    } else CanvasIO.noop
  }

  def renderText(str: String, x: Int, y: Int): CanvasIO[Unit] =
    if (str.isEmpty) CanvasIO.noop
    else renderChar(str.head, x, y).andThen(renderText(str.tail, x + charWidth, y))

  lazy val invert: BitmapFont =
    copy(bitmap = Image.invert(bitmap), mask = Color(255 - mask.r, 255 - mask.b, 255 - mask.g))
}
