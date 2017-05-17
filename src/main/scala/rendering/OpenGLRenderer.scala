package net.jakewoods.breakblock.rendering

import cats._
import cats.implicits._

import scala.concurrent.Future
import scala.concurrent.duration._

import org.lwjgl._
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.system._
import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.system.MemoryStack._
import org.lwjgl.system.MemoryUtil._

import monix.execution.{Cancelable, CancelableFuture, Ack}
import monix.execution.Ack.Continue
import monix.execution.Scheduler
import monix.eval.Task
import monix.reactive.{Observable, Observer}

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.game.data._

class OpenGLRenderer(initialState: GameState, systems: List[(GameActions, GameState) => GameState]) {
  var gameState: GameState = initialState

  def run: Unit = {
    Window.create.map(window => {
      loop(window)
    }).leftMap( e => println(s"ERROR: ${e}"))
  }

  def loop(window: Window): Unit = {
    glClearColor(0.4f, 0.0f, 0.7f, 0.0f)

    while(!glfwWindowShouldClose(window.id)) {
      // gather input
      val events = pollEvents(window)
      val gameActions = GameActions(frameEvents = events)

      // update - ideally something else should handle this but for now
      //          we need to do it in this loop
      gameState = systems.foldLeft(gameState)((state, system) => system(gameActions, state))

      // render
      glfwSwapBuffers(window.id)
    }
  }

  def pollEvents(window: Window): List[Window.Event] = {
    // This is probably terrible, to-fix later.
    var events = scala.collection.mutable.MutableList[Window.Event]()

    val keyCallback = GLFWKeyCallback.create(new GLFWKeyCallback() {
      def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
        events += Window.KeyPress(key, scancode, action, mods)
      }
    })

    glfwSetKeyCallback(window.id, keyCallback)
    glfwPollEvents()
    glfwSetKeyCallback(window.id, null)

    events.toList
  }

  def render: Unit = {
  }
}
