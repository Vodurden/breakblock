package net.jakewoods.breakblock.game.data

import scala.collection.immutable.IntMap

import Entity._

case class ComponentMap[A](intMap: IntMap[A]) {
  def findByEntity(entity: Entity): Option[A] = intMap.get(entity)

  def filter(f: ((Entity, A)) => Boolean): ComponentMap[A] = ComponentMap(intMap.filter(f))

  def delete(entity: Entity): ComponentMap[A] = {
    ComponentMap(intMap - entity)
  }

  def update(entity: Entity, newComponent: A): ComponentMap[A] = {
    ComponentMap(intMap + (entity -> newComponent))
  }

  def ++(right: Iterable[(Entity, A)]): ComponentMap[A] =
    update(right)

  def update(right: Iterable[(Entity, A)]): ComponentMap[A] =
    ComponentMap(this.intMap ++ right)

  def get(e: Entity): A = intMap(e)
  def values: Iterable[A] = intMap.values
  def keys: Iterable[Entity] = intMap.keys

  def mapValues[B](f: A => B): ComponentMap[B] = ComponentMap(intMap.transform((id: Entity, v: A) => f(v)))

  /** Returns a new component map containing entities which have components
    * in both component maps.
    */
  def intersection[B](right: ComponentMap[B]): ComponentMap[(A,B)] =
    intersectionWith((_: Entity, a: A, b: B) => (a, b))(right)

  def leftIntersection[B](right: ComponentMap[B]): ComponentMap[A] =
    intersectionWith((_: Entity, a: A, _: B) => a)(right)

  def rightInteresction[B](right: ComponentMap[B]): ComponentMap[B] =
    intersectionWith((_: Entity, _: A, b: B) => b)(right)

  def intersectionWith[B,C](f: (Entity,A,B) => C)(right: ComponentMap[B]): ComponentMap[C] =
    ComponentMap(this.intMap.intersectionWith(right.intMap, f))

  def toList: List[(Entity, A)] = intMap.toList
}

object ComponentMap {
  def empty[A]: ComponentMap[A] = ComponentMap[A](IntMap[A]())

}
