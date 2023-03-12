package eu.joaocosta.ld47

import scala.util.Random

import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._

object RenderOps {
  lazy val renderLogo: CanvasIO[Unit] = CanvasIO.blit(Resources.logo, Some(Color(0, 0, 0)))(0, 0)
  lazy val renderGameOver: CanvasIO[Unit] = CanvasIO.blit(Resources.gameOver, Some(Color(0, 0, 0)))(16, 96)
  lazy val renderBackground: CanvasIO[Unit] = CanvasIO.blit(Resources.background)(0, 0)

  lazy val (renderShipLeft, renderShipBase, renderShipRight) = {
    val mask = Color(255, 255, 255)
    (
      CanvasIO.blit(Resources.character.getSprite(0), Some(mask))(128 - 8, 112 - 8),
      CanvasIO.blit(Resources.character.getSprite(1), Some(mask))(128 - 8, 112 - 8),
      CanvasIO.blit(Resources.character.getSprite(2), Some(mask))(128 - 8, 112 - 8))
  }

  lazy val (renderJetLow, renderJetHigh, renderJetBoostLow, renderJetBoostHigh) = {
    val mask = Color(255, 255, 255)
    (
      CanvasIO.blit(Resources.jets.getSprite(0), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(Resources.jets.getSprite(1), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(Resources.jets.getSprite(2), Some(mask))(128 - 8, 112 + 8),
      CanvasIO.blit(Resources.jets.getSprite(3), Some(mask))(128 - 8, 112 + 8))
  }

  def renderBoost(boostLevel: Double): CanvasIO[Unit] =
    CanvasIO.blit(Resources.boostEmpty)(0, 0).andThen(CanvasIO.blit(Resources.boostFull)(0, 0, 0, 0, (64 * boostLevel).toInt, 8))

  def renderFuel(fuelLevel: Double): CanvasIO[Unit] =
    CanvasIO.blit(Resources.fuelEmpty)(0, 8).andThen(CanvasIO.blit(Resources.fuelFull)(0, 8, 0, 0, (64 * fuelLevel).toInt, 8))

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

  def renderGameState(state: AppState.GameState, keyboardInput: KeyboardInput): CanvasIO[Unit] = {
    val map = state.level.track
      .translate(-state.player.x, -state.player.y)
      .rotate(state.player.rotation)
      .translate(128, 112)
    val timeRift =
      Resources.timeRift
        .translate(-128, -128)
        .rotate(state.timeRift.rotation)
        .translate(state.timeRift.x, state.timeRift.y)
        .translate(-state.player.x, -state.player.y)
        .rotate(state.player.rotation)
        .translate(128, 112)
    CanvasIO.sequence_(List(
      renderBackground,
      CanvasIO.blit(map.toSurfaceView(256, 224), Some(Color(0, 0, 0)))(0, 0),
      renderPlayer(keyboardInput),
      CanvasIO.blit(timeRift.toSurfaceView(256, 224), Some(Color(255, 0, 255)))(0, 0),
      renderBoost(state.player.boost),
      renderFuel(state.player.fuel)))
  }

}
