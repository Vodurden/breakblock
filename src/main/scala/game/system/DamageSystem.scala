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
        val state3 = applyCollisionDamage(secondImpact.entity, state)

        applyDeath(state3)
      }
      case BoundaryCollision(_) => state
    }
  }

  def applyCollisionDamage(entity: Entity, state: GameState): GameState = {
    val damagedBreakables = state.breakables.adjust(entity, breakable => {
      breakable.copy(health = breakable.health - 1)
    })

    state.copy(breakables = damagedBreakables)
  }

  def applyDeath(state: GameState): GameState = {
    val deadEntities = state.breakables
      .filter { case (_, breakable) => breakable.health <= 0 }
      .keys

    state.deleteEntities(deadEntities)
  }
}
