package eu.joaocosta.minart

import java.io.{ FileInputStream, InputStream }
import scala.io.{ Codec, Source }
import scala.util.Try

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val javaSoundPlayer: DefaultBackend[Any, SoundPlayer] = (_) => JavaSoundPlayer
}
