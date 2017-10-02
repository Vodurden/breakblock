package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.game.data.SpatialComponent._

case class GameStateInfo(
  collidingEntities: List[(Entity, SpatialComponent, Location, Float)]
)

object GameStateInfo {
  def analyze(gameState: GameState): GameStateInfo = {
    val collidingEntities = SpatialComponent.getColliding(gameState.spatials)

    GameStateInfo(collidingEntities)
  }
}
