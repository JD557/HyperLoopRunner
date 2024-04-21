package eu.joaocosta.ld47

import eu.joaocosta.minart.audio._
import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.backend.subsystem._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._

import scala.io.Source
import scala.concurrent.duration._

object Main {

  val canvasSettings = Canvas.Settings(width = 256, height = 224, scale = Some(2), clearColor = Color(0, 0, 0))

  val initialState: AppState = AppState.Loading(0, (() => initialGameState) :: Resources.allResources)
  val frameRate              = LoopFrequency.hz60
  val terminateWhen          = (_: AppState) => false

  val tau = 2 * math.Pi

  def updatePlayer(
      level: Level,
      player: AppState.GameState.Player,
      keyboardInput: KeyboardInput
  ): AppState.GameState.Player = {
    val boosting = player.boost > 0.0 && keyboardInput.isDown(Key.Space)
    val topSpeed = if (boosting) 20.0 else 10.0
    val newBoost = if (boosting) math.max(player.boost - 0.005, 0.0) else player.boost
    val maxSpeed =
      level.collisionMap.getPixel(player.x.toInt, player.y.toInt).map(_.r / 255.0 * topSpeed).getOrElse(topSpeed)
    val newRot =
      if (keyboardInput.isDown(Key.Left)) player.rotation - 0.05
      else if (keyboardInput.isDown(Key.Right)) player.rotation + 0.05
      else player.rotation
    val accel =
      if (keyboardInput.isDown(Key.Up)) 1.0
      else if (keyboardInput.isDown(Key.Down)) -1.0
      else 0
    val deltaAccelX  = accel * math.sin(player.rotation)
    val deltaAccelY  = -accel * math.cos(player.rotation)
    val newRawSpeedX = player.vx * 0.9 + deltaAccelX
    val newRawSpeedY = player.vy * 0.9 + deltaAccelY
    val totalSpeed   = math.sqrt((newRawSpeedX * newRawSpeedX) + (newRawSpeedY * newRawSpeedY))
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
    val nextX          = player.x + newSpeedX
    val nextY          = player.y + newSpeedY
    lazy val collision = level.collisionMap.getPixel(nextX.toInt, nextY.toInt).exists(_.r == 0)
    lazy val stopped   = totalSpeed < 0.1
    if (collision) {
      player.copy(
        x = player.x - newSpeedX,
        y = player.y - newSpeedY,
        vx = -newSpeedX,
        vy = -newSpeedY,
        rotation = normalizedRot,
        boost = newBoost,
        fuel = player.fuel - 0.01
      )
    } else if (stopped)
      player.copy(vx = 0, vy = 0, rotation = normalizedRot, boost = newBoost)
    else
      player.copy(
        x = nextX,
        y = nextY,
        vx = newSpeedX,
        vy = newSpeedY,
        rotation = normalizedRot,
        boost = newBoost,
        fuel = player.fuel - 0.0001
      )
  }

  def updateTimeRift(level: Level, timeRift: AppState.GameState.TimeRift): AppState.GameState.TimeRift = {
    val currentWaypoint = level.riftWaypoints(timeRift.currentWaypoint % level.riftWaypoints.size)
    val dx              = currentWaypoint._1 - timeRift.x
    val dy              = currentWaypoint._2 - timeRift.y
    val waypointDist    = math.sqrt(dx * dx + dy * dy)
    val nx              = dx / waypointDist
    val ny              = dy / waypointDist
    val nextWaypoint =
      if (waypointDist <= 10) timeRift.currentWaypoint + 1
      else timeRift.currentWaypoint
    val vx = if (waypointDist == 0) 0.0 else nx * level.riftSpeed
    val vy = if (waypointDist == 0) 0.0 else ny * level.riftSpeed
    val rot: Double =
      if (vx >= 0) -math.acos(-ny)
      else math.acos(-ny)
    timeRift.copy(x = timeRift.x + vx, y = timeRift.y + vy, rotation = rot, currentWaypoint = nextWaypoint)
  }

  def updateGameState(gameState: AppState.GameState, keyboardInput: KeyboardInput): AppState = {
    if (gameState.isEndGame.isDefined) AppState.Outro(1.0, gameState)
    else
      gameState
        .updatePlayer(player => updatePlayer(gameState.level, player, keyboardInput))
        .updateTimeRift(timeRift => updateTimeRift(gameState.level, timeRift))
  }

  val frameCounter = {
    var frameNumber: Int = 0
    var timer            = System.currentTimeMillis
    () => {
      frameNumber += 1
      if (frameNumber % 10 == 0) {
        val currTime = System.currentTimeMillis()
        val fps      = 10.0 / ((currTime - timer) / 1000.0)
        println("FPS:" + fps)
        timer = System.currentTimeMillis()
      }
    }
  }

  def transitionTo(audioPlayer: AudioPlayer, state: AppState): AppState = state match {
    case AppState.Menu =>
      audioPlayer.stop()
      audioPlayer.play(Resources.menuSound.repeating)
      state
    case _: AppState.Intro =>
      audioPlayer.stop()
      state
    case _: AppState.GameState =>
      audioPlayer.stop()
      audioPlayer.play(Resources.ingameSound.repeating)
      state
    case _: AppState.GameOver =>
      audioPlayer.stop()
      audioPlayer.play(Resources.gameoverSound)
      state
    case _ => state
  }

  lazy val initialGameState = Level.levels.head.initialState

  val appLoop = AppLoop
    .statefulAppLoop { (state: AppState) => (system) =>
      import system._
      state match {
        case AppState.Loading(_, Nil) =>
          transitionTo(audioPlayer, AppState.Menu)
        case AppState.Loading(loaded, loadNext :: remaining) =>
          canvas.clear()
          canvas.fillRegion(10, 224 - 20, 256 - 20, 10, Color(255, 255, 255))
          canvas.fillRegion(10 + 2, 224 - 20 + 2, 256 - 20 - 4, 10 - 4, Color(0, 0, 0))
          val percentage = loaded.toDouble / (loaded + remaining.size)
          canvas.fillRegion(10 + 3, 224 - 20 + 3, (percentage * (256 - 20 - 6)).toInt, 10 - 6, Color(255, 255, 255))
          canvas.redraw()
          loadNext()
          AppState.Loading(loaded + 1, remaining)
        case AppState.Menu =>
          val keyboard = canvas.getKeyboardInput()
          canvas.clear()
          RenderOps.renderBackground(canvas)
          RenderOps.renderLogo(canvas)
          canvas.redraw()
          if (keyboard.keysPressed(Key.Enter)) transitionTo(audioPlayer, AppState.Intro(0.005, initialGameState, true))
          else state
        case AppState.Intro(scale, nextState, noSound) =>
          canvas.clear()
          val surface = nextState.level.track
            .translate(-nextState.player.x, -nextState.player.y)
            .scale(scale, scale)
            .rotate(scale * tau)
            .translate(128, 112)
            .toSurfaceView(256, 224)
          RenderOps.renderBackground(canvas)
          canvas.blit(surface, BlendMode.ColorMask(Color(0, 0, 0)))(0, 0)
          canvas.redraw()
          if (scale < 1.0) AppState.Intro(scale + 0.005, nextState, noSound)
          else if (noSound) transitionTo(audioPlayer, nextState)
          else nextState
        case AppState.Outro(scale, lastState) =>
          canvas.clear()
          val surface = lastState.level.track
            .translate(-lastState.player.x, -lastState.player.y)
            .scale(scale, scale)
            .rotate(scale * tau)
            .translate(128, 112)
            .toSurfaceView(256, 224)
          RenderOps.renderBackground(canvas)
          canvas.blit(surface, BlendMode.ColorMask(Color(0, 0, 0)))(0, 0)
          canvas.redraw()
          if (scale <= 0.0) {
            if (lastState.isEndGame == Some(AppState.GameState.EndGame.PlayerWins))
              if (lastState.level == Level.levels.last) transitionTo(audioPlayer, AppState.Menu)
              else
                AppState.Intro(
                  0.005,
                  Level.levels.dropWhile(_ != lastState.level).tail.head.initialState,
                  noSound = false
                )
            else transitionTo(audioPlayer, AppState.GameOver(lastState.level))
          } else AppState.Outro(scale - 0.005, lastState)
        case gs: AppState.GameState =>
          val keyboard = canvas.getKeyboardInput()
          frameCounter()
          canvas.clear()
          RenderOps.renderGameState(canvas, gs, keyboard)
          canvas.redraw()
          updateGameState(gs, keyboard)
        case AppState.GameOver(level) =>
          val keyboard = canvas.getKeyboardInput()
          canvas.clear()
          RenderOps.renderBackground(canvas)
          RenderOps.renderGameOver(canvas)
          canvas.redraw()
          if (keyboard.isDown(Key.Enter))
            transitionTo(audioPlayer, AppState.Intro(0.005, level.initialState, noSound = true))
          else if (keyboard.isDown(Key.Backspace)) transitionTo(audioPlayer, AppState.Menu)
          else state
      }
    }
    .configure((canvasSettings, AudioPlayer.Settings()), frameRate, initialState)

  def main(args: Array[String]): Unit = appLoop.run()
}
