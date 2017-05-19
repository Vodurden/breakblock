package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl.math._

/** Defines how this object reacts when it collides with another object
  *
  * Static: No movement
  * Bounce: Reflect of the surface of the other collision
  */
sealed trait CollisionType
object CollisionType {
  case object Static extends CollisionType
  case object Bounce extends CollisionType
}

case class SpatialComponent(
  topLeftCorner: Vector2,
  bottomRightCorner: Vector2,

  velocity: Vector2,
  collisionType: CollisionType
) {
  def translate(xOffset: Float, yOffset: Float): SpatialComponent = {
    this.copy(
      topLeftCorner = Vector2(topLeftCorner.x + xOffset, topLeftCorner.y + yOffset),
      bottomRightCorner = Vector2(bottomRightCorner.x + xOffset, bottomRightCorner.y + yOffset)
    )
  }

  def translate(v: Vector2): SpatialComponent = {
    translate(v.x, v.y)
  }

  def x = topLeftCorner.x
  def y = topLeftCorner.y
  def width = Math.abs(topLeftCorner.x - bottomRightCorner.x)
  def height = Math.abs(topLeftCorner.y - bottomRightCorner.y)

  def x1 = topLeftCorner.x
  def y1 = topLeftCorner.y
  def x2 = bottomRightCorner.x
  def y2 = bottomRightCorner.y

  def centerX = topLeftCorner.x + (width / 2)
  def centerY = topLeftCorner.y + (height / 2)
}

object SpatialComponent {
  sealed trait Location
  object Location {
    case object Top extends Location
    case object Bottom extends Location
    case object Left extends Location
    case object Right extends Location
  }

  def fromRectangle(x: Float, y: Float, width: Float, height: Float): SpatialComponent = {
    SpatialComponent(
      topLeftCorner = Vector2(x, y),
      bottomRightCorner = Vector2(x + width, y + height),
      velocity = Vector2(0, 0),
      collisionType = CollisionType.Static
    )
  }

  def getColliding(spatials: ComponentMap[SpatialComponent]): List[Entity] = {
    spatials.components.toList.combinations(2).flatMap {
      case Seq((entityA, a), (entityB, b)) => {
        SpatialComponent.collision(a, b).map { case(aLocation, bLocation, overlap) =>
          List(entityA, entityB)
        }.getOrElse(List.empty)
      }
    }.toList
  }

  def mapColliding(spatials: ComponentMap[SpatialComponent])
    (f: (Entity, SpatialComponent, Location, Float) => SpatialComponent): ComponentMap[SpatialComponent] = {

    spatials.components.toList.combinations(2).foldLeft(spatials) {
      case (spatials, List((entityA, a), (entityB, b))) => {
        SpatialComponent.collision(a, b).map { case(aLocation, bLocation, overlap) =>
          val newA = f(entityA, a, aLocation, overlap)
          val newB = f(entityB, b, bLocation, overlap)

          spatials.update(entityA, newA).update(entityB, newB)
        }.getOrElse(spatials)
      }
    }
  }

  // Detects a collison between a and b. Returns the location of impact relative to (a, b)
  //
  // For example: If a was moving upwards and collided with b moving downwards then
  // the return value would be Some((Top, Bottom)) because A collided on it's top
  // and B collided on it's bottom.
  def collision(
    a: SpatialComponent, b: SpatialComponent
  ): Option[(Location, Location, Float)] = {
    if(a.topLeftCorner.x < b.bottomRightCorner.x &&
      a.bottomRightCorner.x > b.topLeftCorner.x &&
      a.topLeftCorner.y < b.bottomRightCorner.y &&
      a.bottomRightCorner.y > b.topLeftCorner.y) {

      val bottomCollision = Math.abs(a.bottomRightCorner.y - b.topLeftCorner.y)
      val topCollision = Math.abs(a.topLeftCorner.y - b.bottomRightCorner.y)
      val leftCollision = Math.abs(a.topLeftCorner.x - b.bottomRightCorner.x)
      val rightCollision = Math.abs(a.bottomRightCorner.x - b.topLeftCorner.x)
      val smallestCollision = List(bottomCollision, topCollision, leftCollision, rightCollision).min

      if(smallestCollision == bottomCollision) {
        Some(Location.Bottom, Location.Top, smallestCollision)
      } else if(smallestCollision == topCollision) {
        Some(Location.Top, Location.Bottom, smallestCollision)
      } else if(smallestCollision == rightCollision) {
        Some(Location.Right, Location.Left, smallestCollision)
      } else if(smallestCollision == leftCollision) {
        Some(Location.Left, Location.Right, smallestCollision)
      } else {
        None
      }
    } else {
      None
    }
  }
}
