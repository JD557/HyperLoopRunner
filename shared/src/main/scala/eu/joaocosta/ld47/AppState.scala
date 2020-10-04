package eu.joaocosta.ld47

sealed trait AppState
object AppState {
  case object Menu extends AppState
  case class Intro(scale: Double, nextState: GameState) extends AppState
  case class Outro(scale: Double, lastState: GameState) extends AppState
  case class GameState(
    level: Level,
    player: GameState.Player,
    timeRift: GameState.TimeRift) extends AppState {
    def updatePlayer(f: GameState.Player => GameState.Player): GameState =
      copy(player = f(player))
    def updateTimeRift(f: GameState.TimeRift => GameState.TimeRift): GameState =
      copy(timeRift = f(timeRift))

    lazy val isEndGame: Option[GameState.EndGame] = {
      val dPlayerX = (player.x - timeRift.x)
      val dPlayerY = (player.y - timeRift.y)
      val distance = dPlayerX * dPlayerX + dPlayerY * dPlayerY
      if (distance < 128 * 128) {
        val currentWaypoint = level.riftWaypoints(timeRift.currentWaypoint % level.riftWaypoints.size)
        val dWaypointX = currentWaypoint._1 - timeRift.x
        val dWayPointY = currentWaypoint._2 - timeRift.y
        val scalarProduct = (dPlayerX * dWaypointX) + (dPlayerY * dWayPointY)
        if (scalarProduct > 0) Some(GameState.EndGame.PlayerLoses)
        else Some(GameState.EndGame.PlayerWins)
      } else None
    }
  }
  case class GameOver(level: Level) extends AppState

  object GameState {
    case class Player(x: Double, y: Double, vx: Double, vy: Double, rotation: Double)
    case class TimeRift(x: Double, y: Double, rotation: Double, currentWaypoint: Int)

    sealed trait EndGame
    object EndGame {
      case object PlayerWins extends EndGame
      case object PlayerLoses extends EndGame
    }
  }
}

