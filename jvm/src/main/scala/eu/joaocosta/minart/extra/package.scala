package eu.joaocosta.minart

import scala.io.Source

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val jvmResourceLoader: DefaultBackend[Any, ResourceLoader] = (_) => (name: String) => Source.fromResource(name)
}
