package game.core

import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import game.model._

/**
 * Basic game engine scaffold that manages a 10x10 grid world and resolves navigation
 * for troops and enemies using A* pathfinding.
 */
class GameEngine extends ApplicationAdapter {
  private val worldSize = 10
  private val grid: Grid = Grid(width = worldSize, height = worldSize)
  private val pathfinder = new AStarPathfinder(grid)

  private var hq: HeadQuarters = _
  private var buildings: List[Building] = Nil
  private var troops: List[Troop] = Nil
  private var enemies: List[Enemy] = Nil

  override def create(): Unit = {
    // Initialize HQ at the center of the grid.
    hq = HeadQuarters(Position(worldSize / 2, worldSize / 2), size = 20)

    // Example world layout. Building sizes must be multiples of 10.
    buildings = List(
      Building(Position(1, 1), size = 10, buildingType = Barracks),
      Building(Position(7, 1), size = 20, buildingType = Factory)
    )

    // Apply no-go zones for all buildings except the HQ so that pathfinding
    // respects restricted construction areas.
    buildings.foreach(b => grid.addNoGoZone(b))

    troops = List(Troop(Position(0, 0)), Troop(Position(0, 9)))
    enemies = List(Enemy(Position(9, 0)), Enemy(Position(9, 9)))

    resolvePaths()
  }

  /**
   * Computes navigation paths for troops (toward the nearest enemy) and enemies
   * (toward the HQ). Paths are recalculated whenever create() is called, but could be
   * reused in an update loop in a more complete implementation.
   */
  private def resolvePaths(): Unit = {
    troops.foreach { troop =>
      nearestEnemy(troop.position).foreach { target =>
        pathfinder.findPath(troop.position, target.position).foreach { path =>
          Gdx.app.log("PATH", s"Troop at ${troop.position} -> enemy at ${target.position}: ${path.mkString(" -> ")}")
        }
      }
    }

    enemies.foreach { enemy =>
      pathfinder.findPath(enemy.position, hq.position, allowHq = true).foreach { path =>
        Gdx.app.log("PATH", s"Enemy at ${enemy.position} -> HQ at ${hq.position}: ${path.mkString(" -> ")}")
      }
    }
  }

  private def nearestEnemy(from: Position): Option[Enemy] = {
    enemies.sortBy(e => manhattanDistance(from, e.position)).headOption
  }

  private def manhattanDistance(a: Position, b: Position): Int =
    math.abs(a.x - b.x) + math.abs(a.y - b.y)
}
