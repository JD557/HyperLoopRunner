package eu.joaocosta.ld47

sealed trait AppState
object AppState {
  case object Menu extends AppState
  case class Intro(scale: Double) extends AppState
  case class GameState(
    level: Level,
    playerX: Double,
    playerY: Double,
    rotation: Double,
    timeRiftX: Double,
    timeRiftY: Double) extends AppState
}

