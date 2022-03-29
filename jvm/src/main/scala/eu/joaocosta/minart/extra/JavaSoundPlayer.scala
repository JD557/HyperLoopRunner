package eu.joaocosta.minart.extra

import java.io.{ BufferedInputStream, InputStream }
import javax.sound.sampled.{ AudioSystem, Clip }

import eu.joaocosta.minart.runtime.pure.RIO
import eu.joaocosta.minart.runtime.Resource

object JavaSoundPlayer extends SoundPlayer {

  type AudioResource = Clip

  def loadClip(resource: Resource): AudioResource = {
    resource.withInputStream { is =>
      // TODO check if the input stream needs to be copied
      val bis = new BufferedInputStream(is)
      val clip = AudioSystem.getClip()
      clip.open(AudioSystem.getAudioInputStream(bis))
      clip
    }.get
  }

  def newChannel(): SoundPlayer.SoundChannel[AudioResource] = new SoundPlayer.SoundChannel[AudioResource] {

    private var currentClip: Option[Clip] = None

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
}
