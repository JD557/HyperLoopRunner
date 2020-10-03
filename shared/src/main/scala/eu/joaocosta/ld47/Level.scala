package eu.joaocosta.ld47

import eu.joaocosta.minart.extra._

case class Level(
  track: Image,
  collisionMap: Image,
  startPosition: (Double, Double),
  riftWaypoints: List[(Double, Double)],
  riftSpeed: Double) {
  def initialState = AppState.GameState(
    level = this,
    player = AppState.GameState.Player(
      x = startPosition._1,
      y = startPosition._2,
      vx = 0,
      vy = 0,
      rotation = 0.0),
    timeRift = AppState.GameState.TimeRift(
      x = riftWaypoints.head._1,
      y = riftWaypoints.head._2,
      currentWaypoint = 0))
}

