package eu.joaocosta.ld47

import scala.util.Random

import eu.joaocosta.minart.core.KeyboardInput.Key
import eu.joaocosta.minart.core._
import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.pure._

object RenderOps {
  val renderLogo: CanvasIO[Unit] = Resources.logo.map(_.render(0, 0, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderGameOver: CanvasIO[Unit] = Resources.gameOver.map(_.render(16, 96, Some(Color(0, 0, 0)))).getOrElse(CanvasIO.noop)
  val renderBackground: CanvasIO[Unit] = Resources.background.map(_.render(0, 0)).getOrElse(CanvasIO.noop)
  val renderShipLeft: CanvasIO[Unit] = Resources.character.map(_.render(128 - 8, 112 - 8, 0, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderShipBase: CanvasIO[Unit] = Resources.character.map(_.render(128 - 8, 112 - 8, 16, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderShipRight: CanvasIO[Unit] = Resources.character.map(_.render(128 - 8, 112 - 8, 32, 0, 16, 16, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderJetLow: CanvasIO[Unit] = Resources.jets.map(_.render(128 - 8, 112 + 8, 0, 0, 16, 4, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)
  val renderJetHigh: CanvasIO[Unit] = Resources.jets.map(_.render(128 - 8, 112 + 8, 0, 4, 16, 4, Some(Color(255, 255, 255)))).getOrElse(CanvasIO.noop)

  def renderPlayer(keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val renderShip =
      if (keyboardInput.isDown(Key.Left)) renderShipLeft
      else if (keyboardInput.isDown(Key.Right)) renderShipRight
      else renderShipBase
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

  def renderGameState(state: AppState.GameState, keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val mapTransform =
      Transformation.Translate(-state.player.x, -state.player.y)
        .andThen(Transformation.Rotate(state.player.rotation))
        .andThen(Transformation.Translate(128, 112))
    val timeRiftTransform =
      Transformation.Translate(-128, -128)
        .andThen(Transformation.Rotate(state.timeRift.rotation))
        .andThen(Transformation.Translate(128, 128))
        .andThen(Transformation.Translate(state.timeRift.x - 128, state.timeRift.y - 128))
        .andThen(mapTransform)
    RenderOps.renderBackground
      .andThen(RenderOps.renderTransformed(state.level.track, mapTransform, Some(Color(0, 0, 0))))
      .andThen(RenderOps.renderPlayer(keyboardInput))
      .andThen(RenderOps.renderTransformed(Resources.timeRift.get, timeRiftTransform, Some(Color(255, 0, 255))))
  }

}
