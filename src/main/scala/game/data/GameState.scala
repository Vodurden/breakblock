package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl._

case class GameState(x: Int)

case class GameStateChange(f: (GameState => GameState))

case class GameActions(
  frameEvents: List[Window.Event]
)
