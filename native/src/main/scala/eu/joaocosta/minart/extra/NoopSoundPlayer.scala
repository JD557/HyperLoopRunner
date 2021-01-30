package eu.joaocosta.minart.extra

import eu.joaocosta.minart.pure._

// Dummy interface
object NoopSoundPlayer extends SoundPlayer {

  type AudioResource = Unit

  def loadClip(resource: Resource): Unit = ()

  def playOnce(clip: Unit): RIO[Any, Unit] = RIO.noop

  def playLooped(clip: Unit): RIO[Any, Unit] = RIO.noop

  val stop: RIO[Any, Unit] = RIO.noop
}
