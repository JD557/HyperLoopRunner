package eu.joaocosta.ld47

import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._

object Resources {
  val midiPlayer = SoundPlayer.default()

  lazy val background = Image.loadPpmImage(Resource("assets/bg.ppm"))
  lazy val logo = Image.loadPpmImage(Resource("assets/logo.ppm"))
  lazy val gameOver = Image.loadPpmImage(Resource("assets/gameover.ppm"))
  lazy val character = Image.loadPpmImage(Resource("assets/char.ppm"))
  lazy val jets = Image.loadPpmImage(Resource("assets/jets.ppm"))
  lazy val timeRift = Image.loadPpmImage(Resource("assets/timerift.ppm"))

  lazy val boostFull = Image.loadPpmImage(Resource("assets/boost-full.ppm"))
  lazy val boostEmpty = Image.loadPpmImage(Resource("assets/boost-empty.ppm"))

  lazy val fuelFull = Image.loadPpmImage(Resource("assets/fuel-full.ppm"))
  lazy val fuelEmpty = Image.loadPpmImage(Resource("assets/fuel-empty.ppm"))

  lazy val ingameSound = midiPlayer.loadClip(Resource(Platform() match {
    case Platform.JS => "assets/ingame-music.mp3"
    case _ => "assets/ingame-music.mid"
  }))
  lazy val gameoverSound = midiPlayer.loadClip(Resource(Platform() match {
    case Platform.JS => "assets/gameover.mp3"
    case _ => "assets/gameover.mid"
  }))
  lazy val menuSound = midiPlayer.loadClip(Resource(Platform() match {
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
