package net.jakewoods.beatemup.opengl

import resource._

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack._

object Memory {
  def stackAlloc[A](body: MemoryStack => A): A = {
    val stack = stackPush()
    val result = body(stack)
    stack.close()
    result
  }
}
