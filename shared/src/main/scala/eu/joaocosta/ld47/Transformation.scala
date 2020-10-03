package eu.joaocosta.ld47

sealed trait Transformation {
  def apply(x: Double, y: Double): (Double, Double)
  def andThen(that: Transformation) = Transformation.AndThen(this, that)
}
object Transformation {
  case class AndThen(t1: Transformation, t2: Transformation) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) = {
      val (xx, yy) = t2(x, y)
      t1(xx, yy)
    }
  }
  case class Scale(scale: Double) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) =
      (x / scale, y / scale)
  }
  case class Translate(dx: Double, dy: Double) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) =
      (x - dx, y - dy)
  }
  case class Rotate(theta: Double) extends Transformation {
    private lazy val ct = math.cos(theta)
    private lazy val st = math.sin(theta)
    def apply(x: Double, y: Double): (Double, Double) =
      (x * ct - y * st, x * st + y * ct)
  }
}
