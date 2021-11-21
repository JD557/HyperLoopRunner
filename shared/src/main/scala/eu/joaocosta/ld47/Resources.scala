package eu.joaocosta.ld47

import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.extra._

object Resources {
  val resourceLoader = ResourceLoader.default()
  val midiPlayer = SoundPlayer.default()

  lazy val background = Image.loadPpmImage(resourceLoader.loadResource("assets/bg.ppm"))
  lazy val logo = Image.loadPpmImage(resourceLoader.loadResource("assets/logo.ppm"))
  lazy val gameOver = Image.loadPpmImage(resourceLoader.loadResource("assets/gameover.ppm"))
  lazy val character = Image.loadPpmImage(resourceLoader.loadResource("assets/char.ppm"))
  lazy val jets = Image.loadPpmImage(resourceLoader.loadResource("assets/jets.ppm"))
  lazy val timeRift = Image.loadPpmImage(resourceLoader.loadResource("assets/timerift.ppm"))

  lazy val boostFull = Image.loadPpmImage(resourceLoader.loadResource("assets/boost-full.ppm"))
  lazy val boostEmpty = Image.loadPpmImage(resourceLoader.loadResource("assets/boost-empty.ppm"))

  lazy val fuelFull = Image.loadPpmImage(resourceLoader.loadResource("assets/fuel-full.ppm"))
  lazy val fuelEmpty = Image.loadPpmImage(resourceLoader.loadResource("assets/fuel-empty.ppm"))

  lazy val ingameSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/ingame-music.mp3"
    case _ => "assets/ingame-music.mid"
  }))
  lazy val gameoverSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/gameover.mp3"
    case _ => "assets/gameover.mid"
  }))
  lazy val menuSound = midiPlayer.loadClip(resourceLoader.loadResource(Platform() match {
    case Platform.JS => "assets/menu.mp3"
    case _ => "assets/menu.mid"
  }))

  val allResources: List[() => Any] = List(
    () => background,
    () => logo,
    () => gameOver,
    () => character,
    () => jets,
    () => timeRift,
    () => boostFull,
    () => boostEmpty,
    () => fuelFull,
    () => fuelEmpty,
    () => ingameSound,
    () => gameoverSound,
    () => menuSound)
}
