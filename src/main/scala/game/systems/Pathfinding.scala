package game.systems

import com.badlogic.gdx.math.Vector2
import game.entities.buildings.Building
import scala.collection.mutable

case class GridNode(x: Int, y: Int) {
  def toWorldPosition(gridSize: Float): Vector2 =
    new Vector2(x * gridSize, y * gridSize)

  def neighbors: List[GridNode] = List(
    GridNode(x + 1, y),
    GridNode(x - 1, y),
    GridNode(x, y + 1),
    GridNode(x, y - 1),
    GridNode(x + 1, y + 1),
    GridNode(x - 1, y + 1),
    GridNode(x + 1, y - 1),
    GridNode(x - 1, y - 1)
  )

  def distanceTo(other: GridNode): Float = {
    val dx = math.abs(x - other.x)
    val dy = math.abs(y - other.y)
    // Diagonal distance heuristic
    math.sqrt(dx * dx + dy * dy).toFloat
  }
}

class PathfindingGrid(buildings: List[Building], val gridSize: Float = 10f) {
  private val minX = -960
  private val minY = -960
  private val maxX = 2240
  private val maxY = 2240

  def worldToGrid(pos: Vector2): GridNode = {
    GridNode(
      (pos.x / gridSize).toInt,
      (pos.y / gridSize).toInt
    )
  }

  def isWalkable(node: GridNode): Boolean = {
    val worldPos = node.toWorldPosition(gridSize)

    // Check world bounds
    if (worldPos.x < minX || worldPos.x > maxX ||
      worldPos.y < minY || worldPos.y > maxY) {
      return false
    }

    // Check collision with buildings
    !buildings.exists { building =>
      val margin = 5f // Small margin around buildings
      worldPos.x >= building.position.x - margin &&
        worldPos.x <= building.position.x + building.width + margin &&
        worldPos.y >= building.position.y - margin &&
        worldPos.y <= building.position.y + building.height + margin
    }
  }

  def findPath(start: Vector2, end: Vector2): Option[List[Vector2]] = {
    val startNode = worldToGrid(start)
    val endNode = worldToGrid(end)

    if (!isWalkable(endNode)) {
      // If target is not walkable, find nearest walkable node
      return findPathToNearestWalkable(start, end)
    }

    val openSet = mutable.PriorityQueue.empty[(GridNode, Float)](
      Ordering.by[(GridNode, Float), Float](-_._2)
    )
    val cameFrom = mutable.Map[GridNode, GridNode]()
    val gScore = mutable.Map[GridNode, Float]().withDefaultValue(Float.MaxValue)
    val fScore = mutable.Map[GridNode, Float]().withDefaultValue(Float.MaxValue)

    gScore(startNode) = 0
    fScore(startNode) = startNode.distanceTo(endNode)
    openSet.enqueue((startNode, fScore(startNode)))

    val closedSet = mutable.Set[GridNode]()
    var iterations = 0
    val maxIterations = 1000 // Prevent infinite loops

    while (openSet.nonEmpty && iterations < maxIterations) {
      iterations += 1
      val (current, _) = openSet.dequeue()

      if (current == endNode) {
        return Some(reconstructPath(cameFrom, current))
      }

      closedSet += current

      current.neighbors.foreach { neighbor =>
        if (!closedSet.contains(neighbor) && isWalkable(neighbor)) {
          val isDiagonal = neighbor.x != current.x && neighbor.y != current.y
          val moveCost = if (isDiagonal) 1.414f else 1f
          val tentativeGScore = gScore(current) + moveCost

          if (tentativeGScore < gScore(neighbor)) {
            cameFrom(neighbor) = current
            gScore(neighbor) = tentativeGScore
            fScore(neighbor) = gScore(neighbor) + neighbor.distanceTo(endNode)

            if (!openSet.exists(_._1 == neighbor)) {
              openSet.enqueue((neighbor, fScore(neighbor)))
            }
          }
        }
      }
    }

    None // No path found
  }

  private def findPathToNearestWalkable(start: Vector2, end: Vector2): Option[List[Vector2]] = {
    val endNode = worldToGrid(end)
    var radius = 1
    val maxRadius = 10

    while (radius <= maxRadius) {
      val candidates = for {
        dx <- -radius to radius
        dy <- -radius to radius
        if math.abs(dx) == radius || math.abs(dy) == radius
        node = GridNode(endNode.x + dx, endNode.y + dy)
        if isWalkable(node)
      } yield node

      if (candidates.nonEmpty) {
        val nearest = candidates.minBy(_.distanceTo(worldToGrid(start)))
        return findPath(start, nearest.toWorldPosition(gridSize))
      }

      radius += 1
    }

    None
  }

  private def reconstructPath(cameFrom: mutable.Map[GridNode, GridNode], current: GridNode): List[Vector2] = {
    var path = List(current.toWorldPosition(gridSize))
    var node = current

    while (cameFrom.contains(node)) {
      node = cameFrom(node)
      path = node.toWorldPosition(gridSize) :: path
    }

    // Smooth the path by skipping unnecessary waypoints
    smoothPath(path)
  }

  private def smoothPath(path: List[Vector2]): List[Vector2] = {
    if (path.length <= 2) return path

    var smoothed = List(path.head)
    var current = 0

    while (current < path.length - 1) {
      var farthest = current + 1

      // Try to skip as many waypoints as possible
      for (i <- (current + 2) until path.length) {
        if (hasLineOfSight(path(current), path(i))) {
          farthest = i
        }
      }

      smoothed = path(farthest) :: smoothed
      current = farthest
    }

    smoothed.reverse
  }

  private def hasLineOfSight(from: Vector2, to: Vector2): Boolean = {
    val steps = 10
    for (i <- 0 to steps) {
      val t = i.toFloat / steps
      val point = new Vector2(
        from.x + (to.x - from.x) * t,
        from.y + (to.y - from.y) * t
      )
      if (!isWalkable(worldToGrid(point))) {
        return false
      }
    }
    true
  }
}