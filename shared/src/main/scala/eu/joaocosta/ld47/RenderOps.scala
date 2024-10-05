package eu.joaocosta.ld47

import scala.util.Random

import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._

object RenderOps {
  def renderLogo(canvas: Canvas): Unit = canvas.blit(Resources.logo, BlendMode.ColorMask(Color(0, 0, 0)))(0, 0)
  def renderGameOver(canvas: Canvas): Unit = canvas.blit(Resources.gameOver, BlendMode.ColorMask(Color(0, 0, 0)))(16, 96)
  def renderBackground(canvas: Canvas): Unit = canvas.blit(Resources.background)(0, 0)

  def renderShipLeft(canvas: Canvas): Unit =
    canvas.blit(Resources.character.getSprite(0), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 - 8)
  def renderShipBase(canvas: Canvas): Unit =
    canvas.blit(Resources.character.getSprite(1), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 - 8)
  def renderShipRight(canvas: Canvas): Unit =
    canvas.blit(Resources.character.getSprite(2), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 - 8)

  def renderJetLow(canvas: Canvas): Unit =
    canvas.blit(Resources.jets.getSprite(0), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 + 8)
  def renderJetHigh(canvas: Canvas): Unit =
    canvas.blit(Resources.jets.getSprite(1), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 + 8)
  def renderJetBoostLow(canvas: Canvas): Unit =
    canvas.blit(Resources.jets.getSprite(2), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 + 8)
  def renderJetBoostHigh(canvas: Canvas): Unit =
    canvas.blit(Resources.jets.getSprite(3), BlendMode.ColorMask(Color(255, 255, 255)))(128 - 8, 112 + 8)

  def renderBoost(canvas: Canvas, boostLevel: Double): Unit = {
    canvas.blit(Resources.boostEmpty)(0, 0)
    canvas.blit(Resources.boostFull)(0, 0, 0, 0, (64 * boostLevel).toInt, 8)
  }

  def renderFuel(canvas: Canvas, fuelLevel: Double): Unit = {
    canvas.blit(Resources.fuelEmpty)(0, 8)
    canvas.blit(Resources.fuelFull)(0, 8, 0, 0, (64 * fuelLevel).toInt, 8)
  }

  def renderPlayer(canvas: Canvas, keyboardInput: KeyboardInput): Unit = {
    // Render Jets
    if (keyboardInput.isDown(Key.Up))
      if (keyboardInput.isDown(Key.Space))
        if (Random.nextBoolean()) renderJetBoostHigh(canvas) else renderJetBoostLow(canvas)
      else
        if (Random.nextBoolean()) renderJetHigh(canvas) else renderJetLow(canvas)
    // Render Ship
    if (keyboardInput.isDown(Key.Left)) renderShipLeft(canvas)
    else if (keyboardInput.isDown(Key.Right)) renderShipRight(canvas)
    else renderShipBase(canvas)
  }

  def renderGameState(canvas: Canvas, state: AppState.GameState, keyboardInput: KeyboardInput): Unit = {
    val map = state.level.track
      .translate(-state.player.x, -state.player.y)
      .rotate(-state.player.rotation)
      .translate(128, 112)
      .toSurfaceView(256, 224)
    val timeRift =
      Resources.timeRift
        .translate(-128, -128)
        .rotate(-state.timeRift.rotation)
        .translate(state.timeRift.x, state.timeRift.y)
        .translate(-state.player.x, -state.player.y)
        .rotate(-state.player.rotation)
        .translate(128, 112)
        .toSurfaceView(256, 224)
    renderBackground(canvas)
    canvas.blit(map, BlendMode.ColorMask(Color(0, 0, 0)))(0, 0)
    renderPlayer(canvas, keyboardInput)
    canvas.blit(timeRift, BlendMode.ColorMask(Color(255, 0, 255)))(0, 0)
    renderBoost(canvas, state.player.boost)
    renderFuel(canvas, state.player.fuel)
  }

}
