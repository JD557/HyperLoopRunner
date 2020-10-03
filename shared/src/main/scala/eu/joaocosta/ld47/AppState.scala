package eu.joaocosta.ld47

sealed trait AppState
object AppState {
  case object Menu extends AppState
  case class Intro(scale: Double) extends AppState
  case class GameState(x: Double, y: Double, rotation: Double) extends AppState
}

