package game.core

import com.badlogic.gdx.{Game, Gdx, Graphics}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import game.entities.buildings._
import game.entities.troops._
import game.systems.Projectile

object GameState {
  var playerTroops: List[Troop] = List()
  var enemies: List[EnemyTroop] = List()
  private var projectiles: List[Projectile] = List()

  def update(delta: Float): Unit = {
    // Update all entities
    playerTroops.foreach(_.update(delta))
    enemies.foreach(_.update(delta))

    // Update and filter active projectiles
    projectiles = projectiles.filter(_.isActive)
    projectiles.foreach(_.update(delta))

    // Check projectile collisions
    checkProjectileCollisions()

    // Remove dead enemies
    enemies = enemies.filter(!_.stats.isDead)
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    playerTroops.foreach(_.render(shapeRenderer))
    enemies.foreach(_.render(shapeRenderer))
    projectiles.foreach(_.render(shapeRenderer))
  }

  def addProjectile(projectile: Projectile): Unit = {
    projectiles = projectile :: projectiles
  }

  private def checkProjectileCollisions(): Unit = {
    projectiles.foreach { projectile =>
      // Check collisions with enemies
      enemies.find { enemy =>
        !enemy.stats.isDead &&
          enemy.position.dst(projectile.position) < enemy.radius + projectile.size
      }.foreach { enemy =>
        // Apply damage and deactivate projectile
        enemy.stats = enemy.stats.takeDamage(projectile.damage)
        projectile.isActive = false
      }
    }
  }
}

class GameEngine extends Game {
  var batch: SpriteBatch = _
  var camera: GameCamera = _
  var shapeRenderer: ShapeRenderer = _

  override def create(): Unit = {
    batch = new SpriteBatch
    camera = new GameCamera
    camera.setToOrtho(false, 1280, 720)
    camera.zoom = 2f  // Start more zoomed out

    shapeRenderer = new ShapeRenderer()

    // Initialize with some test troops
    GameState.enemies = List(
      new EnemyTroop(new Vector2(500, 300)),
      new EnemyTroop(new Vector2(400, 400))
    )

    GameState.playerTroops = List(
      new ArcherTroop(new Vector2(100, 200)),
      new MeleeTroop(new Vector2(150, 150)),
      new NinjaTroop(new Vector2(150, 100))
    )
  }

  override def render(): Unit = {
    // Clear screen
    Gdx.gl.glClearColor(0, 0, 0.08f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val delta = Gdx.graphics.getDeltaTime

    // Update camera
    camera.update(delta)

    // Update game state
    GameState.update(delta)

    // Render
    shapeRenderer.setProjectionMatrix(camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    GameState.render(shapeRenderer)
    shapeRenderer.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
    shapeRenderer.dispose()
  }
}
