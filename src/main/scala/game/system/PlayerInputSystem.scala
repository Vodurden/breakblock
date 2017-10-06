package net.jakewoods.breakblock.game.system

import org.lwjgl.glfw.GLFW._

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.game.data._

object PlayerInputSystem {
  val system = (frame: FrameState, state: GameState) => {
    frame.events.foldLeft(state)((s, event) => processEvent(event, s))
  }

  def processEvent(event: Window.Event, state: GameState): GameState = {
    event match {
      case Window.KeyPress(GLFW_KEY_A, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, -15)
      case Window.KeyPress(GLFW_KEY_D, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, 15)
      case _ => state
    }
  }

  def movePaddleX(state: GameState, speed: Int): GameState = {
    println(s"Searching in: $state")

    state.paddle.map(paddle => {
      val paddleSpatial = state.spatials.findByEntity(paddle).get
      val newSpatial = paddleSpatial.translate(speed, 0)
      val newSpatials = state.spatials.update(paddle, newSpatial)
      state.copy(spatials = newSpatials)
    }).getOrElse(state)
  }
}
