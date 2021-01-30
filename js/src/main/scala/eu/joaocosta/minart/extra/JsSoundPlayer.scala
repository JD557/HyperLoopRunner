package eu.joaocosta.minart.extra

import java.io.InputStream

import org.scalajs.dom
import org.scalajs.dom.html.Audio

import eu.joaocosta.minart.pure._

object JsSoundPlayer extends SoundPlayer {

  type AudioResource = Audio

  private[this] var currentClip: Option[Audio] = None

  def loadClip(resource: Resource): AudioResource = {
    val elem = dom.document.createElement("audio").asInstanceOf[Audio]
    elem.src = resource.path
    elem
  }

  def playOnce(clip: Audio): RIO[Any, Unit] = RIO.suspend {
    currentClip.foreach(_.pause())
    currentClip = Some(clip)
    currentClip.foreach { clip =>
      clip.currentTime = 0
      clip.loop = false
      clip.play()
    }
  }

  def playLooped(clip: Audio): RIO[Any, Unit] = RIO.suspend {
    currentClip.foreach(_.pause())
    currentClip = Some(clip)
    currentClip.foreach { clip =>
      clip.currentTime = 0
      clip.loop = true
      clip.play()
    }
  }

  val stop: RIO[Any, Unit] = RIO.suspend(currentClip.foreach(_.pause()))
}
