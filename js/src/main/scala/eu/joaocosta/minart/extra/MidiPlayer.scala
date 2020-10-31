package eu.joaocosta.minart.extra

import java.io.InputStream
import org.scalajs.dom
import org.scalajs.dom.html.Audio

import eu.joaocosta.minart.pure._

object MidiPlayer {

  private[this] var currentSequence: Option[Audio] = None

  def loadSequence(path: String): Audio = {
    val elem = dom.document.createElement("audio").asInstanceOf[Audio]
    elem.src = "./assets/" + path
    elem
  }

  def playOnce(sequence: Audio): RIO[Any, Unit] = RIO.suspend {
    currentSequence.foreach(_.pause())
    sequence.loop = false
    sequence.play()
    currentSequence = Some(sequence)
  }

  def playLooped(sequence: Audio): RIO[Any, Unit] = RIO.suspend {
    currentSequence.foreach(_.pause())
    sequence.loop = true
    sequence.play()
    currentSequence = Some(sequence)
  }

  val stop: RIO[Any, Unit] = RIO.suspend(currentSequence.foreach(_.pause()))
}
