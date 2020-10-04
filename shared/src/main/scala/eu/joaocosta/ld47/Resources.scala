package eu.joaocosta.ld47

import eu.joaocosta.minart.core._
import eu.joaocosta.minart.extra._

object Resources {
  val resourceLoader = ResourceLoader.default()

  val background = Image.loadPpmImage(resourceLoader.loadResource("bg.ppm"))
  val logo = Image.loadPpmImage(resourceLoader.loadResource("logo.ppm"))
  val gameOver = Image.loadPpmImage(resourceLoader.loadResource("gameover.ppm"))
  val character = Image.loadPpmImage(resourceLoader.loadResource("char.ppm"))
  val jets = Image.loadPpmImage(resourceLoader.loadResource("jets.ppm"))
  val timeRift = Image.loadPpmImage(resourceLoader.loadResource("timerift.ppm"))

  val ingameSound = MidiPlayer.loadSequence(resourceLoader.getClass.getResourceAsStream("/ingame-music.mid"))
  val gameoverSound = MidiPlayer.loadSequence(resourceLoader.getClass.getResourceAsStream("/gameover.mid"))
}
