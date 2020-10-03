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

  val initialGameState = AppState.GameState(
    x = 920.0,
    y = 580.0,
    rotation = 0.0)

  val resourceLoader = ResourceLoader.default()

  val background = Image.loadPpmImage(resourceLoader.loadResource("bg.ppm"))
  val logo = Image.loadPpmImage(resourceLoader.loadResource("logo.ppm"))
  val map = Image.loadPpmImage(resourceLoader.loadResource("map.ppm"))
  val character = Image.loadPpmImage(resourceLoader.loadResource("char.ppm"))

  val tau = 2 * math.Pi

  val renderLogo: CanvasIO[Unit] = logo.map(_.render(0, 0, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderBackground: CanvasIO[Unit] = background.map(_.render(0, 0)).getOrElse(CanvasIO.noop)
  val renderChar: CanvasIO[Unit] = character.map(_.render(128 - 8, 112 - 8, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)

  def renderMap(transform: Transformation) = {
    val pixels = for {
      x <- 0 until 256
      y <- 0 until 224
      (ix, iy) = transform(x, y)
      color <- map.toOption.flatMap(_.pixels.lift(iy.toInt).flatMap(_.lift(ix.toInt)))
    } yield CanvasIO.putPixel(x, y, color)
    CanvasIO.sequence_(pixels)
  }

  def renderGameState(state: AppState.GameState): CanvasIO[Unit] = {
    val mapTransform =
      Transformation.Translate(-state.x, -state.y)
        .andThen(Transformation.Rotate(state.rotation))
        .andThen(Transformation.Translate(128, 112))
    renderBackground.andThen(renderMap(mapTransform)).andThen(renderChar)
  }

  def updateGameState(state: AppState.GameState, keyboardInput: KeyboardInput): AppState = {
    val maxSpeed = map.toOption.flatMap(_.pixels.lift(state.y.toInt).flatMap(_.lift(state.x.toInt))).map(_.r / 255.0 * 5).getOrElse(5.0)
    val newRot =
      if (keyboardInput.isDown(Key.Left)) state.rotation - 0.05
      else if (keyboardInput.isDown(Key.Right)) state.rotation + 0.05
      else state.rotation
    val speed =
      if (keyboardInput.isDown(Key.Up)) maxSpeed
      else if (keyboardInput.isDown(Key.Down)) -maxSpeed
      else 0
    val speedX = speed * math.sin(state.rotation)
    val speedY = -speed * math.cos(state.rotation)
    val normalizedRot =
      if (newRot > tau) newRot - tau
      else if (newRot < 0) newRot + tau
      else newRot
    val nextX = state.x + speedX
    val nextY = state.y + speedY
    val stopped = map.toOption.flatMap(_.pixels.lift(nextY.toInt).flatMap(_.lift(nextX.toInt))).contains(Color(0, 0, 0))
    if (stopped)
      state.copy(rotation = normalizedRot)
    else
      state.copy(x = state.x + speedX, y = state.y + speedY, rotation = normalizedRot)
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
        transform = Transformation.Translate(-initialGameState.x, -initialGameState.y)
          .andThen(Transformation.Scale(scale))
          .andThen(Transformation.Rotate(scale * tau))
          .andThen(Transformation.Translate(128, 112))
        _ <- renderBackground.andThen(renderMap(transform))
        newState = if (scale >= 1.0) initialGameState else AppState.Intro(scale + 0.005)
        _ <- CanvasIO.redraw
      } yield newState
    case gs @ AppState.GameState(x, y, rot) =>
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
