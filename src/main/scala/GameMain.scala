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
import net.jakewoods.breakblock.rendering.OpenGLRenderer
import net.jakewoods.breakblock.game.system.PlayerInputSystem
import net.jakewoods.breakblock.game.data._

import monix.execution.Scheduler
import scala.concurrent.Await
import scala.concurrent.duration._

object GameMain {
  def main(args: Array[String]): Unit = {
    println("Hello streaming!")

    val paddle = Entity(Some("paddle"))

    val positionComponents = List(
      PositionComponent(paddle, 320, 500)
    )

    val rectangleRenderComponents = List(
      RectangleRenderComponent(paddle, 50, 50)
    )

    val initialState = GameState(positionComponents, rectangleRenderComponents)

    val systems = List(
      PlayerInputSystem.system
    )

    val renderer = new OpenGLRenderer(initialState, systems)
    renderer.run
  }
}
