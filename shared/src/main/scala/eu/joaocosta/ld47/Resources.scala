package eu.joaocosta.ld47

import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._

object Resources {
  val soundPlayer = SoundPlayer.default()
  val bgSoundChannel = soundPlayer.newChannel()

  lazy val background = Image.loadQoiImage(Resource("assets/bg.qoi"))
  lazy val logo = Image.loadQoiImage(Resource("assets/logo.qoi"))
  lazy val gameOver = Image.loadQoiImage(Resource("assets/gameover.qoi"))
  lazy val character = Image.loadQoiImage(Resource("assets/char.qoi")).map(surface =>
    new SpriteSheet(surface, 16, 16))
  lazy val jets = Image.loadQoiImage(Resource("assets/jets.qoi")).map(surface =>
    new SpriteSheet(surface, 16, 4))
  lazy val timeRift = Image.loadQoiImage(Resource("assets/timerift.qoi"))

  lazy val boostFull = Image.loadQoiImage(Resource("assets/boost-full.qoi"))
  lazy val boostEmpty = Image.loadQoiImage(Resource("assets/boost-empty.qoi"))

  lazy val fuelFull = Image.loadQoiImage(Resource("assets/fuel-full.qoi"))
  lazy val fuelEmpty = Image.loadQoiImage(Resource("assets/fuel-empty.qoi"))

  lazy val ingameSound = soundPlayer.loadClip(Resource(Platform() match {
    case Platform.JS => "assets/ingame-music.mp3"
    case _ => "assets/ingame-music.mid"
  }))
  lazy val gameoverSound = soundPlayer.loadClip(Resource(Platform() match {
    case Platform.JS => "assets/gameover.mp3"
    case _ => "assets/gameover.mid"
  }))
  lazy val menuSound = soundPlayer.loadClip(Resource(Platform() match {
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
