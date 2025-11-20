package game.entities.troops

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import game.systems.{Stats, Weapon}
import com.badlogic.gdx.graphics.Color
import game.core.GameState  // Added this import


trait Troop {
  var position: Vector2
  var stats: Stats
  var radius: Float
  var color: Color
  var target: Option[Troop] = None
  var weapon: Weapon
  var lastMoveDirection: Vector2 = new Vector2(0, 0)
  var lastAvoidance: Vector2 = new Vector2(0, 0)

  def isEnemy: Boolean

  def update(delta: Float): Unit = {
    if (!stats.isDead) {
      updateTarget()
      updateMovement(delta)
      updateCombat(delta)
      weapon.update(delta)
    }
  }

  protected def updateTarget(): Unit = {
    if (target.isEmpty || target.exists(_.stats.isDead)) {
      target = findTargets
        .filter(!_.stats.isDead)
        .minByOption(enemy => position.dst(enemy.position))
    }
  }

  protected def updateMovement(delta: Float): Unit = {
    target match {
      case Some(enemy) =>
        val distance = position.dst(enemy.position)
        if (distance > stats.attackRange) {
          val toTarget = new Vector2(
            enemy.position.x - position.x,
            enemy.position.y - position.y
          )

          val desiredDirection = if (toTarget.isZero) new Vector2(0, 0) else new Vector2(toTarget).nor()
          val avoidance = obstacleAvoidance()
          val moveDirection = desiredDirection.add(avoidance)

          lastAvoidance.set(avoidance)
          lastMoveDirection.set(moveDirection)

          if (!moveDirection.isZero) {
            moveDirection.nor().scl(stats.movementSpeed * delta)

            val desiredPosition = new Vector2(position).add(moveDirection)
            val correctedPosition = GameState.buildings.foldLeft(desiredPosition) { (pos, building) =>
              building.resolveCollision(pos, radius, GameState.buildingBuffer)
            }

            position.set(correctedPosition)
          }
        }
      case None => // No target behavior
    }
  }

  protected def updateCombat(delta: Float): Unit = {
    target.foreach { enemy =>
      val distance = position.dst(enemy.position)
      if (distance <= stats.attackRange && !enemy.stats.isDead) {
        weapon.attack(position, enemy.position, enemy)
      }
    }
  }

  protected def findTargets: List[Troop]

  private def obstacleAvoidance(): Vector2 = {
    GameState.buildings.foldLeft(new Vector2(0, 0)) { (acc, building) =>
      val closestPoint = building.closestPoint(position)
      val away = new Vector2(position).sub(closestPoint)
      val distance = away.len()
      val desiredSeparation = radius + GameState.buildingBuffer

      if (distance > 0 && distance < desiredSeparation) {
        val strength = (desiredSeparation - distance) / desiredSeparation
        acc.add(away.nor().scl(strength))
      } else {
        acc
      }
    }
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    if (!stats.isDead) {
      // Render troop
      shapeRenderer.setColor(color)
      shapeRenderer.circle(position.x, position.y, radius)

      // Render health bar
      renderHealthBar(shapeRenderer)

      // Render weapon
      weapon.render(shapeRenderer)
    }
  }

  private def renderHealthBar(shapeRenderer: ShapeRenderer): Unit = {
    val healthBarWidth = radius * 2
    val healthBarHeight = 4f
    val healthBarY = position.y + radius + 5f

    // Background (red)
    shapeRenderer.setColor(Color.RED)
    shapeRenderer.rect(
      position.x - radius,
      healthBarY,
      healthBarWidth,
      healthBarHeight
    )

    // Foreground (green)
    val healthPercentage = stats.hp / stats.maxHp
    shapeRenderer.setColor(Color.GREEN)
    shapeRenderer.rect(
      position.x - radius,
      healthBarY,
      healthBarWidth * healthPercentage,
      healthBarHeight
    )
  }

  def renderDebugVectors(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(Color.CYAN)
    shapeRenderer.line(position.x, position.y, position.x + lastMoveDirection.x * 20f, position.y + lastMoveDirection.y * 20f)

    shapeRenderer.setColor(Color.YELLOW)
    shapeRenderer.line(position.x, position.y, position.x + lastAvoidance.x * 30f, position.y + lastAvoidance.y * 30f)
  }
}
