package game.systems

case class Stats(
                  damage: Float,
                  attackSpeed: Float,
                  hp: Float,
                  maxHp: Float,
                  movementSpeed: Float,
                  armor: Float,
                  attackRange: Float,
                  // For weapons
                  projectileSpeed: Option[Float] = None,  // None for melee weapons
                  projectileSize: Option[Float] = None
                ) {
  def isDead: Boolean = hp <= 0

  def takeDamage(amount: Float): Stats = {
    val damageReduction = armor / 100f  // Simple armor calculation
    val actualDamage = amount * (1 - damageReduction)
    this.copy(hp = math.max(0, hp - actualDamage))
  }
}

object Stats {
  def forMelee: Stats = Stats(
    damage = 15f,
    attackSpeed = 1.0f,
    hp = 100f,
    maxHp = 100f,
    movementSpeed = 75f,
    armor = 20f,
    attackRange = 20f
  )

  def forRanged: Stats = Stats(
    damage = 10f,
    attackSpeed = 0.8f,
    hp = 70f,
    maxHp = 70f,
    movementSpeed = 120f,
    armor = 10f,
    attackRange = 200f,
    projectileSpeed = Some(300f),
    projectileSize = Some(5f)
  )
  // Add more troop type factories as needed

  def forHQ: Stats = Stats(
    damage = 0f,
    attackSpeed = 0f,
    hp = 400f,
    maxHp = 400f,
    movementSpeed = 0f,
    armor = 40f,
    attackRange = 0f
  )
}
