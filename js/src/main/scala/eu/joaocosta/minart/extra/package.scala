package eu.joaocosta.minart

import java.io.{ ByteArrayInputStream, InputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.scalajs.js

import org.scalajs.dom.raw.XMLHttpRequest

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val jsSoundPlayer: DefaultBackend[Any, SoundPlayer] = (_) => JsSoundPlayer
}
