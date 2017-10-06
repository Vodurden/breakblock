package net.jakewoods.breakblock.game.system

import org.lwjgl.glfw.GLFW._

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.opengl.math._
import net.jakewoods.breakblock.game.data._

object PlayerInputSystem {
  val system = (frame: FrameState, state: GameState) => {
    frame.events.foldLeft(state)((s, event) => processEvent(event, s))
  }

  def processEvent(event: Window.Event, state: GameState): GameState = {
    event match {
      case Window.KeyPress(GLFW_KEY_A, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, -15)
      case Window.KeyPress(GLFW_KEY_D, _, GLFW_PRESS | GLFW_REPEAT, _) => movePaddleX(state, 15)
      case Window.KeyPress(GLFW_KEY_S, _, GLFW_PRESS | GLFW_REPEAT, _) => {
        val maybeState = for {
          paddle <- state.paddle
          spatial <- state.get[SpatialComponent](paddle)
        } yield state.mkBullet(
          spatial.centerX.toInt,
          spatial.y.toInt - 6,
          Vector3(1.0f, 1.0f, 1.0f)
        )

        maybeState.getOrElse(state)
      }
      case _ => state
    }
  }

  def movePaddleX(state: GameState, speed: Int): GameState = {
    println(s"Searching in: $state")

    state.paddle.map(paddle => {
      state.modify(paddle) { (spatial: SpatialComponent) => spatial.translate(speed, 0) }
    }).getOrElse(state)
  }
}
