package eu.joaocosta.ld47

import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics._

object Resources {
  val soundPlayer = SoundPlayer.default()
  val bgSoundChannel = soundPlayer.newChannel()

  lazy val background = Image.loadQoiImage(Resource("assets/bg.qoi")).get
  lazy val logo = Image.loadQoiImage(Resource("assets/logo.qoi")).get
  lazy val gameOver = Image.loadQoiImage(Resource("assets/gameover.qoi")).get
  lazy val character = Image.loadQoiImage(Resource("assets/char.qoi")).map(surface =>
    new SpriteSheet(surface, 16, 16)).get
  lazy val jets = Image.loadQoiImage(Resource("assets/jets.qoi")).map(surface =>
    new SpriteSheet(surface, 16, 4)).get
  lazy val timeRift = Image.loadQoiImage(Resource("assets/timerift.qoi")).map(surface =>
    Plane.fromSurfaceWithFallback(surface, Color(255, 0, 255))).get

  lazy val boostFull = Image.loadQoiImage(Resource("assets/boost-full.qoi")).get
  lazy val boostEmpty = Image.loadQoiImage(Resource("assets/boost-empty.qoi")).get

  lazy val fuelFull = Image.loadQoiImage(Resource("assets/fuel-full.qoi")).get
  lazy val fuelEmpty = Image.loadQoiImage(Resource("assets/fuel-empty.qoi")).get

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
