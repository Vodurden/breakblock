package net.jakewoods.breakblock.rendering

import resource._
import cats._
import cats.implicits._

import java.nio._

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
import net.jakewoods.breakblock.opengl.math._
import net.jakewoods.breakblock.opengl.Shader._
import net.jakewoods.breakblock.game.data._

class OpenGLRenderer(initialState: GameState, systems: List[(FrameState, GameState) => GameState]) {
  val INT_BYTES = 4
  val FLOAT_BYTES = 4
  var gameState: GameState = initialState

  def run: Unit = {
    (for {
      window <- Window.create
      worldShader <- Shader.compileProgram("world")
    } yield loop(window, worldShader)).leftMap(e => {
      println(s"ERROR: ${e}")
    })
  }

  def loop(window: Window, worldShader: ShaderProgram): Unit = {
    glClearColor(0.01f, 0.01f, 0.01f, 0.0f)

    while(!glfwWindowShouldClose(window.id)) {
      // gather input
      val events = pollEvents(window)
      val frameState = FrameState(events = events)

      // update - ideally something else should handle this but for now
      //          we need to do it in this loop
      gameState = systems.foldLeft(gameState)((state, system) => system(frameState, state))

      render(window, worldShader, gameState)
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

  def render(window: Window, worldShader: ShaderProgram, gameState: GameState): Unit = {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    val worldToNormalProjection =
      Matrix4x4.forOrtho(0.0f, 640.0f, 480.0f, 0.0f, -1.0f, 1.0f).allocateBuffer

    // Render our rectangles
    for(stack <- managed(stackPush())) {
      val verticies: FloatBuffer = stack.floats(
        // Positions    // Textures
        0.0f, 1.0f,     0.0f, 1.0f,
        1.0f, 0.0f,     1.0f, 0.0f,
        0.0f, 0.0f,     0.0f, 0.0f,

        0.0f, 1.0f,     0.0f, 1.0f,
        1.0f, 1.0f,     1.0f, 1.0f,
        1.0f, 0.0f,     1.0f, 0.0f
      )

      val vao = glGenVertexArrays()
      val vbo = glGenBuffers()
      glBindVertexArray(vao)
      glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticies, GL_DYNAMIC_DRAW)

        // Position attribute
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * FLOAT_BYTES, 0)
        glEnableVertexAttribArray(0)

        // Texture attribute
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * FLOAT_BYTES, 2 * FLOAT_BYTES)
        glEnableVertexAttribArray(1)
      glBindBuffer(GL_ARRAY_BUFFER, 0)
      glBindVertexArray(0)

      // This tells me we need a much better way of encoding entities.
      // We basically want to get the entities that have
      // a position and rectangle component.
      val spatials = gameState.spatials
      val rectangles = gameState.sprites
      val renderables = ComponentMap.intersect(spatials, rectangles).getAll

      glUseProgram(worldShader)
      renderables.foreach { case(spatial, sprite) =>
        val translationMatrix = Matrix4x4.forTranslation(Vector3(spatial.x, spatial.y, 0.0f))
        val scaleMatrix = Matrix4x4.forScale(Vector3(spatial.width, spatial.height, 1.0f))
        val transformMatrix = translationMatrix * scaleMatrix

        val transformLocation = glGetUniformLocation(worldShader, "transform")
        glUniformMatrix4fv(transformLocation, false, transformMatrix.allocateBuffer)

        val projectionLocation = glGetUniformLocation(worldShader, "projection")
        glUniformMatrix4fv(projectionLocation, false, worldToNormalProjection)

        val color = sprite.color.allocateBuffer
        val colorLocation = glGetUniformLocation(worldShader, "spriteColor")
        glUniform3fv(colorLocation, color)

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
      }
    }

    glfwSwapBuffers(window.id)
  }
}
