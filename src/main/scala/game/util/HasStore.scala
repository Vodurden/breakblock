package net.jakewoods.breakblock.game.util

import net.jakewoods.breakblock.game.data.ComponentMap

import scala.collection.immutable.IntMap

trait HasStore[S, C] {
  def get(s: S): IntMap[C]
  def set(s: S, c: IntMap[C]): S
  def update(s: S, f: IntMap[C] => IntMap[C]): S = {
    set(s, f(get(s)))
  }
}

object HasStore {
  implicit def tuple2[S, A, B](
    implicit ha: HasStore[S, A], hb: HasStore[S, B]
  ): HasStore[S, (A,B)] = new HasStore[S, (A,B)] {
    override def get(s: S): IntMap[(A, B)] = {
      ha.get(s).intersectionWith(hb.get(s), (e: Int, a: A, b: B) => (a, b))
    }

    override def set(s: S, c: IntMap[(A, B)]): S = {
      val aValues: IntMap[A] = c.transform((entity, t: (A,B)) => t._1)
      val bValues: IntMap[B] = c.transform((entity, t: (A,B)) => t._2)

      val state2 = ha.update(s, existing => existing ++ aValues)
      val state3 = hb.update(state2, existing => existing ++ bValues)

      state3
    }
  }
}
