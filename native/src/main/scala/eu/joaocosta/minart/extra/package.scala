package eu.joaocosta.minart

import java.io.{ FileInputStream, InputStream }
import scala.io.Source

import eu.joaocosta.minart.backend.defaults.DefaultBackend

package object extra {
  implicit val nativePlatform: DefaultBackend[Any, Platform] = DefaultBackend.fromConstant(Platform.Native)

  implicit val nativeResourceLoader: DefaultBackend[Any, ResourceLoader] = DefaultBackend.fromConstant(new ResourceLoader {
    def loadResource(name: String): Resource = new Resource {
      def path = "./" + name
      def asSource(): Source = Source.fromFile("./" + name)
      def asInputStream(): InputStream = new FileInputStream("./" + name)
    }
  })

  implicit val nativeSoundPlayer: DefaultBackend[Any, SoundPlayer] = (_) => NoopSoundPlayer
}
