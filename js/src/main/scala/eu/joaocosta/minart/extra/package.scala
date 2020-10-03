package eu.joaocosta.minart

import scala.io.Source

import eu.joaocosta.minart.backend.defaults.DefaultBackend
import org.scalajs.dom.raw.XMLHttpRequest

package object extra {
  implicit val jsResourceLoader: DefaultBackend[Any, ResourceLoader] = (_) => (name: String) => {
    val xhr = new XMLHttpRequest()

    xhr.open("GET", "./assets/" + name, false)
    xhr.send()
    Source.fromString(xhr.responseText)
  }
}
