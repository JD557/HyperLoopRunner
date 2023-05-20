package eu.joaocosta.ld47

import eu.joaocosta.minart.audio._
import eu.joaocosta.minart.audio.pure._
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.backend.subsystem._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.runtime.pure._

import scala.io.Source
import scala.concurrent.duration._

object Main extends MinartApp[AppState, AppLoop.LowLevelAllSubsystems] {

  val loopRunner = LoopRunner()
  val createSubsystem = () => LowLevelCanvas.create() ++ LowLevelAudioPlayer.create()

  val canvasSettings = Canvas.Settings(
    width = 256,
    height = 224,
    scale = Some(2),
    clearColor = Color(0, 0, 0))

  val initialState: AppState = AppState.Loading(0, (() => initialGameState) :: Resources.allResources)
  val frameRate = LoopFrequency.hz60
  val terminateWhen = (_: AppState) => false

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
        boost = newBoost,
        fuel = player.fuel - 0.01)
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
        boost = newBoost,
        fuel = player.fuel - 0.0001)
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

  def transitionTo(state: AppState): AudioPlayerIO[AppState] = state match {
    case AppState.Menu =>
      for {
        _ <- AudioPlayerIO.stop
        _ <- AudioPlayerIO.play(Resources.menuSound.repeating)
      } yield state
    case _: AppState.Intro =>
      AudioPlayerIO.stop.as(state)
    case _: AppState.GameState =>
      for {
        _ <- AudioPlayerIO.stop
        _ <- AudioPlayerIO.play(Resources.ingameSound.repeating)
      } yield state
    case _: AppState.GameOver =>
      for {
        _ <- AudioPlayerIO.stop
        _ <- AudioPlayerIO.play(Resources.gameoverSound)
      } yield state
    case _ => RIO.pure(state)
  }

  lazy val initialGameState = Level.levels.head.initialState

  val appLoop = AppLoop.statefulAppLoop { (state: AppState) =>
    state match {
      case AppState.Loading(_, Nil) =>
        transitionTo(AppState.Menu)
      case AppState.Loading(loaded, loadNext :: remaining) => for {
        _ <- CanvasIO.clear()
        _ <- CanvasIO.fillRegion(10, 224 - 20, 256 - 20, 10, Color(255, 255, 255))
        _ <- CanvasIO.fillRegion(10 + 2, 224 - 20 + 2, 256 - 20 - 4, 10 - 4, Color(0, 0, 0))
        percentage = loaded.toDouble / (loaded + remaining.size)
        _ <- CanvasIO.fillRegion(10 + 3, 224 - 20 + 3, (percentage * (256 - 20 - 6)).toInt, 10 - 6, Color(255, 255, 255))
        _ <- CanvasIO.redraw
        _ = loadNext()
      } yield AppState.Loading(loaded + 1, remaining)
      case AppState.Menu =>
        for {
          keyboard <- CanvasIO.getKeyboardInput
          _ <- CanvasIO.clear()
          _ <- RenderOps.renderBackground.andThen(RenderOps.renderLogo)
          newState <- if (keyboard.keysPressed(Key.Enter)) transitionTo(AppState.Intro(0.005, initialGameState, true))
          else CanvasIO.suspend(state)
          _ <- CanvasIO.redraw
        } yield newState
      case AppState.Intro(scale, nextState, noSound) =>
        for {
          _ <- CanvasIO.clear()
          surface = nextState.level.track
            .translate(-nextState.player.x, -nextState.player.y)
            .scale(scale, scale)
            .rotate(scale * tau)
            .translate(128, 112)
            .toSurfaceView(256, 224)
          _ <- RenderOps.renderBackground
          _ <- CanvasIO.blit(surface, Some(Color(0, 0, 0)))(0, 0)
          newState <- if (scale < 1.0) CanvasIO.suspend(AppState.Intro(scale + 0.005, nextState, noSound))
          else if (noSound) transitionTo(nextState)
          else CanvasIO.suspend(nextState)
          _ <- CanvasIO.redraw
        } yield newState
      case AppState.Outro(scale, lastState) =>
        for {
          _ <- CanvasIO.clear()
          surface = lastState.level.track
            .translate(-lastState.player.x, -lastState.player.y)
            .scale(scale, scale)
            .rotate(scale * tau)
            .translate(128, 112)
            .toSurfaceView(256, 224)
          _ <- RenderOps.renderBackground
          _ <- CanvasIO.blit(surface, Some(Color(0, 0, 0)))(0, 0)
          newState <- if (scale <= 0.0) {
            if (lastState.isEndGame == Some(AppState.GameState.EndGame.PlayerWins))
              if (lastState.level == Level.levels.last) transitionTo(AppState.Menu)
              else RIO.suspend(AppState.Intro(0.005, Level.levels.dropWhile(_ != lastState.level).tail.head.initialState, noSound = false))
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
          newState <- if (keyboard.isDown(Key.Enter)) transitionTo(AppState.Intro(0.005, level.initialState, noSound = true))
          else if (keyboard.isDown(Key.Backspace)) transitionTo(AppState.Menu)
          else CanvasIO.suspend(state)
          _ <- CanvasIO.redraw
        } yield newState
    }
  }.configure((canvasSettings, AudioPlayer.Settings()), frameRate, initialState)
}
