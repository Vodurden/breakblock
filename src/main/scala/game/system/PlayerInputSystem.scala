package net.jakewoods.breakblock.game.system

import org.lwjgl.glfw.GLFW._

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.game.data._

object PlayerInputSystem {
  val system = (actions: GameActions, state: GameState) => {
    actions.frameEvents.foldLeft(state)((s, event) => processEvent(event, s))
  }

  def processEvent(event: Window.Event, state: GameState): GameState = {
    event match {
      case Window.KeyPress(GLFW_KEY_A, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, -1)
      case Window.KeyPress(GLFW_KEY_D, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, 1)
      case _ => state
    }
  }

  def movePaddleX(state: GameState, amount: Int): GameState = {
    println(s"Searching in: $state")

    val paddlePositionIndex = state.positionComponents.indexWhere((p: PositionComponent) => p.entity.name == Some("paddle"))
    val paddlePosition = state.positionComponents(paddlePositionIndex)
    val newPosition = paddlePosition.copy(x = paddlePosition.x + amount)
    val newPositions = state.positionComponents.updated(paddlePositionIndex, newPosition)

    state.copy(positionComponents = newPositions)
  }
}
