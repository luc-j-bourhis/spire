package spire.algebra

import spire.std._

import scala.{ specialized => spec }
import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import scala.annotation.tailrec

trait CoordinateSpace[V, @spec(Float, Double) F] extends InnerProductSpace[V, F] {
  def dimensions: Int

  def coord(v: V, i: Int): F  // = v dot axis(i)

  def axis(i: Int): V

  def _x(v: V): F = coord(v, 0)
  def _y(v: V): F = coord(v, 1)
  def _z(v: V): F = coord(v, 2)

  def basis: Vector[V] = Vector.tabulate(dimensions)(axis)

  def dot(v: V, w: V): F = {
    @tailrec def loop(sum: F, i: Int): F = if (i < dimensions) {
      loop(scalar.plus(sum, scalar.times(coord(v, i), coord(w, i))), i + 1)
    } else {
      sum
    }

    loop(scalar.zero, 0)
  }
}

object CoordinateSpace {
  @inline final def apply[V, @spec(Float,Double) F](implicit V: CoordinateSpace[V, F]) = V

  def seq[A, CC[A] <: SeqLike[A, CC[A]]](dimensions0: Int)(implicit field0: Field[A],
      cbf0: CanBuildFrom[CC[A], A, CC[A]]) = new SeqCoordinateSpace[A, CC[A]] {
    val scalar = field0
    val cbf = cbf0
    val dimensions = dimensions0
  }

  def array[@spec(Float, Double) A: Field: ClassTag](dimensions0: Int): CoordinateSpace[Array[A], A] =
    new CoordinateSpace[Array[A], A] {
      final val dimensions = dimensions0
      def scalar = Field[A]
      def zero: Array[A] = new Array[A](0)
      def negate(x: Array[A]): Array[A] = ArraySupport.negate(x)
      def plus(x: Array[A], y: Array[A]): Array[A] = ArraySupport.plus(x, y)
      override def minus(x: Array[A], y: Array[A]): Array[A] = ArraySupport.minus(x, y)
      def timesl(r: A, x: Array[A]): Array[A] = ArraySupport.timesl(r, x)
      override def dot(x: Array[A], y: Array[A]): A = ArraySupport.dot(x, y)
      def coord(v: Array[A], i: Int): A = v(i)
      def axis(i: Int): Array[A] = ArraySupport.axis(dimensions, i)
    }
}
