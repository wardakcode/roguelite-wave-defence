package game.entities.troops

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import game.systems.{MeleeWeapon, RangedWeapon, Stats, Weapon}
import game.core.{GameEngine, GameState}

class EnemyTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 15f
  var color: Color = Color.BLACK
  var weapon: Weapon = new MeleeWeapon(this)

  override def update(delta: Float): Unit = {
    // Move in a simple pattern - left and right
    position.y += stats.movementSpeed * math.sin(Gdx.graphics.getFrameId * 0.0012f).toFloat * delta
    position.x += stats.movementSpeed * math.sin(Gdx.graphics.getFrameId * 0.0002f).toFloat * delta
  }


}

class MeleeTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 10f
  var color: Color = Color.GOLD  // Melee troops will be red
  var weapon: Weapon = new MeleeWeapon(this)
}

class ArcherTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forRanged
  var radius: Float = 8f
  var color: Color = Color.BLUE
  var weapon: Weapon = new RangedWeapon(this)

}

class SpearTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 12f
  var color: Color = Color.GREEN
  var weapon: Weapon = new MeleeWeapon(this)

}

class NinjaTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 7f
  var color: Color = Color.CYAN
  var weapon: Weapon = new MeleeWeapon(this)

}
