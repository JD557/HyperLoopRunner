package eu.joaocosta.minart.extra

import eu.joaocosta.minart.backend.defaults.DefaultBackend
import eu.joaocosta.minart.runtime.pure.RIO

trait SoundPlayer {
  type AudioResource

  def loadClip(resource: Resource): AudioResource

  def playOnce(clip: AudioResource): RIO[Any, Unit]

  def playLooped(clip: AudioResource): RIO[Any, Unit]

  val stop: RIO[Any, Unit]
}

object SoundPlayer {
  def default()(implicit d: DefaultBackend[Any, SoundPlayer]): SoundPlayer = d.defaultValue(())
}
