package eu.joaocosta.minart.extra

import java.io.InputStream
import eu.joaocosta.minart.pure._

// Dummy interface
object MidiPlayer {

  def loadSequence(path: String): Unit = ()

  def playOnce(sequence: Unit): RIO[Any, Unit] = RIO.noop

  def playLooped(sequence: Unit): RIO[Any, Unit] = RIO.noop

  val stop: RIO[Any, Unit] = RIO.noop
}
