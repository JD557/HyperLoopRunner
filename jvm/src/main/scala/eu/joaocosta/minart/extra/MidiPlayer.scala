package eu.joaocosta.minart.extra

import java.io.InputStream
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence
import javax.sound.midi.Sequencer

import eu.joaocosta.minart.pure._

object MidiPlayer {
  private val sequencer: Sequencer = {
    val seq = MidiSystem.getSequencer()
    seq.open()
    seq
  }

  def loadSequence(path: String): Sequence = {
    val is = this.getClass.getResourceAsStream("/" + path)
    MidiSystem.getSequence(is);
  }

  def playOnce(sequence: Sequence): RIO[Any, Unit] = RIO.suspend {
    sequencer.stop()
    sequencer.setLoopCount(0)
    sequencer.setSequence(sequence)
    sequencer.start()
  }

  def playLooped(sequence: Sequence): RIO[Any, Unit] = RIO.suspend {
    sequencer.stop()
    sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY)
    sequencer.setSequence(sequence)
    sequencer.start()
  }

  val stop: RIO[Any, Unit] = RIO.suspend(sequencer.stop())
}
