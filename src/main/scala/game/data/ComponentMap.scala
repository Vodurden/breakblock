package net.jakewoods.breakblock.game.data

case class ComponentMap[A](components: Map[Entity, A]) {
  def findByEntity(entity: Entity): Option[A] = {
    components.get(entity)
  }

  def delete(entity: Entity): ComponentMap[A] = {
    ComponentMap(components - entity)
  }

  def update(entity: Entity, newComponent: A): ComponentMap[A] = {
    ComponentMap(components + (entity -> newComponent))
  }

  def get(e: Entity): A = components(e)
  def getAll: Iterable[A] = components.values

  def intersect[B](other: ComponentMap[B]): ComponentMap[(A, B)] = {
    ComponentMap.intersect(this, other)
  }
}

object ComponentMap {
  def empty[A]: ComponentMap[A] = ComponentMap[A](Map[Entity, A]())

  /** Returns a new component map containing entities which have components
    * in both component maps.
    */
  def intersect[A,B](a: ComponentMap[A], b: ComponentMap[B]): ComponentMap[(A, B)] = {
    val keysIntersection = a.components.keySet.intersect(b.components.keySet)

    ComponentMap(
      keysIntersection
        .map(key => (key, (a.components.get(key).get, b.components.get(key).get)))
        .toMap[Entity, (A,B)]
    )
  }
}
