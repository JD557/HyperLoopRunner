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
    CanvasIO.blitWithMask(surface, Color(0, 0, 0))(0, 0)
  }.getOrElse(CanvasIO.noop)
  lazy val renderGameOver: CanvasIO[Unit] = Resources.gameOver.map { surface =>
    CanvasIO.blitWithMask(surface, Color(0, 0, 0))(16, 96)
  }.getOrElse(CanvasIO.noop)
  lazy val renderBackground: CanvasIO[Unit] = Resources.background.map { surface =>
    CanvasIO.blit(surface)(0, 0)
  }.getOrElse(CanvasIO.noop)

  lazy val (renderShipLeft, renderShipBase, renderShipRight) = Resources.character.map { surface =>
    val render = CanvasIO.blitWithMask(surface, Color(255, 255, 255)) _
    (
      render(128 - 8, 112 - 8, 0, 0, 16, 16),
      render(128 - 8, 112 - 8, 16, 0, 16, 16),
      render(128 - 8, 112 - 8, 32, 0, 16, 16))
  }.getOrElse((CanvasIO.noop, CanvasIO.noop, CanvasIO.noop))

  val (renderJetLow, renderJetHigh, renderJetBoostLow, renderJetBoostHigh) =
    Resources.jets.map { surface =>
      val render = CanvasIO.blitWithMask(surface, Color(255, 255, 255)) _
      (
        render(128 - 8, 112 + 8, 0, 0, 16, 4),
        render(128 - 8, 112 + 8, 0, 4, 16, 4),
        render(128 - 8, 112 + 8, 0, 8, 16, 4),
        render(128 - 8, 112 + 8, 0, 12, 16, 4))
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

  private val rows = (0 until 224)
  private val columns = (0 until 256)

  def renderTransformed(image: RamSurface, transform: Transformation, colorMask: Option[Color] = None) = CanvasIO.accessCanvas { canvas =>
    rows.foreach { y =>
      columns.foreach { x =>
        val (ix, iy) = transform(x, y)
        image.getPixel(ix.toInt, iy.toInt).foreach { color =>
          if (!colorMask.contains(color)) canvas.putPixel(x, y, color)
        }
      }
    }
  }

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
      renderTransformed(state.level.track, mapTransform, Some(Color(0, 0, 0))),
      renderPlayer(keyboardInput),
      renderTransformed(Resources.timeRift.get, timeRiftTransform, Some(Color(255, 0, 255))),
      renderBoost(state.player.boost),
      renderFuel(state.player.fuel)))
  }

}
