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

  val resourceLoader = ResourceLoader.default()
  val background = Image.loadPpmImage(resourceLoader.loadResource("bg.ppm"))
  val logo = Image.loadPpmImage(resourceLoader.loadResource("logo.ppm"))
  val map = Image.loadPpmImage(resourceLoader.loadResource("map.ppm"))
  val character = Image.loadPpmImage(resourceLoader.loadResource("char.ppm"))
  val timeRift = Image.loadPpmImage(resourceLoader.loadResource("timerift.ppm"))

  val levels = List(
    Level(
      background = background.get,
      track = Image.loadPpmImage(resourceLoader.loadResource("map.ppm")).get,
      collisionMap = Image.loadPpmImage(resourceLoader.loadResource("map.ppm")).get,
      startPosition = (920.0, 580.0),
      riftWaypoints = List(
        (635, 750),
        (920, 750),
        (920, 84),
        (75, 84),
        (75, 930)),
      riftSpeed = 2.5))

  val initialGameState = levels.head.initialState

  val tau = 2 * math.Pi

  val renderLogo: CanvasIO[Unit] = logo.map(_.render(0, 0, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderBackground: CanvasIO[Unit] = background.map(_.render(0, 0)).getOrElse(CanvasIO.noop)
  val renderChar: CanvasIO[Unit] = character.map(_.render(128 - 8, 112 - 8, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)

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

  def renderGameState(state: AppState.GameState): CanvasIO[Unit] = {
    val mapTransform =
      Transformation.Translate(-state.player.x, -state.player.y)
        .andThen(Transformation.Rotate(state.player.rotation))
        .andThen(Transformation.Translate(128, 112))
    val timeRiftTransform =
      Transformation.Translate(state.timeRift.x - 128, state.timeRift.y - 128)
        .andThen(mapTransform)
    renderBackground
      .andThen(renderTransformed(state.level.track, mapTransform))
      .andThen(renderChar)
      .andThen(renderTransformed(timeRift.get, timeRiftTransform, Some(Color(255, 0, 255))))
  }

  def updatePlayer(player: AppState.GameState.Player, keyboardInput: KeyboardInput): AppState.GameState.Player = {
    val maxSpeed = map.toOption.flatMap(_.pixels.lift(player.y.toInt).flatMap(_.lift(player.x.toInt))).map(_.r / 255.0 * 5).getOrElse(5.0)
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
    val stopped = map.toOption.flatMap(_.pixels.lift(nextY.toInt).flatMap(_.lift(nextX.toInt))).contains(Color(0, 0, 0))
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
    val waypointDist = math.sqrt(math.pow(dx, 2) + math.pow(dy, 2))
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

  def updateGameState(gameState: AppState.GameState, keyboardInput: KeyboardInput): AppState.GameState = {
    gameState
      .updatePlayer(player => updatePlayer(player, keyboardInput))
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

  val renderFrame = (state: State) => state match {
    case AppState.Menu =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ <- CanvasIO.clear()
        _ <- renderBackground.andThen(renderLogo)
        newState = if (keyboard.isDown(Key.Enter)) AppState.Intro(0.005) else AppState.Menu
        _ <- CanvasIO.redraw
      } yield newState
    case AppState.Intro(scale) =>
      for {
        _ <- CanvasIO.clear()
        transform = Transformation.Translate(-initialGameState.player.x, -initialGameState.player.y)
          .andThen(Transformation.Scale(scale))
          .andThen(Transformation.Rotate(scale * tau))
          .andThen(Transformation.Translate(128, 112))
        _ <- renderBackground.andThen(renderTransformed(initialGameState.level.track, transform))
        newState = if (scale >= 1.0) initialGameState else AppState.Intro(scale + 0.005)
        _ <- CanvasIO.redraw
      } yield newState
    case gs: AppState.GameState =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ = frameCounter()
        _ <- CanvasIO.clear()
        _ <- renderGameState(gs)
        newState = updateGameState(gs, keyboard)
        _ <- CanvasIO.redraw
      } yield newState
  }
}
