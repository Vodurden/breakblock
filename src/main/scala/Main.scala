package net.jakewoods.breakblock

import scala.math.Pi

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

import net.jakewoods.breakblock.opengl._
import net.jakewoods.breakblock.opengl.math._

object Main {
  type Window = Long

  val INT_BYTES = 4
  val FLOAT_BYTES = 4

  def initialize(): Window = {
    GLFWErrorCallback.createPrint(System.err).set

    if(!glfwInit()) {
      println("Unable to initialize GLFW!")
      return NULL
    }

    // Configure GLFW
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    glfwWindowHint(GLFW_SAMPLES, 4)

    val window = glfwCreateWindow(640, 480, "Hello World!", NULL, NULL)
    if(window == NULL) {
      println("Failed to create GLFW window.")
      return NULL
    }

    glfwSetKeyCallback(window, handleKeypress _)

    // Get the thread stack and push a new frame
    for(stack <- managed(stackPush())) {
      val pWidth: IntBuffer = stack.mallocInt(1)
      val pHeight: IntBuffer = stack.mallocInt(1)

      glfwGetWindowSize(window, pWidth, pHeight)

      val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

      // Center the window
      glfwSetWindowPos(
        window,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
      );
    }

    glfwMakeContextCurrent(window)

    glfwSwapInterval(1)

    glfwShowWindow(window)

    GL.createCapabilities()

    window
  }

  def handleKeypress(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
    println("key pressed")
  }

  def drawTriangle(shader: Int, texture1: Int, texture2: Int): Unit = {
    for(stack <- managed(stackPush())) {
      val verticies: FloatBuffer = stack.floats(
        // Positions           // Colors           // Texture Coords
        0.5f,   0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 1.0f,   // Top Right
        0.5f,  -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 0.0f,   // Bottom Right
        -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f,   // Bottom Left
        -0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   0.0f, 1.0f    // Top Left
      )
      val indicies: IntBuffer = stack.ints(
        0, 1, 2, // First triangle
        2, 3, 0  // Second triangle
      )

      val rotation = Quaternion.forAxisAngle(Orientation.z, GLFW.glfwGetTime.toFloat * (Pi.toFloat / 4.0f))
      val translationMatrix = Matrix4x4.forTranslation(Vector3(0.5f, -0.5f, 0.0f))
      val rotationMatrix = Matrix4x4.forRotation(rotation)
      val scaleMatrix = Matrix4x4.forScale(Vector3(0.5f, 0.5f, 0.5f))
      val matrix = translationMatrix * scaleMatrix * rotationMatrix
      val matrixBuffer = matrix.allocateBuffer

      val vao = glGenVertexArrays()
      glBindVertexArray(vao)
        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticies, GL_DYNAMIC_DRAW)

        val ebo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicies, GL_DYNAMIC_DRAW)

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * FLOAT_BYTES, 0)
        glEnableVertexAttribArray(0)

        // Color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * FLOAT_BYTES, 3 * FLOAT_BYTES)
        glEnableVertexAttribArray(1)

        // Texture attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * FLOAT_BYTES, 6 * FLOAT_BYTES)
        glEnableVertexAttribArray(2)
      glBindVertexArray(0)

      glUseProgram(shader)

      val transformLocation = glGetUniformLocation(shader, "transform")
      glUniformMatrix4fv(transformLocation, false, matrixBuffer)

      glActiveTexture(GL_TEXTURE0)
      glBindTexture(GL_TEXTURE_2D, texture1)
      glUniform1i(glGetUniformLocation(shader, "ourTexture1"), 0)

      glActiveTexture(GL_TEXTURE1)
      glBindTexture(GL_TEXTURE_2D, texture2)
      glUniform1i(glGetUniformLocation(shader, "ourTexture2"), 1)

      glBindVertexArray(vao)
      glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
      glBindVertexArray(0)

      glBindTexture(GL_TEXTURE_2D, 0)
    }
  }

  def loop(window: Window, shader: Int, texture1: Int, texture2: Int) {
    glClearColor(0.4f, 0.0f, 0.7f, 0.0f)

    while(!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      drawTriangle(shader, texture1, texture2)

      glfwPollEvents()
      glfwSwapBuffers(window)
    }
  }

  def main(args: Array[String]): Unit = {
    println("Hello!")

    val window = initialize()

    val stuff = for {
      shader <- Shader.compileProgram("vertexShader.glsl", "fragmentShader.glsl")
      texture1 <- Texture.load("wall.jpg")
      texture2 <- Texture.load("awesomeface.png")
    } yield (shader, texture1, texture2)

    stuff.map { case (shader, texture1, texture2) =>
      loop(window, shader, texture1, texture2)
    }.leftMap { e => println(s"ERROR: ${e}")}
  }
}
