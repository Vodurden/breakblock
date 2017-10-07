package net.jakewoods.breakblock.game.system

import net.jakewoods.breakblock.game.data._
import PhysicsSystem._
import Entity._

object DamageSystem {

  val system = (frame: FrameState, state: GameState) => {
    val state2 = applyDeath(state)

    state2
  }

  def onCollision(collision: Collision, frame: FrameState, state: GameState): GameState = {
    collision match {
      case EntityCollision(firstImpact, secondImpact) => {
        val state2 = applyCollisionDamage(firstImpact.entity, state)
        val state3 = applyCollisionDamage(secondImpact.entity, state2)

        applyDeath(state3)
      }
      case BoundaryCollision(_) => state
    }
  }

  def applyCollisionDamage(entity: Entity, state: GameState): GameState = {
    state.modify(entity) { (b: BreakableComponent) => b.copy(health = b.health - 1) }
  }

  def applyDeath(state: GameState): GameState = {
    state.deleteIf((breakable: BreakableComponent) => breakable.health <= 0)
  }
}
