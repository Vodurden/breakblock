package net.jakewoods.breakblock.game.system

import net.jakewoods.breakblock.game.data._

object DamageSystem {
  val system = (frame: FrameState, state: GameState) => {
    val state1 = applyCollisionDamage(frame, state)
    val state2 = applyDeath(state1)

    state2
  }

  def applyCollisionDamage(frame: FrameState, state: GameState): GameState = {
    val collidingEntities = SpatialComponent.getColliding(state.spatials)
    val damagedBreakables = collidingEntities.map(entity => {
      println(s"DEATH COLLISION, ${frame.time}")
      state.breakables.findByEntity(entity).map(breakable => {
        (entity, breakable.copy(health = breakable.health - 1))
      })
    }).flatten.toMap

    state.copy(
      breakables = state.breakables ++ damagedBreakables
    )
  }

  def applyDeath(state: GameState): GameState = {
    val deadEntities = state.breakables
      .filter { case (_, breakable) => breakable.health <= 0 }
      .keys

    state.deleteEntities(deadEntities)
  }
}
