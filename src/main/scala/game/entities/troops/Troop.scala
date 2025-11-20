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
  var path: List[Vector2] = List()
  var pathIndex: Int = 0
  private var lastTargetSample: Vector2 = new Vector2(0, 0)
  private var lastTargetRef: Option[Troop] = None

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
      if (target != lastTargetRef) {
        path = List()
        pathIndex = 0
        lastTargetRef = target
      }
    }
  }

  protected def updateMovement(delta: Float): Unit = {
    target match {
      case Some(enemy) =>
        val distance = position.dst(enemy.position)
        if (distance > stats.attackRange) {
          val shouldRepath = path.isEmpty || pathIndex >= path.length ||
            lastTargetSample.dst2(enemy.position) > (GameState.gridCellSize * GameState.gridCellSize) * 0.25f

          if (shouldRepath) {
            path = GameState.findPath(position, enemy.position)
            pathIndex = 0
            lastTargetSample.set(enemy.position)
          }

          if (path.nonEmpty && pathIndex < path.length) {
            val waypoint = path(pathIndex)
            val toWaypoint = new Vector2(waypoint).sub(position)

            if (toWaypoint.len() < math.max(4f, radius / 2f)) {
              pathIndex += 1
            } else {
              lastAvoidance.set(0f, 0f)
              lastMoveDirection.set(toWaypoint)

              val moveVector = new Vector2(toWaypoint).nor().scl(stats.movementSpeed * delta)
              val desiredPosition = new Vector2(position).add(moveVector)
              val correctedPosition = GameState.resolveCollisions(desiredPosition, radius)

              position.set(correctedPosition)
            }
          }
        }
      case None =>
        path = List()
        pathIndex = 0
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

    if (path.nonEmpty) {
      shapeRenderer.setColor(Color.MAGENTA)
      val startPoint = new Vector2(position)
      val pathPoints = startPoint :: path
      pathPoints.sliding(2).foreach {
        case List(from, to) =>
          shapeRenderer.line(from.x, from.y, to.x, to.y)
        case _ =>
      }
    }
  }
}
