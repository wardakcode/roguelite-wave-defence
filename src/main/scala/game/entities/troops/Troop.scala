package game.entities.troops

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import game.systems.{Stats, Weapon}
import com.badlogic.gdx.graphics.Color
import game.core.GameState

trait Troop {
  var position: Vector2
  var stats: Stats
  var radius: Float
  var color: Color
  var target: Option[Troop] = None
  var weapon: Weapon

  // Pathfinding variables
  protected var currentPath: List[Vector2] = List()
  protected var pathfindingTimer: Float = 0f
  protected val pathfindingInterval: Float = 1.5f // Recalculate path every 0.5 seconds

  def isEnemy: Boolean

  def update(delta: Float): Unit = {
    if (!stats.isDead) {
      updateMovement(delta)
      updateCombat(delta)
      weapon.update(delta)
    }
  }

  def resetHp(): Unit = stats = stats.copy(hp = stats.maxHp)

  protected def updateMovement(delta: Float): Unit = {
    // Safety check
    if (GameState.pathfindingGrid == null) return

    pathfindingTimer -= delta

    target match {
      case Some(enemy) if !enemy.stats.isDead =>
        val distance = position.dst(enemy.position)

        // If out of attack range, move towards enemy
        if (distance > stats.attackRange) {
          // Recalculate path periodically or if we have no path
          if (pathfindingTimer <= 0 || currentPath.isEmpty) {
            GameState.pathfindingGrid.findPath(position, enemy.position) match {
              case Some(path) =>
                currentPath = path
                pathfindingTimer = pathfindingInterval
              case None =>
                // If no path found, try direct movement
                currentPath = List(enemy.position)
                pathfindingTimer = pathfindingInterval
            }
          }

          // Follow the path
          if (currentPath.nonEmpty) {
            val nextWaypoint = currentPath.head
            val directionToWaypoint = new Vector2(nextWaypoint).sub(position)
            val distanceToWaypoint = directionToWaypoint.len()

            if (distanceToWaypoint < 5f) {
              // Reached waypoint, move to next one
              currentPath = currentPath.tail
            } else {
              // Move towards waypoint
              directionToWaypoint.nor()
              position.x += directionToWaypoint.x * stats.movementSpeed * delta
              position.y += directionToWaypoint.y * stats.movementSpeed * delta
            }
          }
        } else {
          // Within attack range, clear path
          currentPath = List()
        }
      case _ =>
        // No valid target, clear path
        currentPath = List()
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

  def render(shapeRenderer: ShapeRenderer): Unit = {
    if (!stats.isDead) {
      // Render path (for debugging)
      renderPath(shapeRenderer)

      // Render troop
      shapeRenderer.setColor(color)
      shapeRenderer.circle(position.x, position.y, radius)

      // Render health bar
      renderHealthBar(shapeRenderer)

      // Render weapon
      weapon.render(shapeRenderer)
    }
  }

  private def renderPath(shapeRenderer: ShapeRenderer): Unit = {
    if (currentPath.nonEmpty) {
      shapeRenderer.setColor(0.5f, 0.5f, 1f, 0.3f)
      var prev = position
      currentPath.foreach { waypoint =>
        shapeRenderer.rectLine(prev.x, prev.y, waypoint.x, waypoint.y, 2f)
        prev = waypoint
      }
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
}