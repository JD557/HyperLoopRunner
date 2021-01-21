package eu.joaocosta.ld47

sealed trait Transformation {
  def apply(x: Double, y: Double): (Double, Double)
  protected def unsafeApply(res: Transformation.MutRes)
  def andThen(that: Transformation) = Transformation.AndThen(this, that)
}
object Transformation {

  private class MutRes(var x: Double, var y: Double) {
    def update(newX: Double, newY: Double): this.type = {
      x = newX
      y = newY
      this
    }
    def asTuple = (x, y)
  }

  case class AndThen(t1: Transformation, t2: Transformation) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) = {
      val res = new MutRes(x, y)
      unsafeApply(res)
      res.asTuple
    }
    def unsafeApply(res: MutRes): Unit = {
      t2.unsafeApply(res)
      t1.unsafeApply(res)
    }
  }
  case class Scale(scale: Double) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) =
      (x / scale, y / scale)
    def unsafeApply(res: MutRes): Unit =
      res.update(res.x / scale, res.y / scale)
  }
  case class Translate(dx: Double, dy: Double) extends Transformation {
    def apply(x: Double, y: Double): (Double, Double) =
      (x - dx, y - dy)
    def unsafeApply(res: MutRes): Unit =
      res.update(res.x - dx, res.y - dy)
  }
  case class Rotate(theta: Double) extends Transformation {
    val ct = math.cos(theta)
    val st = math.sin(theta)
    def apply(x: Double, y: Double): (Double, Double) =
      (x * ct - y * st, x * st + y * ct)
    def unsafeApply(res: MutRes): Unit =
      res.update(res.x * ct - res.y * st, res.x * st + res.y * ct)
  }
}
