package game.entities.troops

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import game.systems.{MeleeWeapon, PassiveWeapon, RangedWeapon, Stats, Weapon}
import game.core.{GameEngine, GameState}

class EnemyTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 15f
  var color: Color = Color.BLACK
  var weapon: Weapon = new MeleeWeapon(this)
  val isEnemy: Boolean = true

  protected def findTargets: List[Troop] = GameState.playerTroops
}

class MeleeTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 10f
  var color: Color = Color.GOLD  // Melee troops will be red
  var weapon: Weapon = new MeleeWeapon(this)
  val isEnemy: Boolean = false

  protected def findTargets: List[Troop] = GameState.enemies
}

class ArcherTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forRanged
  var radius: Float = 8f
  var color: Color = Color.BLUE
  var weapon: Weapon = new RangedWeapon(this)
  val isEnemy: Boolean = false

  protected def findTargets: List[Troop] = GameState.enemies

}

class SpearTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 12f
  var color: Color = Color.GREEN
  var weapon: Weapon = new MeleeWeapon(this)
  val isEnemy: Boolean = false

  protected def findTargets: List[Troop] = GameState.enemies

}

class NinjaTroop(startPos: Vector2) extends Troop {
  var position: Vector2 = startPos
  var stats: Stats = Stats.forMelee
  var radius: Float = 7f
  var color: Color = Color.CYAN
  var weapon: Weapon = new MeleeWeapon(this)
  val isEnemy: Boolean = false

  protected def findTargets: List[Troop] = GameState.enemies

}

class HQTroop extends Troop {
  var position: Vector2 = new Vector2(130, 390)
  var stats: Stats = Stats.forHQ
  var radius: Float = 28f
  var color: Color = Color.WHITE
  var weapon: Weapon = new PassiveWeapon(this)
  val isEnemy: Boolean = false

  override protected def updateMovement(delta: Float): Unit = ()
  override protected def updateTarget(): Unit = ()
  override protected def updateCombat(delta: Float): Unit = ()

  protected def findTargets: List[Troop] = GameState.enemies
}
