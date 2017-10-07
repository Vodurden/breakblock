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
import net.jakewoods.breakblock.game.system._
import net.jakewoods.breakblock.game.data._

import monix.execution.Scheduler
import scala.concurrent.Await

import scala.concurrent.duration._
import scala.util.Random

object GameMain {
  def main(args: Array[String]): Unit = {
    println("Start game")
    val random = new Random()
    val genEntityId = () => random.nextInt(50000)

    val blueish = Vector3(0.0f, 0.0f, 0.5f)
    val initialState = GameState.empty
      .mkBall(() => 50)
      .mkPaddle(() => 100)
      .mkBrickLine(genEntityId)(22, 5, 7, 5, Vector3(0.0f, 0.0f, 0.5f))
      .mkBrickLine(genEntityId)(22, 30, 7, 5, Vector3(0.0f, 0.2f, 0.5f))
      .mkBrickLine(genEntityId)(22, 55, 7, 5, Vector3(0.0f, 0.5f, 0.5f))
      .mkBrickLine(genEntityId)(22, 80, 7, 5, Vector3(0.2f, 0.5f, 0.5f))
      .mkBrickLine(genEntityId)(22, 105, 7, 5, Vector3(0.5f, 0.5f, 0.2f))

    val updateState = (frameState: FrameState, gameState: GameState) => {
      val physicsSystem = new PhysicsSystem(List(DamageSystem.onCollision))

      val systems = List[(FrameState, GameState) => GameState](
        PlayerInputSystem.system,
        DamageSystem.system,
        physicsSystem.system
      )

      systems.foldLeft(gameState)((state, system) => {
        system(frameState, state)
      })
    }

    val renderer = new OpenGLRenderer(initialState, updateState)

    renderer.run
  }
}
