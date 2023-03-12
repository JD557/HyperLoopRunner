package eu.joaocosta.minart.extra

import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.graphics.image._

case class BitmapFont(
  bitmap: Surface,
  mask: Color,
  charWidth: Int,
  charHeight: Int,
  startingChar: Char) {

  private val spriteSheet = new SpriteSheet(bitmap, charWidth, charHeight)

  private val maxOffest = 255 - startingChar
  def renderChar(char: Char, x: Int, y: Int): MSurfaceIO[Unit] = {
    val offset = char - startingChar
    MSurfaceIO.when(offset >= 0 && offset < maxOffest)(
      MSurfaceIO.blit(spriteSheet.getSprite(offset), Some(mask))(x, y))
  }

  def renderText(str: String, x: Int, y: Int): MSurfaceIO[Unit] =
    MSurfaceIO.when(str.nonEmpty)(renderChar(str.head, x, y).andThen(renderText(str.tail, x + charWidth, y)))

  lazy val invert: BitmapFont =
    copy(bitmap = bitmap.view.invertColor.toRamSurface(), mask = Color(255 - mask.r, 255 - mask.b, 255 - mask.g))
}
