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
  val initialState: AppState = AppState.Loading
  val frameRate = FrameRate.fps60
  val terminateWhen = (_: State) => false

  val tau = 2 * math.Pi

  def updatePlayer(level: Level, player: AppState.GameState.Player, keyboardInput: KeyboardInput): AppState.GameState.Player = {
    val boosting = player.boost > 0.0 && keyboardInput.isDown(Key.Space)
    val topSpeed = if (boosting) 20.0 else 10.0
    val newBoost = if (boosting) math.max(player.boost - 0.005, 0.0) else player.boost
    val maxSpeed = level.collisionMap.getPixel(player.x.toInt, player.y.toInt).map(_.r / 255.0 * topSpeed).getOrElse(topSpeed)
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
    lazy val collision = level.collisionMap.getPixel(nextX.toInt, nextY.toInt).exists(_.r == 0)
    lazy val stopped = totalSpeed < 0.1
    if (collision) {
      player.copy(
        x = player.x - newSpeedX,
        y = player.y - newSpeedY,
        vx = -newSpeedX,
        vy = -newSpeedY,
        rotation = normalizedRot,
        boost = newBoost)
    } else if (stopped)
      player.copy(
        vx = 0,
        vy = 0,
        rotation = normalizedRot,
        boost = newBoost)
    else
      player.copy(
        x = nextX,
        y = nextY,
        vx = newSpeedX,
        vy = newSpeedY,
        rotation = normalizedRot,
        boost = newBoost)
  }

  def updateTimeRift(level: Level, timeRift: AppState.GameState.TimeRift): AppState.GameState.TimeRift = {
    val currentWaypoint = level.riftWaypoints(timeRift.currentWaypoint % level.riftWaypoints.size)
    val dx = currentWaypoint._1 - timeRift.x
    val dy = currentWaypoint._2 - timeRift.y
    val waypointDist = math.sqrt(dx * dx + dy * dy)
    val nx = dx / waypointDist
    val ny = dy / waypointDist
    val nextWaypoint =
      if (waypointDist <= 10) timeRift.currentWaypoint + 1
      else timeRift.currentWaypoint
    val vx = if (waypointDist == 0) 0.0 else nx * level.riftSpeed
    val vy = if (waypointDist == 0) 0.0 else ny * level.riftSpeed
    val rot: Double =
      if (vx >= 0) -math.acos(-ny)
      else math.acos(-ny)
    timeRift.copy(
      x = timeRift.x + vx,
      y = timeRift.y + vy,
      rotation = rot,
      currentWaypoint = nextWaypoint)
  }

  def updateGameState(gameState: AppState.GameState, keyboardInput: KeyboardInput): AppState = {
    if (gameState.isEndGame.isDefined) AppState.Outro(1.0, gameState)
    else gameState
      .updatePlayer(player => updatePlayer(gameState.level, player, keyboardInput))
      .updateTimeRift(timeRift => updateTimeRift(gameState.level, timeRift))
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

  def transitionTo(state: AppState): CanvasIO[AppState] = state match {
    case AppState.Menu =>
      MidiPlayer.playLooped(Resources.menuSound).as(state)
    case _: AppState.Intro =>
      MidiPlayer.stop.as(state)
    case _: AppState.GameState =>
      MidiPlayer.playLooped(Resources.ingameSound).as(state)
    case _: AppState.GameOver =>
      MidiPlayer.playOnce(Resources.gameoverSound).as(state)
    case _ => CanvasIO.pure(state)
  }

  val initialGameState = Level.levels.head.initialState

  val renderFrame = (state: State) => state match {
    case AppState.Loading =>
      CanvasIO.clear().andThen(CanvasIO.redraw).andThen(transitionTo(AppState.Menu))
    case AppState.Menu =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderLogo)
        newState <- if (keyboard.keysPressed(Key.Enter)) transitionTo(AppState.Intro(0.005, initialGameState))
        else CanvasIO.suspend(state)
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
        newState <- if (scale >= 1.0) transitionTo(nextState) else CanvasIO.suspend(AppState.Intro(scale + 0.005, nextState))
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
        newState <- if (scale <= 0.0) {
          if (lastState.isEndGame == Some(AppState.GameState.EndGame.PlayerWins))
            if (lastState.level == Level.levels.last) transitionTo(AppState.Menu)
            else RIO.suspend(AppState.Intro(0.005, Level.levels.dropWhile(_ != lastState.level).tail.head.initialState))
          else transitionTo(AppState.GameOver(lastState.level))
        } else RIO.suspend(AppState.Outro(scale - 0.005, lastState))
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
    case AppState.GameOver(level) =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- RenderOps.renderBackground.andThen(RenderOps.renderGameOver)
        newState <- if (keyboard.isDown(Key.Enter)) transitionTo(AppState.Intro(0.005, level.initialState))
        else if (keyboard.isDown(Key.Backspace)) transitionTo(AppState.Menu)
        else CanvasIO.suspend(state)
        _ <- CanvasIO.redraw
      } yield newState
  }
}
