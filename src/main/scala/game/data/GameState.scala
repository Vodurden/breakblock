package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl.math._

case class GameState(
  paddle: Option[Entity],

  spatials: ComponentMap[SpatialComponent],
  sprites: ComponentMap[SpriteComponent],
  breakables: ComponentMap[BreakableComponent]
) {
  def deleteEntity(entity: Entity): GameState = {
    this.copy(
      paddle = if(Some(entity) == this.paddle) None else this.paddle,

      spatials = spatials.delete(entity),
      sprites = sprites.delete(entity),
      breakables = breakables.delete(entity)
    )
  }

  def mkPaddle(rng: () => Int): GameState = {
    val paddle = Entity(rng())

    val width = 100
    val height = 10
    val x = (GameState.gameWidth / 2) - (width / 2)
    val y = GameState.gameHeight - (GameState.gameHeight / 20) - height

    val spatial = SpatialComponent.fromRectangle(x, y, width, height)
      .copy(collisionType = CollisionType.Static)

    val color = Vector3(0.5f, 0.0f, 0.0f) // Red-ish
    val sprite = SpriteComponent(color)

    this.copy(
      paddle = Some(paddle),
      spatials = spatials.update(paddle, spatial),
      sprites = sprites.update(paddle, sprite)
    )
  }

  def mkBall(rng: () => Int): GameState = {
    val ball = Entity(rng())

    val x = (GameState.gameWidth * 0.2).toInt
    val y = (GameState.gameHeight * 0.6).toInt

    val speed = 2
    val spatial = SpatialComponent.fromRectangle(x, y, 10, 10)
      .copy(
        velocity = Vector2(speed, speed),
        collisionType = CollisionType.Bounce
      )

    val color = Vector3(1.0f, 1.0f, 1.0f)
    val sprite = SpriteComponent(color)

    this.copy(
      spatials = spatials.update(ball, spatial),
      sprites = sprites.update(ball, sprite)
    )
  }

  def mkBrick(rng: () => Int)(x: Int, y: Int, color: Vector3): GameState = {
    val brick = Entity(rng())

    val spatial = SpatialComponent.fromRectangle(x, y, 80, 20)
      .copy(collisionType = CollisionType.Static)

    val sprite = SpriteComponent(color)

    val breakable = BreakableComponent(1)

    this.copy(
      spatials = spatials.update(brick, spatial),
      sprites = sprites.update(brick, sprite),
      breakables = breakables.update(brick, breakable)
    )
  }

  def mkBrickLine(rng: () => Int)(startX: Int, startY: Int, numBricks: Int, padding: Int, color: Vector3): GameState = {
    Range(0, numBricks).foldLeft(this)((state, i) => {
      val x = startX + (80 * i) + (padding * i)

      state.mkBrick(rng)(x, startY, color)
    })
  }
}

object GameState {
  val gameWidth = 640
  val gameHeight = 480

  def empty: GameState = GameState(
    paddle = None,
    spatials = ComponentMap.empty[SpatialComponent],
    sprites = ComponentMap.empty[SpriteComponent],
    breakables = ComponentMap.empty[BreakableComponent]
  )
}
