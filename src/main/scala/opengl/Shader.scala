package net.jakewoods.breakblock.opengl

import resource._
import cats._
import cats.implicits._

import scala.io.Source
import scala.util.Try

import org.lwjgl._
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.system.MemoryStack._

import java.nio._

object Shader {
  type Shader = Int
  type ShaderProgram = Int

  sealed trait Error
  case class ReadError(shaderName: String, inner: Throwable) extends Error
  case class VertexCompileError(error: String, source: String) extends Error
  case class FragmentCompileError(error: String, source: String) extends Error
  case class ProgramCompileError(error: String) extends Error

  def compileProgram(
    vertexShaderName: String, fragmentShaderName: String
  ): Either[Error, ShaderProgram] = {
    val readShader = (shaderName: String) => Try(Source.fromResource(s"glsl/${shaderName}").getLines.mkString("\n"))
      .toEither
      .leftMap(e => ReadError(shaderName, e))

    for {
      vertexShaderSource <- readShader(vertexShaderName)
      fragmentShaderSource <- readShader(fragmentShaderName)

      vertexShader <- compileVertexShader(vertexShaderSource)
      fragmentShader <- compileFragmentShader(fragmentShaderSource)
      program <- compileProgram(vertexShader, fragmentShader)
    } yield program
  }

  private def compileProgram(
    vertexShader: Shader, fragmentShader: Shader
  ): Either[ProgramCompileError, ShaderProgram] = {
    val shaderProgram = glCreateProgram()
    Memory.stackAlloc { stack =>
      glAttachShader(shaderProgram, vertexShader)
      glAttachShader(shaderProgram, fragmentShader)
      glLinkProgram(shaderProgram)

      val linkSucceded: IntBuffer = stack.callocInt(1)

      glGetProgramiv(shaderProgram, GL_LINK_STATUS, linkSucceded)
      if(linkSucceded.get(0) == GL_TRUE) {
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        Right(shaderProgram)
      } else {
        val errorLog = glGetProgramInfoLog(shaderProgram)

        Left(ProgramCompileError(errorLog))
      }
    }
  }

  private def compileVertexShader(source: String): Either[VertexCompileError, Shader] =
    compileShader(source, GL_VERTEX_SHADER).leftMap(e => VertexCompileError(e, source))

  private def compileFragmentShader(source: String): Either[FragmentCompileError, Shader] =
    compileShader(source, GL_FRAGMENT_SHADER).leftMap(e => FragmentCompileError(e, source))

  private def compileShader(source: String, shaderType: Int): Either[String, Shader] = {
    val shader = glCreateShader(shaderType)
    Memory.stackAlloc { stack =>
      glShaderSource(shader, source)
      glCompileShader(shader)

      val compileSucceded: IntBuffer = stack.callocInt(1)
      glGetShaderiv(shader, GL_COMPILE_STATUS, compileSucceded)

      if(compileSucceded.get(0) == GL_TRUE) {
        Right(shader)
      } else {
        val errorLog = glGetShaderInfoLog(shader)
        Left(errorLog)
      }
    }
  }
}
