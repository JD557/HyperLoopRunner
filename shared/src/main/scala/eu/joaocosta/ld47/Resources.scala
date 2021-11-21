package eu.joaocosta.ld47

import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.extra._

object Resources {
  val resourceLoader = ResourceLoader.default()
  val midiPlayer = SoundPlayer.default()

  val background = Image.loadPpmImage(resourceLoader.loadResource("assets/bg.ppm"))
  val logo = Image.loadPpmImage(resourceLoader.loadResource("assets/logo.ppm"))
  val gameOver = Image.loadPpmImage(resourceLoader.loadResource("assets/gameover.ppm"))
  val character = Image.loadPpmImage(resourceLoader.loadResource("assets/char.ppm"))
  val jets = Image.loadPpmImage(resourceLoader.loadResource("assets/jets.ppm"))
  val timeRift = Image.loadPpmImage(resourceLoader.loadResource("assets/timerift.ppm"))

  val boostFull = Image.loadPpmImage(resourceLoader.loadResource("assets/boost-full.ppm"))
  val boostEmpty = Image.loadPpmImage(resourceLoader.loadResource("assets/boost-empty.ppm"))

  val fuelFull = Image.loadPpmImage(resourceLoader.loadResource("assets/fuel-full.ppm"))
  val fuelEmpty = Image.loadPpmImage(resourceLoader.loadResource("assets/fuel-empty.ppm"))

  val ingameSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/ingame-music.mp3"
    case _ => "assets/ingame-music.mid"
  }))
  val gameoverSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/gameover.mp3"
    case _ => "assets/gameover.mid"
  }))
  val menuSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/menu.mp3"
    case _ => "assets/menu.mid"
  }))
}
