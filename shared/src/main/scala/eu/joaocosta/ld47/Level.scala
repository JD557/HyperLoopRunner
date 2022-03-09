package eu.joaocosta.ld47

import eu.joaocosta.minart.extra._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.backend.defaults._

case class Level(
  track: RamSurface,
  collisionMap: RamSurface,
  startPosition: (Float, Float),
  riftWaypoints: List[(Float, Float)],
  riftSpeed: Float) {
  def initialState = AppState.GameState(
    level = this,
    player = AppState.GameState.Player(
      x = startPosition._1,
      y = startPosition._2,
      vx = 0,
      vy = 0,
      rotation = 0.0f,
      boost = 1.0f,
      fuel = 1.0f),
    timeRift = AppState.GameState.TimeRift(
      x = riftWaypoints.head._1,
      y = riftWaypoints.head._2,
      rotation = 0,
      currentWaypoint = 0))
}

object Level {
  val levels = List(
    Level(
      track = Image.loadPpmImage(Resource("assets/leveltut-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resource("assets/leveltut-col.ppm")).get,
      startPosition = (512, 128),
      riftWaypoints = List(
        (512, 524),
        (524, 512),
        (512, 500),
        (500, 512)),
      riftSpeed = 0.1f),
    Level(
      track = Image.loadPpmImage(Resource("assets/level1-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resource("assets/level1-col.ppm")).get,
      startPosition = (920, 580),
      riftWaypoints = List(
        (635, 750),
        (900, 750),
        (920, 700),
        (920, 130),
        (900, 84),
        (100, 84),
        (75, 150),
        (75, 880),
        (110, 960)),
      riftSpeed = 2.7f),
    Level(
      track = Image.loadPpmImage(Resource("assets/level2-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resource("assets/level2-col.ppm")).get,
      startPosition = (75, 660),
      riftWaypoints = List(
        (75, 920),
        (75, 490),
        (125, 325),
        (195, 225),
        (370, 115),
        (534, 97),
        (935, 410),
        (935, 710),
        (900, 730),
        (640, 730),
        (160, 920)),
      riftSpeed = 3.5f),
    Level(
      track = Image.loadPpmImage(Resource("assets/level3-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resource("assets/level3-col.ppm")).get,
      startPosition = (75, 550),
      riftWaypoints = List(
        (75, 850),
        (75, 220),
        (115, 130),
        (860, 95),
        (935, 130),
        (935, 515),
        (895, 515),
        (640, 340),
        (515, 680),
        (550, 725),
        (910, 725),
        (950, 820),
        (910, 935),
        (75, 935)),
      riftSpeed = 4.0f),
    Level(
      track = Image.loadPpmImage(Resource("assets/levelboss-map.ppm")).get,
      collisionMap = Image.loadPpmImage(Resource("assets/levelboss-col.ppm")).get,
      startPosition = (512, 768),
      riftWaypoints = List(
        (512, 524),
        (524, 512),
        (512, 500),
        (500, 512)),
      riftSpeed = 0.1f))
}

