package eu.joaocosta.ld47

import scala.util.Random

import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._

object RenderOps {
  lazy val renderLogo: CanvasIO[Unit] = Resources.logo.map { surface =>
    CanvasIO.blit(surface, Some(Color(0, 0, 0)))(0, 0)
  }.getOrElse(CanvasIO.noop)
  lazy val renderGameOver: CanvasIO[Unit] = Resources.gameOver.map { surface =>
    CanvasIO.blit(surface, Some(Color(0, 0, 0)))(16, 96)
  }.getOrElse(CanvasIO.noop)
  lazy val renderBackground: CanvasIO[Unit] = Resources.background.map { surface =>
    CanvasIO.blit(surface)(0, 0)
  }.getOrElse(CanvasIO.noop)

  lazy val (renderShipLeft, renderShipBase, renderShipRight) = Resources.character.map { surface =>
    val mask = Color(255, 255, 255)
    (
      CanvasIO.blit(surface.getSprite(0), Some(mask))(128 - 8, 112 - 8),
      CanvasIO.blit(surface.getSprite(1), Some(mask))(128 - 8, 112 - 8),
      CanvasIO.blit(surface.getSprite(2), Some(mask))(128 - 8, 112 - 8))
  }.getOrElse((CanvasIO.noop, CanvasIO.noop, CanvasIO.noop))

  lazy val (renderJetLow, renderJetHigh, renderJetBoostLow, renderJetBoostHigh) = Resources.jets.map { surface =>
    val mask = Color(255, 255, 255)
    (
      CanvasIO.blit(surface.getSprite(0), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(surface.getSprite(1), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(surface.getSprite(2), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(surface.getSprite(3), Some(mask))(128 - 8, 112 + 8))
  }.getOrElse((CanvasIO.noop, CanvasIO.noop, CanvasIO.noop, CanvasIO.noop))

  def renderBoost(boostLevel: Double): CanvasIO[Unit] =
    (for {
      boostEmpty <- Resources.boostEmpty
      boostFull <- Resources.boostFull
    } yield CanvasIO.blit(boostEmpty)(0, 0).andThen(CanvasIO.blit(boostFull)(0, 0, 0, 0, (64 * boostLevel).toInt, 8))).getOrElse(CanvasIO.noop)

  def renderFuel(fuelLevel: Double): CanvasIO[Unit] =
    (for {
      fuelEmpty <- Resources.fuelEmpty
      fuelFull <- Resources.fuelFull
    } yield CanvasIO.blit(fuelEmpty)(0, 8).andThen(CanvasIO.blit(fuelFull)(0, 8, 0, 0, (64 * fuelLevel).toInt, 8))).getOrElse(CanvasIO.noop)

  def renderPlayer(keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val renderShip =
      if (keyboardInput.isDown(Key.Left)) renderShipLeft
      else if (keyboardInput.isDown(Key.Right)) renderShipRight
      else renderShipBase
    val renderJets: CanvasIO[Unit] =
      if (keyboardInput.isDown(Key.Up))
        if (keyboardInput.isDown(Key.Space))
          CanvasIO.suspend(Random.nextBoolean()).flatMap(if (_) renderJetBoostHigh else renderJetBoostLow)
        else
          CanvasIO.suspend(Random.nextBoolean()).flatMap(if (_) renderJetHigh else renderJetLow)
      else CanvasIO.noop
    renderJets.andThen(renderShip)
  }

  def renderTransformed(image: RamSurface, transform: Transformation, colorMask: Color) = CanvasIO.blit(
    Plane.fromSurfaceWithFallback(image, colorMask)
      .contramap((x, y) => transform.applyInt(x, y))
      .toSurfaceView(256, 224),
    Some(colorMask))(0, 0)

  def renderGameState(state: AppState.GameState, keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val mapTransform =
      Transformation.Translate(-state.player.x, -state.player.y)
        .andThen(Transformation.Rotate(state.player.rotation))
        .andThen(Transformation.Translate(128, 112))
    val timeRiftTransform =
      Transformation.Translate(-128, -128)
        .andThen(Transformation.Rotate(state.timeRift.rotation))
        .andThen(Transformation.Translate(state.timeRift.x, state.timeRift.y))
        .andThen(mapTransform)
    CanvasIO.sequence_(List(
      renderBackground,
      renderTransformed(state.level.track, mapTransform, Color(0, 0, 0)),
      renderPlayer(keyboardInput),
      renderTransformed(Resources.timeRift.get, timeRiftTransform, Color(255, 0, 255)),
      renderBoost(state.player.boost),
      renderFuel(state.player.fuel)))
  }

}
