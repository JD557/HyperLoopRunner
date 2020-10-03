package eu.joaocosta.minart

import scala.io.Source

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val nativeResourceLoader: DefaultBackend[Any, ResourceLoader] = DefaultBackend.fromConstant(new ResourceLoader {
    def loadResource(name: String): Source = Source.fromFile("assets/" + name)
  })
}
