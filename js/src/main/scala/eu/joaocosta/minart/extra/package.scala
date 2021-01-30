package eu.joaocosta.minart

import java.io.{ ByteArrayInputStream, InputStream }
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.scalajs.js

import org.scalajs.dom.raw.XMLHttpRequest

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val jsPlatform: DefaultBackend[Any, Platform] = DefaultBackend.fromConstant(Platform.JS)

  implicit val jsResourceLoader: DefaultBackend[Any, ResourceLoader] = (_) => (name: String) => new Resource {
    def path = "./" + name

    def asSource(): Source = {
      val xhr = new XMLHttpRequest()
      xhr.open("GET", "./" + name, false)
      xhr.send()
      Source.fromString(xhr.responseText)
    }

    def asInputStream(): InputStream = {
      val xhr = new XMLHttpRequest()
      xhr.open("GET", "./" + name, false)
      //xhr.overrideMimeType("text/plain; charset=x-user-defined")
      xhr.asInstanceOf[js.Dynamic].overrideMimeType("text/plain; charset=x-user-defined")
      xhr.send()
      new ByteArrayInputStream(xhr.responseText.toCharArray.map(_.toByte));
    }
  }

  implicit val jsSoundPlayer: DefaultBackend[Any, SoundPlayer] = (_) => JsSoundPlayer
}
