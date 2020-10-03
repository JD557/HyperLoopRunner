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
  val initialState: AppState = AppState(0, 0, 0)
  val frameRate = FrameRate.fps60
  val terminateWhen = (_: State) => false

  val resourceLoader = ResourceLoader.default()

  val background = Image.loadPpmImage(resourceLoader.loadResource("bg.ppm"))
  val map = Image.loadPpmImage(resourceLoader.loadResource("map.ppm"))

  val tau = 2 * math.Pi

  val renderBackground: CanvasIO[Unit] = background.map(_.render(0, 0)).getOrElse(CanvasIO.noop)

  def renderMap(posX: Double, posY: Double, rot: Double) = {
    val transform = Transformation.Translate(-posX, -posY)
      .andThen(Transformation.Rotate(rot))
      .andThen(Transformation.Translate(128, 112))
    val pixels = for {
      x <- 0 until 256
      y <- 0 until 224
      (ix, iy) = transform(x, y)
      color <- map.toOption.flatMap(_.pixels.lift(iy.toInt).flatMap(_.lift(ix.toInt)))
    } yield CanvasIO.putPixel(x, y, color)
    CanvasIO.sequence_(pixels)
  }

  val initialAppState = AppState(
    x = 0.0,
    y = 0.0,
    rotation = 0.0)

  def renderAppState(state: AppState): CanvasIO[Unit] = {
    renderBackground.andThen(renderMap(state.x, state.y, state.rotation))
  }

  def updateAppState(state: AppState, keyboardInput: KeyboardInput): AppState = {
    val newRot =
      if (keyboardInput.isDown(Key.Left)) state.rotation - 0.05
      else if (keyboardInput.isDown(Key.Right)) state.rotation + 0.05
      else state.rotation
    val speed =
      if (keyboardInput.isDown(Key.Up)) 5.0
      else if (keyboardInput.isDown(Key.Down)) -5.0
      else 0
    val speedX = speed * math.sin(state.rotation)
    val speedY = -speed * math.cos(state.rotation)
    val normalizedRot =
      if (newRot > tau) newRot - tau
      else if (newRot < 0) newRot + tau
      else newRot
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
    case gs @ AppState(x, y, rot) =>
      for {
        keyboard <- CanvasIO.getKeyboardInput
        _ = frameCounter()
        _ <- CanvasIO.clear()
        _ <- renderAppState(gs)
        newState = updateAppState(gs, keyboard)
        _ <- CanvasIO.redraw
      } yield newState
  }
}
