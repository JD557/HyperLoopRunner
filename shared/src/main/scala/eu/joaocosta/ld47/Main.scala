package eu.joaocosta.ld47

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.core.KeyboardInput.Key
import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._
import eu.joaocosta.minart.extra._

import scala.io.Source
import scala.concurrent.duration._

object Main extends MinartApp {

  type State = AppState
  val renderLoop = RenderLoop.default()
  val canvasSettings = Canvas.Settings(
    width = 256,
    height = 224,
    scale = 2)
  val canvasManager: CanvasManager = CanvasManager.default(canvasSettings)
  val initialState: AppState = AppState.Menu
  val frameRate = FrameRate.fps60
  val terminateWhen = (_: State) => false

  val levels = List(
    Level(
      track = Image.loadPpmImage(Resources.resourceLoader.loadResource("level1-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resources.resourceLoader.loadResource("level1-col.ppm")).get,
      startPosition = (920, 580),
      riftWaypoints = List(
        (635, 750),
        (920, 750),
        (920, 84),
        (75, 84),
        (75, 930)),
      riftSpeed = 2.5),
    Level(
      track = Image.loadPpmImage(Resources.resourceLoader.loadResource("level2-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resources.resourceLoader.loadResource("level2-col.ppm")).get,
      startPosition = (75, 660.0),
      riftWaypoints = List(
        (75, 920),
        (75, 490),
        (125, 325),
        (195, 225),
        (370, 115),
        (534, 97),
        (935, 410),
        (935, 730)),
      riftSpeed = 3),
    Level(
      track = Image.loadPpmImage(Resources.resourceLoader.loadResource("level3-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resources.resourceLoader.loadResource("level3-col.ppm")).get,
      startPosition = (75, 550.0),
      riftWaypoints = List(
        (75, 850),
        (115, 130),
        (935, 130),
        (895, 515),
        (640, 340),
        (515, 725),
        (950, 725),
        (950, 935),
        (75, 935)),
      riftSpeed = 3.5))

  val initialGameState = levels.head.initialState

  val tau = 2 * math.Pi

  def updatePlayer(level: Level, player: AppState.GameState.Player, keyboardInput: KeyboardInput): AppState.GameState.Player = {
    val topSpeed = 10.0
    val maxSpeed = level.collisionMap.pixels.lift(player.y.toInt).flatMap(_.lift(player.x.toInt)).map(_.r / 255.0 * topSpeed).getOrElse(topSpeed)
    val newRot =
      if (keyboardInput.isDown(Key.Left)) player.rotation - 0.05
      else if (keyboardInput.isDown(Key.Right)) player.rotation + 0.05
      else player.rotation
    val accel =
      if (keyboardInput.isDown(Key.Up)) 1.0
      else if (keyboardInput.isDown(Key.Down)) -1.0
      else 0
    val deltaAccelX = accel * math.sin(player.rotation)
    val deltaAccelY = -accel * math.cos(player.rotation)
    val newRawSpeedX = player.vx * 0.9 + deltaAccelX
    val newRawSpeedY = player.vy * 0.9 + deltaAccelY
    val totalSpeed = math.sqrt((newRawSpeedX * newRawSpeedX) + (newRawSpeedY * newRawSpeedY))
    val newSpeedX =
      if (totalSpeed > 0) (newRawSpeedX / totalSpeed) * math.min(totalSpeed, maxSpeed)
      else 0
    val newSpeedY =
      if (totalSpeed > 0) (newRawSpeedY / totalSpeed) * math.min(totalSpeed, maxSpeed)
      else 0
    val normalizedRot =
      if (newRot > tau) newRot - tau
      else if (newRot < 0) newRot + tau
      else newRot
    val nextX = player.x + newSpeedX
    val nextY = player.y + newSpeedY
    lazy val collision = level.collisionMap.pixels.lift(nextY.toInt).flatMap(_.lift(nextX.toInt)).exists(_.r == 0)
    lazy val stopped = totalSpeed < 0.1
    if (collision) {
      player.copy(
        x = player.x - newSpeedX,
        y = player.y - newSpeedY,
        vx = -newSpeedX,
        vy = -newSpeedY,
        rotation = normalizedRot)
    } else if (stopped)
      player.copy(
        vx = 0,
        vy = 0,
        rotation = normalizedRot)
    else
      player.copy(
        x = nextX,
        y = nextY,
        vx = newSpeedX,
        vy = newSpeedY,
        rotation = normalizedRot)
  }

  def updateTimeRift(level: Level, timeRift: AppState.GameState.TimeRift): AppState.GameState.TimeRift = {
    val currentWaypoint = level.riftWaypoints(timeRift.currentWaypoint % level.riftWaypoints.size)
    val dx = currentWaypoint._1 - timeRift.x
    val dy = currentWaypoint._2 - timeRift.y
    val waypointDist = math.sqrt(dx * dx + dy * dy)
    val nextWaypoint =
      if (waypointDist <= 10) timeRift.currentWaypoint + 1
      else timeRift.currentWaypoint
    val vx = if (waypointDist == 0) 0.0 else dx / waypointDist * level.riftSpeed
    val vy = if (waypointDist == 0) 0.0 else dy / waypointDist * level.riftSpeed
    timeRift.copy(
      x = timeRift.x + vx,
      y = timeRift.y + vy,
      currentWaypoint = nextWaypoint)
  }

  def updateGameState(gameState: AppState.GameState, keyboardInput: KeyboardInput): AppState = {
    if (checkEndgame(gameState.level, gameState.player, gameState.timeRift).isDefined) AppState.Outro(1.0, gameState)
    else gameState
      .updatePlayer(player => updatePlayer(gameState.level, player, keyboardInput))
      .updateTimeRift(timeRift => updateTimeRift(gameState.level, timeRift))
  }

  sealed trait EndGame
  case object PlayerWins extends EndGame
  case object PlayerLoses extends EndGame

  def checkEndgame(level: Level, player: AppState.GameState.Player, timeRift: AppState.GameState.TimeRift): Option[EndGame] = {
    val dPlayerX = (player.x - timeRift.x)
    val dPlayerY = (player.y - timeRift.y)
    val distance = dPlayerX * dPlayerX + dPlayerY * dPlayerY
    if (distance < 128 * 128) {
      val currentWaypoint = level.riftWaypoints(timeRift.currentWaypoint % level.riftWaypoints.size)
      val dWaypointX = currentWaypoint._1 - timeRift.x
      val dWayPointY = currentWaypoint._2 - timeRift.y
      val scalarProduct = (dPlayerX * dWaypointX) + (dPlayerY * dWayPointY)
      if (scalarProduct > 0) Some(PlayerLoses)
      else Some(PlayerWins)
    } else None
  }

  val frameCounter = {
    var frameNumber: Int = 0
    var timer = System.currentTimeMillis
    () => {
      frameNumber += 1
      if (frameNumber % 10 == 0) {
        val currTime = System.currentTimeMillis()
        val fps = 10.0 / ((currTime - timer) / 1000.0)
        println("FPS:" + fps)
        timer = System.currentTimeMillis()
      }
    }
  }

  val renderFrame = (state: State) => state match {
    case AppState.Menu =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderLogo)
        newState = if (keyboard.keysPressed(Key.Enter)) AppState.Intro(0.005, initialGameState) else AppState.Menu
        _ <- CanvasIO.redraw
      } yield newState
    case AppState.Intro(scale, nextState) =>
      for {
        _ <- CanvasIO.clear()
        transform = Transformation.Translate(-nextState.player.x, -nextState.player.y)
          .andThen(Transformation.Scale(scale))
          .andThen(Transformation.Rotate(scale * tau))
          .andThen(Transformation.Translate(128, 112))
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderTransformed(nextState.level.track, transform, Some(Color(0, 0, 0))))
        newState = if (scale >= 1.0) nextState else AppState.Intro(scale + 0.005, nextState)
        _ <- CanvasIO.redraw
      } yield newState
    case AppState.Outro(scale, lastState) =>
      for {
        _ <- CanvasIO.clear()
        transform = Transformation.Translate(-lastState.player.x, -lastState.player.y)
          .andThen(Transformation.Scale(scale))
          .andThen(Transformation.Rotate(scale * tau))
          .andThen(Transformation.Translate(128, 112))
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderTransformed(lastState.level.track, transform, Some(Color(0, 0, 0))))
        newState = if (scale <= 0.0) {
          if (checkEndgame(lastState.level, lastState.player, lastState.timeRift) == Some(PlayerWins))
            if (lastState.level == levels.last) AppState.Menu // TODO Win state
            else AppState.Intro(0.005, levels.dropWhile(_ != lastState.level).tail.head.initialState) // TODO clean this up
          else AppState.GameOver
        } else AppState.Outro(scale - 0.005, lastState)
        _ <- CanvasIO.redraw
      } yield newState
    case gs: AppState.GameState =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ = frameCounter()
        _ <- CanvasIO.clear()
        _ <- RenderOps.renderGameState(gs, keyboard)
        newState = updateGameState(gs, keyboard)
        _ <- CanvasIO.redraw
      } yield newState
    case AppState.GameOver =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderGameOver)
        newState = if (keyboard.isDown(Key.Enter)) AppState.Menu else AppState.GameOver
        _ <- CanvasIO.redraw
      } yield newState
  }
}
