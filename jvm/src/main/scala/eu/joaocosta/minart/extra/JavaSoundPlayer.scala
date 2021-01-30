package eu.joaocosta.minart.extra

import java.io.{ BufferedInputStream, InputStream }
import javax.sound.sampled.{ AudioSystem, Clip }

import eu.joaocosta.minart.pure._

object JavaSoundPlayer extends SoundPlayer {

  type AudioResource = Clip

  private var currentClip: Option[Clip] = None

  def loadClip(resource: Resource): AudioResource = {
    val is = new BufferedInputStream(resource.asInputStream)
    val clip = AudioSystem.getClip()
    clip.open(AudioSystem.getAudioInputStream(is))
    clip
  }

  def playOnce(clip: AudioResource): RIO[Any, Unit] = RIO.suspend {
    currentClip.foreach(_.stop())
    currentClip = Some(clip)
    currentClip.foreach { clip =>
      clip.setMicrosecondPosition(0)
      clip.loop(0)
      clip.start()
    }
  }

  def playLooped(clip: AudioResource): RIO[Any, Unit] = RIO.suspend {
    currentClip.foreach(_.stop())
    currentClip = Some(clip)
    currentClip.foreach { clip =>
      clip.setMicrosecondPosition(0)
      clip.loop(Clip.LOOP_CONTINUOUSLY)
      clip.start()
    }
  }

  val stop: RIO[Any, Unit] = RIO.suspend(currentClip.foreach(_.stop()))
}
