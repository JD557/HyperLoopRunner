package eu.joaocosta.ld47

import eu.joaocosta.minart.audio.sound._
import eu.joaocosta.minart.audio._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.graphics._

object Resources {
  val audioPlayer = AudioPlayer.create(AudioPlayer.Settings())

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

  lazy val ingameSound = Sound.loadWavClip(Resource("assets/ingame-music.wav")).get
  lazy val gameoverSound = Sound.loadWavClip(Resource("assets/gameover.wav")).get
  lazy val menuSound = Sound.loadWavClip(Resource("assets/menu.wav")).get

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
