package game.model

/** Position on the discrete 10x10 grid. */
case class Position(x: Int, y: Int) {
  require(x >= 0 && y >= 0, "Positions must be non-negative")
}

sealed trait BuildingType { def name: String }
case object Barracks extends BuildingType { val name: String = "Barracks" }
case object Factory extends BuildingType { val name: String = "Factory" }
case object HQ extends BuildingType { val name: String = "HeadQuarters" }

sealed trait Entity { def position: Position }
case class Troop(position: Position) extends Entity
case class Enemy(position: Position) extends Entity

/** Base representation for a building footprint. Size must be a multiple of 10. */
case class Building(position: Position, size: Int, buildingType: BuildingType) extends Entity {
  require(size % 10 == 0, s"Building size must be in the 10s, received $size")
}

case class HeadQuarters(position: Position, size: Int) extends Entity {
  require(size % 10 == 0, s"HQ size must be in the 10s, received $size")
  val buildingType: BuildingType = HQ
}

/** Grid definition that enforces a 10x10 playable space and supports marking no-go zones. */
case class Grid(width: Int = 10, height: Int = 10) {
  private val blocked: Array[Array[Boolean]] = Array.fill(height, width)(false)

  def inBounds(pos: Position): Boolean = pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height

  def isWalkable(pos: Position): Boolean = inBounds(pos) && !blocked(pos.y)(pos.x)

  /**
   * Marks a no-go zone around a building using its footprint plus one tile in each direction.
   * HQs remain accessible for enemy pathfinding, while other buildings are treated as obstacles.
   */
  def addNoGoZone(building: Building): Unit = {
    val radius = building.size / 10
    val startX = building.position.x - 1
    val startY = building.position.y - 1
    val endX = building.position.x + radius
    val endY = building.position.y + radius

    for {
      y <- startY to endY
      x <- startX to endX
      pos = Position(x, y)
      if inBounds(pos)
    } {
      blocked(pos.y)(pos.x) = true
    }
  }
}

/** A* pathfinder operating on the discrete grid. */
class AStarPathfinder(grid: Grid) {
  private case class Node(position: Position, g: Int, h: Int, parent: Option[Node]) {
    val f: Int = g + h
  }

  private val neighbors = List((1, 0), (-1, 0), (0, 1), (0, -1))

  def findPath(start: Position, goal: Position, allowHq: Boolean = false): Option[List[Position]] = {
    if (!grid.inBounds(start) || !grid.inBounds(goal)) return None

    implicit val ordering: Ordering[Node] = Ordering.by[Node, Int](-_.f)
    val openSet = scala.collection.mutable.PriorityQueue.empty[Node]
    val closed = scala.collection.mutable.Set.empty[Position]

    openSet.enqueue(Node(start, g = 0, h = heuristic(start, goal), parent = None))

    while (openSet.nonEmpty) {
      val current = openSet.dequeue()
      if (current.position == goal) return Some(reconstruct(current))

      closed += current.position

      neighbors.foreach { case (dx, dy) =>
        val neighborPos = Position(current.position.x + dx, current.position.y + dy)
        val walkable =
          if (allowHq && neighborPos == goal) true else grid.isWalkable(neighborPos)

        if (walkable && !closed.contains(neighborPos)) {
          val tentativeG = current.g + 1
          val node = Node(neighborPos, tentativeG, heuristic(neighborPos, goal), Some(current))
          openSet.enqueue(node)
        }
      }
    }

    None
  }

  private def heuristic(a: Position, b: Position): Int = math.abs(a.x - b.x) + math.abs(a.y - b.y)

  private def reconstruct(node: Node): List[Position] = {
    def loop(n: Node, acc: List[Position]): List[Position] = n.parent match {
      case Some(parent) => loop(parent, n.position :: acc)
      case None         => n.position :: acc
    }
    loop(node, Nil)
  }
}
