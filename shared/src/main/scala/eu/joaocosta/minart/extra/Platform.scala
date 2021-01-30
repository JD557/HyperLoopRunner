package eu.joaocosta.minart.extra

import eu.joaocosta.minart.backend.defaults.DefaultBackend

sealed trait Platform

object Platform {
  def get(implicit d: DefaultBackend[Any, Platform]): Platform =
    d.defaultValue(())

  case object JVM extends Platform
  case object JS extends Platform
  case object Native extends Platform
}
