package net.jakewoods.breakblock.game.system

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.game.data._

object PlayerInputSystem {
  val system = (actions: GameActions, state: GameState) => {
    actions.frameEvents.foldLeft(state)((s, event) => processEvent(event, s))
  }

  def processEvent(event: Window.Event, state: GameState): GameState = {
    event match {
      case Window.KeyPress(_, _, _, _) => state.copy(x = state.x + 1)
      case _ => state
    }
  }
}
