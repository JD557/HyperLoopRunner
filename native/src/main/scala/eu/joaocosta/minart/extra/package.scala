package eu.joaocosta.minart

import java.io.{ FileInputStream, InputStream }
import scala.io.Source

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val nativeSoundPlayer: DefaultBackend[Any, SoundPlayer] = (_) => NoopSoundPlayer
}
