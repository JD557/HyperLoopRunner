package eu.joaocosta.ld47

import eu.joaocosta.minart.extra._

case class Level(
  background: Image,
  track: Image,
  collisionMap: Image,
  startPosition: (Double, Double),
  riftWaypoints: List[(Double, Double)]) {
  def initialState = AppState.GameState(
    level = this,
    playerX = startPosition._1,
    playerY = startPosition._2,
    rotation = 0.0,
    timeRiftX = riftWaypoints.head._1,
    timeRiftY = riftWaypoints.head._2)
}

