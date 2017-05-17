package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl._

case class Entity(name: Option[String])

// This is not the most efficient encoding. Consider something else later
sealed trait Component
case class PositionComponent(entity: Entity, x: Int, y: Int) extends Component
case class RectangleRenderComponent(entity: Entity, width: Int, height: Int) extends Component

case class GameState(
  positionComponents: List[PositionComponent],
  rectangleRenderComponents: List[RectangleRenderComponent]
)

case class GameStateChange(f: (GameState => GameState))

case class GameActions(
  frameEvents: List[Window.Event]
)
