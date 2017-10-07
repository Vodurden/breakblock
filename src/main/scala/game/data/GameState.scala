package net.jakewoods.breakblock.game.data

import net.jakewoods.breakblock.opengl.math._
import net.jakewoods.breakblock.game.util.HasStore
import Entity._

import scala.util.Random
import cats._
import cats.data._
import cats.implicits._

import scala.collection.immutable.IntMap

case class GameState(
  spatials: IntMap[SpatialComponent],
  sprites: IntMap[SpriteComponent],
  breakables: IntMap[BreakableComponent],
  players: IntMap[PlayerComponent]
) {
  def deleteEntity(entity: Entity): GameState = {
    this.copy(
      spatials = spatials - entity,
      sprites = sprites - entity,
      breakables = breakables - entity
    )
  }

  def deleteEntities(entities: Iterable[Entity]): GameState = {
    entities.foldLeft(this) { (s, entity) => s.deleteEntity(entity) }
  }

  def update[C](f: IntMap[C] => IntMap[C])(implicit hs: HasStore[GameState, C]): GameState = {
    hs.set(this, f(hs.get(this)))
  }

  def get[C](entity: Entity)(implicit hs: HasStore[GameState, C]): Option[C] = {
    hs.get(this).get(entity)
  }

  def add[C : HasStore[GameState, ?]](entity: Entity, c: C): GameState = {
    update[C](store => store + (entity -> c))
  }

  def modify[C : HasStore[GameState, ?]](entity: Entity)(f: (C => C)): GameState = {
    update[C] { store =>
      store.get(entity).map { component =>
        store + (entity -> f(component))
      }.getOrElse(store)
    }
  }

  def delete[C : HasStore[GameState, ?]](entity: Entity): GameState = {
    update[C] { store => store - entity }
  }

  def deleteIf[C : HasStore[GameState, ?]](f: C => Boolean): GameState = {
    val toDelete = cimapList((e: Entity, c: C) => if(f(c)) Some(e) else None).unite
    deleteEntities(toDelete)
  }

  // Component map: Map a pure function over all entities in C
  def cmap[C](f: C => C)(implicit hs: HasStore[GameState, C]): GameState = {
    cimap((_: Entity, c: C) => f(c))
  }

  // Indexed component map
  def cimap[C](f: (Entity, C) => C)(implicit hs: HasStore[GameState, C]): GameState = {
    update[C](store => store.map { case (e, c) => (e, f(e, c))})
  }

  // Component map that can delete components in it's domain
  def cmapD[C](f: C => Option[C])(implicit hs: HasStore[GameState, C]): GameState = {
    cimapD[C]((_, c) => f(c))
  }

  def cimapD[C](f: (Entity, C) => Option[C])(implicit hs: HasStore[GameState, C]): GameState = {
    update[C](store => store.modifyOrRemove(f))
  }


  def cmapList[C, A](f: C => A)(implicit hs: HasStore[GameState, C]): List[A] = {
    cimapList((_: Entity, c: C) => f(c))
  }

  def cimapList[C, A](f: (Entity, C) => A)(implicit hs: HasStore[GameState, C]): List[A] = {
    this.toList.map { case (e, c) => f(e, c) }
  }

  def toList[C](implicit hs: HasStore[GameState, C]): List[(Entity, C)] = {
    hs.get(this).toList
  }

  def mkPaddle(rng: () => Int): GameState = {
    val paddle: Entity = rng()

    val width = 100
    val height = 10
    val x = (GameState.gameWidth / 2) - (width / 2)
    val y = GameState.gameHeight - (GameState.gameHeight / 20) - height

    val spatial = SpatialComponent.fromRectangle(x, y, width, height)
      .copy(collisionType = CollisionType.Static)

    val color = Vector3(0.5f, 0.0f, 0.0f) // Red-ish
    val sprite = SpriteComponent(color)

    this.add(paddle, spatial)
      .add(paddle, sprite)
      .add(paddle, PlayerComponent())
  }

  def mkBall(rng: () => Int): GameState = {
    val ball: Entity = rng()

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

    this.add(ball, spatial)
      .add(ball, sprite)
  }

  def mkBullet(x: Int, y: Int, color: Vector3): GameState = {
    val random = new Random()
    val genEntityId = () => random.nextInt(50000)
    val bullet: Entity = genEntityId()

    val spatial = SpatialComponent.fromRectangle(x, y, 5, 5)
      .copy(
        velocity = Vector2(0, -5),
        collisionType = CollisionType.Static
      )

    val sprite = SpriteComponent(color)
    val breakable = BreakableComponent(1)

    this.add(bullet, spatial)
      .add(bullet, sprite)
      .add(bullet, breakable)
  }

  def mkBrick(rng: () => Int)(x: Int, y: Int, color: Vector3): GameState = {
    val brick: Entity = rng()

    val spatial = SpatialComponent.fromRectangle(x, y, 80, 20)
      .copy(collisionType = CollisionType.Static)

    val sprite = SpriteComponent(color)

    val breakable = BreakableComponent(1)

    this.add(brick, spatial).add(brick, sprite).add(brick, breakable)
  }

  def mkBrickLine(rng: () => Int)(startX: Int, startY: Int, numBricks: Int, padding: Int, color: Vector3): GameState = {
    Range(0, numBricks).foldLeft(this)((state, i) => {
      val x = startX + (80 * i) + (padding * i)

      state.mkBrick(rng)(x, startY, color)
    })
  }
}

object GameState {
  implicit val hasSpatials: HasStore[GameState, SpatialComponent]
    = new HasStore[GameState, SpatialComponent] {
      override def get(s: GameState) = s.spatials
      override def set(s: GameState, c: IntMap[SpatialComponent]) = s.copy(spatials = c)
    }

  implicit val hasSprites: HasStore[GameState, SpriteComponent] =
    new HasStore[GameState, SpriteComponent] {
      override def get(s: GameState) = s.sprites
      override def set(s: GameState, c: IntMap[SpriteComponent]) = s.copy(sprites = c)
    }

  implicit val hasBreakables: HasStore[GameState, BreakableComponent] =
    new HasStore[GameState, BreakableComponent] {
      override def get(s: GameState) = s.breakables
      override def set(s: GameState, c: IntMap[BreakableComponent]) = s.copy(breakables = c)
    }

  implicit val hasPlayers: HasStore[GameState, PlayerComponent] =
    new HasStore[GameState, PlayerComponent] {
      override def get(s: GameState) = s.players
      override def set(s: GameState, c: IntMap[PlayerComponent]) = s.copy(players = c)
    }

  val gameWidth = 640
  val gameHeight = 480

  def empty: GameState = GameState(
    spatials = IntMap.empty[SpatialComponent],
    sprites = IntMap.empty[SpriteComponent],
    breakables = IntMap.empty[BreakableComponent],
    players = IntMap.empty[PlayerComponent]
  )
}
