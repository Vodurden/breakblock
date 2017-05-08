package net.jakewoods.beatemup.opengl.math

import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}

trait Bufferable {
  def allocateBuffer: FloatBuffer
  def updateBuffer(buffer: FloatBuffer): Unit
}

private[math] object Buffers {
  def createFloatBuffer(size: Int): FloatBuffer = {
    val sizeInBytes = size << 2
    ByteBuffer.allocateDirect(sizeInBytes).order(ByteOrder.nativeOrder).asFloatBuffer()
  }
}
