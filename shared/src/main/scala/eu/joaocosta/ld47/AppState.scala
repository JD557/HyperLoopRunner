package eu.joaocosta.ld47

sealed trait AppState
object AppState {
  case object Menu extends AppState
  case class Intro(scale: Double) extends AppState
  case class GameState(
    level: Level,
    player: GameState.Player,
    timeRift: GameState.TimeRift) extends AppState {
    def updatePlayer(f: GameState.Player => GameState.Player): GameState =
      copy(player = f(player))
    def updateTimeRift(f: GameState.TimeRift => GameState.TimeRift): GameState =
      copy(timeRift = f(timeRift))
  }

  object GameState {
    case class Player(x: Double, y: Double, rotation: Double)
    case class TimeRift(x: Double, y: Double, currentWaypoint: Int)
  }
}

