package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl._

case class FrameState(
  time: Double,
  events: List[Window.Event]
)
