package game.systems

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import game.core.GameState
import game.entities.troops.Troop

trait Weapon {
  var owner: Troop  // Reference to the troop that owns this weapon
  var attackTimer: Float = 0

  def update(delta: Float): Unit = {
    if (attackTimer > 0) attackTimer -= delta
  }

  def render(shapeRenderer: ShapeRenderer): Unit

  def attack(source: Vector2, target: Vector2, enemy: Troop): Unit

  def canAttack: Boolean = attackTimer <= 0
}

class MeleeWeapon(var owner: Troop) extends Weapon {
  def render(shapeRenderer: ShapeRenderer): Unit = {
    // Could render swing animation here
  }

  def attack(source: Vector2, target: Vector2, enemy: Troop): Unit = {
    if (canAttack) {
      // Apply direct damage
      enemy.stats = enemy.stats.takeDamage(owner.stats.damage)
      attackTimer = 1.0f / owner.stats.attackSpeed  // Reset attack timer based on attack speed
    }
  }
}

class RangedWeapon(var owner: Troop) extends Weapon {
  private var projectiles: List[Projectile] = List()

  override def update(delta: Float): Unit = {
    super.update(delta)
    projectiles = projectiles.filter(_.isActive)
    projectiles.foreach(_.update(delta))
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    projectiles.foreach(_.render(shapeRenderer))
  }

  def attack(source: Vector2, target: Vector2, enemy: Troop): Unit = {
    if (canAttack) {
      val direction = new Vector2(target).sub(source).nor()
      val newProjectile = new Projectile(
        new Vector2(source),
        direction,
        owner.stats.projectileSpeed.getOrElse(300f),
        owner.stats.projectileSize.getOrElse(5f),
        owner.stats.damage,
        isFromEnemy = owner.isEnemy
      )
      GameState.addProjectile(newProjectile)
      attackTimer = 1.0f / owner.stats.attackSpeed
    }
  }
}

class PassiveWeapon(var owner: Troop) extends Weapon {
  override def render(shapeRenderer: ShapeRenderer): Unit = {}

  override def attack(source: Vector2, target: Vector2, enemy: Troop): Unit = {}
}

class Projectile(
                  var position: Vector2,
                  val direction: Vector2,
                  val speed: Float,
                  val size: Float,
                  val damage: Float,
                  val maxDistance: Float = 1000f,
                  val isFromEnemy: Boolean = false
                ) {
  private val startPosition = new Vector2(position)
  var isActive: Boolean = true

  def update(delta: Float): Unit = {
    position.x += direction.x * speed * delta
    position.y += direction.y * speed * delta

    if (position.dst(startPosition) > maxDistance) {
      isActive = false
    }
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(Color.YELLOW)
    shapeRenderer.circle(position.x, position.y, size)
  }
}