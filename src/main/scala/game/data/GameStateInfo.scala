package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.game.data.SpatialComponent._

import Entity._

case class GameStateInfo(
  collidingEntities: List[(Entity, SpatialComponent, Location, Float)]
)

object GameStateInfo {
  def from(gameState: GameState): GameStateInfo = {
    val collidingEntities = SpatialComponent.getColliding(gameState.spatials)

    GameStateInfo(collidingEntities)
  }
}
