package eu.joaocosta.ld47

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.core.KeyboardInput.Key
import eu.joaocosta.minart.core._
import eu.joaocosta.minart.pure._
import eu.joaocosta.minart.extra._

import scala.io.Source
import scala.concurrent.duration._
import scala.util.Random

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

  val resourceLoader = ResourceLoader.default()
  val background = Image.loadPpmImage(resourceLoader.loadResource("bg.ppm"))
  val logo = Image.loadPpmImage(resourceLoader.loadResource("logo.ppm"))
  val gameOver = Image.loadPpmImage(resourceLoader.loadResource("gameover.ppm"))
  val character = Image.loadPpmImage(resourceLoader.loadResource("char.ppm"))
  val jets = Image.loadPpmImage(resourceLoader.loadResource("jets.ppm"))
  val timeRift = Image.loadPpmImage(resourceLoader.loadResource("timerift.ppm"))

  val levels = List(
    Level(
      track = Image.loadPpmImage(resourceLoader.loadResource("level1-map.ppm")).get,
      collisionMap = Image.loadPpmImage(resourceLoader.loadResource("level1-col.ppm")).get,
      startPosition = (920, 580),
      riftWaypoints = List(
        (635, 750),
        (920, 750),
        (920, 84),
        (75, 84),
        (75, 930)),
      riftSpeed = 2.5),
    Level(
      track = Image.loadPpmImage(resourceLoader.loadResource("level2-map.ppm")).get,
      collisionMap = Image.loadPpmImage(resourceLoader.loadResource("level2-col.ppm")).get,
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
      riftSpeed = 3))

  val initialGameState = levels.head.initialState

  val tau = 2 * math.Pi

  val renderLogo: CanvasIO[Unit] = logo.map(_.render(0, 0, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderGameOver: CanvasIO[Unit] = gameOver.map(_.render(16, 96, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderBackground: CanvasIO[Unit] = background.map(_.render(0, 0)).getOrElse(CanvasIO.noop)
  val renderCharLeft: CanvasIO[Unit] = character.map(_.render(128 - 8, 112 - 8, 0, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderCharBase: CanvasIO[Unit] = character.map(_.render(128 - 8, 112 - 8, 16, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderCharRight: CanvasIO[Unit] = character.map(_.render(128 - 8, 112 - 8, 32, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderJetLow: CanvasIO[Unit] = jets.map(_.render(128 - 8, 112 + 8, 0, 0, 16, 4, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderJetHigh: CanvasIO[Unit] = jets.map(_.render(128 - 8, 112 + 8, 0, 4, 16, 4, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)

  def renderChar(keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val renderShip =
      if (keyboardInput.isDown(Key.Left)) renderCharLeft
      else if (keyboardInput.isDown(Key.Right)) renderCharRight
      else renderCharBase
    val renderJets: CanvasIO[Unit] =
      if (keyboardInput.isDown(Key.Up)) CanvasIO.suspend(Random.nextBoolean()).flatMap(if (_) renderJetHigh else renderJetLow)
      else CanvasIO.noop
    renderJets.andThen(renderShip)
  }

  def renderTransformed(image: Image, transform: Transformation, colorMask: Option[Color] = None) = CanvasIO.accessCanvas { canvas =>
    for {
      y <- 0 until 224
      x <- 0 until 256
      (ix, iy) = transform(x, y)
      color <- image.pixels.lift(iy.toInt).flatMap(_.lift(ix.toInt))
      if !colorMask.contains(color)
    } canvas.putPixel(x, y, color)
  }

  /*def renderTransformed(image: Image, transform: Transformation, colorMask: Option[Color] = None) = {
    val pixels = for {
      x <- 0 until 256
      y <- 0 until 224
      (ix, iy) = transform(x, y)
      color <- Some(image).flatMap(_.pixels.lift(iy.toInt).flatMap(_.lift(ix.toInt)))
      if !colorMask.contains(color)
    } yield CanvasIO.putPixel(x, y, color)
    CanvasIO.sequence_(pixels)
  }*/

  def renderGameState(state: AppState.GameState, keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val mapTransform =
      Transformation.Translate(-state.player.x, -state.player.y)
        .andThen(Transformation.Rotate(state.player.rotation))
        .andThen(Transformation.Translate(128, 112))
    val timeRiftTransform =
      Transformation.Translate(state.timeRift.x - 128, state.timeRift.y - 128)
        .andThen(mapTransform)
    renderBackground
      .andThen(renderTransformed(state.level.track, mapTransform, Some(Color(0, 0, 0))))
      .andThen(renderChar(keyboardInput))
      .andThen(renderTransformed(timeRift.get, timeRiftTransform, Some(Color(255, 0, 255))))
  }

  def updatePlayer(level: Level, player: AppState.GameState.Player, keyboardInput: KeyboardInput): AppState.GameState.Player = {
    val maxSpeed = level.collisionMap.pixels.lift(player.y.toInt).flatMap(_.lift(player.x.toInt)).map(_.r / 255.0 * 5).getOrElse(5.0)
    val newRot =
      if (keyboardInput.isDown(Key.Left)) player.rotation - 0.05
      else if (keyboardInput.isDown(Key.Right)) player.rotation + 0.05
      else player.rotation
    val speed =
      if (keyboardInput.isDown(Key.Up)) maxSpeed
      else if (keyboardInput.isDown(Key.Down)) -maxSpeed
      else 0
    val speedX = speed * math.sin(player.rotation)
    val speedY = -speed * math.cos(player.rotation)
    val normalizedRot =
      if (newRot > tau) newRot - tau
      else if (newRot < 0) newRot + tau
      else newRot
    val nextX = player.x + speedX
    val nextY = player.y + speedY
    val stopped = level.collisionMap.pixels.lift(nextY.toInt).flatMap(_.lift(nextX.toInt)).contains(Color(0, 0, 0))
    if (stopped)
      player.copy(rotation = normalizedRot)
    else
      player.copy(
        x = player.x + speedX,
        y = player.y + speedY,
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
        _ <- renderBackground.andThen(renderLogo)
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
        _ <- renderBackground.andThen(renderTransformed(nextState.level.track, transform))
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
        _ <- renderBackground.andThen(renderTransformed(lastState.level.track, transform))
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
        _ <- renderGameState(gs, keyboard)
        newState = updateGameState(gs, keyboard)
        _ <- CanvasIO.redraw
      } yield newState
    case AppState.GameOver =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- renderBackground.andThen(renderGameOver)
        newState = if (keyboard.isDown(Key.Enter)) AppState.Menu else AppState.GameOver
        _ <- CanvasIO.redraw
      } yield newState
  }
}
