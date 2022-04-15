package eu.joaocosta.minart.extra

import eu.joaocosta.minart.runtime.pure.RIO
import eu.joaocosta.minart.runtime.Resource

// Dummy interface
object NoopSoundPlayer extends SoundPlayer {

  type AudioResource = Unit

  def loadClip(resource: Resource): Unit = ()

  def newChannel(): SoundPlayer.SoundChannel[AudioResource] = new SoundPlayer.SoundChannel[AudioResource] {
    def playOnce(clip: Unit): RIO[Any, Unit] = RIO.noop
    def playLooped(clip: Unit): RIO[Any, Unit] = RIO.noop
    val stop: RIO[Any, Unit] = RIO.noop
  }
}
