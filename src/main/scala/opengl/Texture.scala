package net.jakewoods.beatemup.opengl

import resource._
import cats._
import cats.implicits._

import scala.util.Try

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import org.lwjgl._
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL20._

import java.nio._
import java.io.FileNotFoundException

object Texture {
  type Texture = Int

  sealed trait Error
  case class ReadError(textureName: String, inner: Throwable) extends Error
  case class ImageReadError(textureName: String, inner: Throwable) extends Error

  val BYTES_PER_PIXEL = 4 // 3 for RGB, 4 for RGBA

  def load(name: String): Either[Error, Texture] = {
    for {
      image <- loadImage(name)
    } yield loadTexture(image)
  }

  def loadTexture(image: BufferedImage): Texture = {
    val width = image.getWidth
    val height = image.getHeight
    val pixels = Array.fill(width * height){0}
    image.getRGB(0, 0, width, height, pixels, 0, width)

    // We're not using the stack for this buffer since images are rather large.
    val buffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL)

    for(y <- Range(0, height)) {
      for(x <- Range(0, width)) {
        val pixel = pixels(y * width + x);
        buffer.put(((pixel >> 16) & 0xFF).toByte);     // Red component
        buffer.put(((pixel >> 8) & 0xFF).toByte);      // Green component
        buffer.put((pixel & 0xFF).toByte);             // Blue component
        buffer.put(((pixel >> 24) & 0xFF).toByte);     // Alpha component. Only for RGBA
      }
    }

    buffer.flip()

    // Now that we have our image we need to bind it to a texture
    val texture = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, texture)

    // Wrapping
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    // Filtering/scaling
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

    // Data
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

    // Unbind our data
    glBindTexture(GL_TEXTURE_2D, 0);

    texture
  }

  def loadImage(name: String): Either[Error, BufferedImage] = {
    val classLoader = Thread.currentThread().getContextClassLoader()

    val textureFileEither = Try(classLoader.getResourceAsStream(s"texture/${name}"))
      .toEither
      .leftMap(e => ReadError(name, e))
      .ensure(ReadError(name, new FileNotFoundException(s"texture/${name}")))(v => v != null)

    val textureDataEither = textureFileEither.flatMap { textureFile =>
      Try(ImageIO.read(textureFile))
        .toEither
        .leftMap(e => ImageReadError(name, e))
    }

    textureFileEither.foreach(textureFile => textureFile.close)

    textureDataEither
  }
}
