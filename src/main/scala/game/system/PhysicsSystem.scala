package net.jakewoods.breakblock.game.system

import net.jakewoods.breakblock.opengl.math._
import net.jakewoods.breakblock.game.data._

import cats._
import cats.implicits._

object PhysicsSystem {
  val system = (frame: FrameState, state: GameState) => {
    val s = applyBoundaryCollision(frame, state)
    val s1 = applyCollision(frame, s)
    val s2 = applyVelocity(frame, s1)

    s2
  }

  def applyVelocity(frame: FrameState, state: GameState): GameState = {
    val newSpatials = ComponentMap(state.spatials.components.mapValues(spatial => {
      spatial.translate(spatial.velocity)
    }))

    state.copy(spatials = newSpatials)
  }

  /** Prevent objects from escaping the map boundary
    */
  def applyBoundaryCollision(frame: FrameState, state: GameState): GameState = {
    val newSpatials = state.spatials.components.mapValues(spatial => {
      val xTranslation = if(spatial.topLeftCorner.x < 0) {
        (-1.0f * spatial.topLeftCorner.x)
      } else if(spatial.bottomRightCorner.x > GameState.gameWidth) {
        (GameState.gameWidth - spatial.bottomRightCorner.x)
      } else {
        0
      }

      val yTranslation = if(spatial.topLeftCorner.y < 0) {
        (-1.0f * spatial.topLeftCorner.y)
      } else if(spatial.bottomRightCorner.y > GameState.gameHeight) {
        (GameState.gameHeight - spatial.bottomRightCorner.y)
      } else {
        0
      }

      // If we need to move then we also need to apply our collison model
      val xVelocity = if(xTranslation != 0) {
        spatial.collisionType match {
          case CollisionType.Static => 0
          case CollisionType.Bounce => spatial.velocity.x * -1.0f
        }
      } else {
        spatial.velocity.x
      }

      val yVelocity = if(yTranslation != 0) {
        spatial.collisionType match {
          case CollisionType.Static => 0
          case CollisionType.Bounce => spatial.velocity.y * -1.0f
        }
      } else {
        spatial.velocity.y
      }

      spatial.translate(xTranslation, yTranslation).copy(velocity = Vector2(xVelocity, yVelocity))
    })

    state.copy(spatials = ComponentMap(newSpatials))
  }

  def applyCollision(frame: FrameState, state: GameState): GameState = {
    val newSpatials = SpatialComponent
      .mapColliding(state.spatials)((_, spatial, location, overlap) => {
        println(s"COLLISION, ${frame.time}")
        val velocity = doVelocity(spatial, location)
        val translation = doTranslation(spatial, location, overlap)

        spatial.copy(velocity = velocity).translate(translation)
      })

    state.copy(spatials = newSpatials)
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
