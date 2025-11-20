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
  var target: Option[EnemyTroop] = None
  var weapon: Weapon

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
      target = GameState.enemies
        .filter(!_.stats.isDead)
        .minByOption(enemy => position.dst(enemy.position))
    }
  }

  protected def updateMovement(delta: Float): Unit = {
    target match {
      case Some(enemy) =>
        val distance = position.dst(enemy.position)
        if (distance > stats.attackRange) {
          val direction = new Vector2(
            enemy.position.x - position.x,
            enemy.position.y - position.y
          ).nor()

          position.x += direction.x * stats.movementSpeed * delta
          position.y += direction.y * stats.movementSpeed * delta
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
}
