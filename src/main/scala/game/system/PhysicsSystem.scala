package net.jakewoods.breakblock.game.system

import net.jakewoods.breakblock.opengl.math._
import net.jakewoods.breakblock.game.data._
import Entity._
import PhysicsSystem._
import SpatialComponent.Location

import cats._
import cats.data._
import cats.implicits._

class PhysicsSystem(collisionHandlers: List[(Collision, FrameState, GameState) => GameState]) {
  val system = (frame: FrameState, gameState: GameState) => {
    val collisions = detectCollisions(gameState)

    val handlers = (handleCollision _) :: collisionHandlers
    val collidedState = collisions.foldLeft(gameState)((state, collision) => {
      handlers.foldLeft(state)((s, handler) => handler(collision, frame, s))
    })

    applyVelocity(frame, collidedState)
  }

  def detectCollisions(state: GameState): List[Collision] = {
    detectBoundaryCollisions(state) ++ detectEntityCollisions(state)
  }

  def detectBoundaryCollisions(state: GameState): List[Collision] = {
    state.cimapList[SpatialComponent, List[Collision]] { case (entity, spatial) =>
      val xCollision: Option[Collision] = if(spatial.topLeftCorner.x < 0) {
        Some(BoundaryCollision(Impact(entity, Location.Left, spatial.topLeftCorner.x * -1)))
      } else if(spatial.bottomRightCorner.x > GameState.gameWidth) {
        Some(BoundaryCollision(Impact(entity, Location.Right, spatial.bottomRightCorner.x - GameState.gameWidth)))
      } else {
        None
      }

      val yCollision: Option[Collision] = if(spatial.topLeftCorner.y < 0) {
        Some(BoundaryCollision(Impact(entity, Location.Top, spatial.topLeftCorner.y * -1)))
      } else if(spatial.bottomRightCorner.y > GameState.gameHeight) {
        Some(BoundaryCollision(Impact(entity, Location.Bottom, spatial.bottomRightCorner.y - GameState.gameHeight)))
      } else {
        None
      }

      List(xCollision, yCollision).unite
    }.flatten
  }

  def detectEntityCollisions(state: GameState): List[Collision] = {
    state.toList[SpatialComponent].combinations(2).map {
      case Seq((entityA, a), (entityB, b)) => {
        SpatialComponent.collision(a, b).map { case(aLocation, bLocation, overlap) =>
          val firstImpact = Impact(entityA, aLocation, overlap)
          val secondImpact = Impact(entityB, bLocation, overlap)

          EntityCollision(firstImpact, secondImpact)
        }
      }
    }.toList.unite
  }

  def handleCollision(
    collision: Collision,
    frame: FrameState,
    state: GameState
  ): GameState = {
    val impacts = collision match {
      case BoundaryCollision(impact) => List(impact)
      case EntityCollision(firstImpact, secondImpact) => List(firstImpact, secondImpact)
    }

    impacts.foldLeft(state) { case (s, impact) =>
      s.modify(impact.entity) { (spatial: SpatialComponent) =>
        val velocity = doVelocity(spatial, impact.location)
        val translation = doTranslation(spatial, impact.location, impact.depth)

        spatial.copy(velocity = velocity).translate(translation)
      }
    }
  }

  def applyVelocity(frame: FrameState, state: GameState): GameState = {
    state.cmap((spatial: SpatialComponent) => spatial.translate(spatial.velocity))
  }

  def doTranslation(
    a: SpatialComponent, location: SpatialComponent.Location, overlap: Float
  ): Vector2 = {
    if(a.collisionType == CollisionType.Bounce) {
      location match {
        case SpatialComponent.Location.Top => Vector2(0, overlap)
        case SpatialComponent.Location.Bottom => Vector2(0, -overlap)
        case SpatialComponent.Location.Left => Vector2(overlap, 0)
        case SpatialComponent.Location.Right => Vector2(-overlap, 0)
      }
    } else {
      Vector2(0, 0)
    }
  }

  def doVelocity(
    spatial: SpatialComponent, location: SpatialComponent.Location
  ): Vector2 = {
    (spatial.collisionType, location) match {
      case (CollisionType.Static, _) => Vector2(0, 0)
      case (CollisionType.Bounce, SpatialComponent.Location.Top | SpatialComponent.Location.Bottom) =>
        Vector2(spatial.velocity.x, spatial.velocity.y * -1.0f)
      case (CollisionType.Bounce, SpatialComponent.Location.Left | SpatialComponent.Location.Right) =>
        Vector2(spatial.velocity.x * -1.0f, spatial.velocity.y)
    }
  }
}

object PhysicsSystem {
  /** Describes a collision between two entities.
    *
    * Provides an `impact` for each entity that describes the collision
    * from it's perspective.
    */
  sealed trait Collision
  case class EntityCollision(firstImpact: Impact, secondImpact: Impact) extends Collision
  case class BoundaryCollision(impact: Impact) extends Collision

  /** Describes a collision from the perspective of `entity`.
    *
    * entity: The entity that was impacted by this collision
    * location: The location relative to the entity where the collision occurred
    * depth: How deeply in units does the colliding object penetrate `entity`
    */
  case class Impact(entity: Entity, location: Location, depth: Float)
}
