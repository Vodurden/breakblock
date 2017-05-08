package net.jakewoods.breakblock.opengl

import resource._
import cats._
import cats.implicits._

import monix.execution.{Cancelable,Ack}
import monix.execution.cancelables.SingleAssignmentCancelable
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.Unbounded

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

case class Window(id: Long) {
  lazy val keypressObservable: Observable[Window.Event] =
    Observable.create(Unbounded) { subscriber =>
      val c = SingleAssignmentCancelable()
      val onKeyPress = GLFWKeyCallback.create(new GLFWKeyCallback() {
        def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
          subscriber.onNext(Window.KeyPress(key, scancode, action, mods))
        }
      })

      glfwSetKeyCallback(id, onKeyPress)
      c := Cancelable(() => glfwSetKeyCallback(id, null))
    }
}

object Window {
  sealed trait Error
  case class GlfwInitializationError() extends Error
  case class WindowCreationError() extends Error

  sealed trait Event
  case class KeyPress(key: Int, scancode: Int, action: Int, mods: Int) extends Event

  def create(): Either[Error, Window] = {
    if(!glfwInit()) {
      return Left(GlfwInitializationError())
    }

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    glfwWindowHint(GLFW_SAMPLES, 4)

    val windowId = glfwCreateWindow(640, 480, "Breakblock!", NULL, NULL)
    if(windowId == NULL) {
      return Left(WindowCreationError())
    }

    // Get the thread stack and push a new frame
    for(stack <- managed(stackPush())) {
      val pWidth: IntBuffer = stack.mallocInt(1)
      val pHeight: IntBuffer = stack.mallocInt(1)

      glfwGetWindowSize(windowId, pWidth, pHeight)

      val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

      // Center the window
      glfwSetWindowPos(
        windowId,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
      );
    }

    glfwMakeContextCurrent(windowId)
    glfwSwapInterval(1)
    glfwShowWindow(windowId)
    GL.createCapabilities()

    Right(Window(id = windowId))
  }
}
