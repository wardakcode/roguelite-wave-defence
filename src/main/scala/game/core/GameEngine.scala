package game.core

import com.badlogic.gdx.{Game, Gdx, Graphics}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.Input.Keys
import game.entities.buildings._
import game.entities.troops._
import game.systems.Projectile

object GameState {
  var buildings: List[Building] = List()
  var playerTroops: List[Troop] = List()
  var enemies: List[EnemyTroop] = List()
  var hq: Option[HQTroop] = None
  private var projectiles: List[Projectile] = List()

  sealed trait Phase
  case object StartMenu extends Phase
  case object Playing extends Phase
  case object Shop extends Phase
  case object GameOver extends Phase

  val maxRounds = 5
  val goldPerKill = 2
  private val meleeCost = 10

  var phase: Phase = StartMenu
  var gold: Int = 0
  var round: Int = 0
  private var roundActive: Boolean = false
  var resultMessage: Option[String] = None

  def resetGame(): Unit = {
    buildings = List(
      new HQ(),
      new Barracks(),
      new Tavern(),
      new Wall()
    )

    val newHq = new HQTroop()
    hq = Some(newHq)

    playerTroops = spawnInitialTroops(newHq)
    enemies = List()
    projectiles = List()
    gold = 0
    round = 0
    roundActive = false
    resultMessage = None
    phase = StartMenu
  }

  def update(delta: Float): Unit = {
    if (phase != Playing) return

    // Update all entities
    playerTroops.foreach(_.update(delta))
    enemies.foreach(_.update(delta))

    // Update and filter active projectiles
    projectiles = projectiles.filter(_.isActive)
    projectiles.foreach(_.update(delta))

    // Check projectile collisions
    checkProjectileCollisions()

    // Remove dead enemies and award gold
    val (aliveEnemies, defeatedEnemies) = enemies.partition(!_.stats.isDead)
    enemies = aliveEnemies
    if (defeatedEnemies.nonEmpty) {
      gold += defeatedEnemies.size * goldPerKill
    }

    if (hq.exists(_.stats.isDead)) {
      phase = GameOver
      resultMessage = Some("The HQ was destroyed.")
    } else if (roundActive && enemies.isEmpty) {
      roundActive = false
      if (round >= maxRounds) {
        phase = GameOver
        resultMessage = Some("All waves cleared!")
      } else {
        phase = Shop
      }
    }
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    buildings.foreach(_.render(shapeRenderer))
    playerTroops.foreach(_.render(shapeRenderer))
    enemies.foreach(_.render(shapeRenderer))
    projectiles.foreach(_.render(shapeRenderer))
  }

  def addProjectile(projectile: Projectile): Unit = {
    projectiles = projectile :: projectiles
  }

  def startRound(): Unit = {
    if (phase == GameOver) return
    round += 1
    spawnEnemiesForRound()
    roundActive = true
    phase = Playing
  }

  def buyMeleeTroop(): Unit = {
    if (gold >= meleeCost) {
      val troop = new MeleeTroop(new Vector2(140 + scala.util.Random.nextInt(40), 330 + scala.util.Random.nextInt(60)))
      playerTroops = troop :: playerTroops
      gold -= meleeCost
    }
  }

  private def spawnInitialTroops(newHq: HQTroop): List[Troop] = {
    val basePositions = List(
      new Vector2(170, 330),
      new Vector2(170, 360),
      new Vector2(170, 390),
      new Vector2(200, 345),
      new Vector2(200, 375)
    )
    val guards = basePositions.map(pos => new MeleeTroop(pos))
    newHq :: guards
  }

  private def spawnEnemiesForRound(): Unit = {
    val count = 7 + (round - 1) * 2
    val toughnessMultiplier = 1f + (round - 1) * 0.2f
    val spawnDistance = 800f + round * 30f
    val hqPosition = hq.map(_.position).getOrElse(new Vector2(0, 0))

    enemies = (0 until count).toList.map { i =>
      val side = i % 4
      val offset = scala.util.Random.nextFloat() * 300f - 150f
      val position = side match {
        case 0 => new Vector2(hqPosition.x - spawnDistance, hqPosition.y + offset)
        case 1 => new Vector2(hqPosition.x + spawnDistance, hqPosition.y + offset)
        case 2 => new Vector2(hqPosition.x + offset, hqPosition.y - spawnDistance)
        case _ => new Vector2(hqPosition.x + offset, hqPosition.y + spawnDistance)
      }
      val enemy = new EnemyTroop(position)
      val boostedHp = enemy.stats.maxHp * toughnessMultiplier
      enemy.stats = enemy.stats.copy(
        damage = enemy.stats.damage * toughnessMultiplier,
        hp = boostedHp,
        maxHp = boostedHp
      )
      enemy
    }
  }

  private def checkProjectileCollisions(): Unit = {
    projectiles.foreach { projectile =>
      val targets = if (projectile.isFromEnemy) playerTroops else enemies

      targets.find { target =>
        !target.stats.isDead &&
          target.position.dst(projectile.position) < target.radius + projectile.size
      }.foreach { target =>
        target.stats = target.stats.takeDamage(projectile.damage)
        projectile.isActive = false
      }
    }
  }
}

class GameEngine extends Game {
  var batch: SpriteBatch = _
  var camera: GameCamera = _
  var shapeRenderer: ShapeRenderer = _
  var uiCamera: OrthographicCamera = _
  var font: BitmapFont = _

  override def create(): Unit = {
    batch = new SpriteBatch
    camera = new GameCamera
    camera.setToOrtho(false, 1280, 720)
    camera.zoom = 2f  // Start more zoomed out

    shapeRenderer = new ShapeRenderer()
    uiCamera = new OrthographicCamera()
    uiCamera.setToOrtho(false, Gdx.graphics.getWidth, Gdx.graphics.getHeight)

    font = new BitmapFont()
    font.setUseIntegerPositions(false)

    GameState.resetGame()
  }

  override def render(): Unit = {
    // Clear screen
    Gdx.gl.glClearColor(0, 0, 0.08f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val delta = Gdx.graphics.getDeltaTime

    handleInput()

    // Update camera
    camera.update(delta)
    uiCamera.update()

    // Update game state
    GameState.update(delta)

    // Render
    shapeRenderer.setProjectionMatrix(camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    GameState.render(shapeRenderer)
    shapeRenderer.end()

    drawUI()
  }

  override def dispose(): Unit = {
    batch.dispose()
    shapeRenderer.dispose()
    font.dispose()
  }

  private def handleInput(): Unit = {
    GameState.phase match {
      case GameState.StartMenu =>
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
          GameState.startRound()
        }
      case GameState.Playing =>
        // No menu interactions while actively defending
      case GameState.Shop =>
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1) || Gdx.input.isKeyJustPressed(Keys.NUM_5)) {
          GameState.buyMeleeTroop()
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
          GameState.startRound()
        }
      case GameState.GameOver =>
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
          GameState.resetGame()
        }
    }
  }

  private def drawUI(): Unit = {
    // UI background panels
    shapeRenderer.setProjectionMatrix(uiCamera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(0f, 0f, 0f, 0.5f)
    shapeRenderer.rect(10, Gdx.graphics.getHeight - 60, 340, 50)

    if (GameState.phase == GameState.StartMenu || GameState.phase == GameState.GameOver) {
      shapeRenderer.rect(Gdx.graphics.getWidth / 2f - 120, Gdx.graphics.getHeight / 2f - 40, 240, 80)
    }

    if (GameState.phase == GameState.Shop) {
      shapeRenderer.rect(Gdx.graphics.getWidth / 2f - 200, Gdx.graphics.getHeight / 2f - 70, 400, 140)
    }

    shapeRenderer.end()

    // UI text
    batch.setProjectionMatrix(uiCamera.combined)
    batch.begin()
    val baseText = s"Round ${GameState.round}/${GameState.maxRounds} | Gold: ${GameState.gold} | Enemies: ${GameState.enemies.size}"
    font.draw(batch, baseText, 20, Gdx.graphics.getHeight - 25)

    GameState.phase match {
      case GameState.StartMenu =>
        font.draw(batch, "Defend the HQ! Press ENTER to start.", Gdx.graphics.getWidth / 2f - 110, Gdx.graphics.getHeight / 2f + 10)
        font.draw(batch, "You begin with 5 melee guards.", Gdx.graphics.getWidth / 2f - 100, Gdx.graphics.getHeight / 2f - 15)
      case GameState.Shop =>
        font.draw(batch, "Shop: Press 1 to recruit a melee troop (10 gold).", Gdx.graphics.getWidth / 2f - 180, Gdx.graphics.getHeight / 2f + 20)
        font.draw(batch, "Press ENTER to begin the next wave.", Gdx.graphics.getWidth / 2f - 130, Gdx.graphics.getHeight / 2f - 5)
      case GameState.GameOver =>
        val result = GameState.resultMessage.getOrElse("Battle over.")
        font.draw(batch, s"$result Press R to restart.", Gdx.graphics.getWidth / 2f - 140, Gdx.graphics.getHeight / 2f + 5)
      case GameState.Playing =>
        font.draw(batch, "Enemies aim for the HQ – hold the line!", 20, Gdx.graphics.getHeight - 45)
    }
    batch.end()
  }
}
