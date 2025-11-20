package game.entities.buildings

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

trait Building {
  var position: Vector2
  var width: Float
  var height: Float
  var color: Color

  def isHQ: Boolean = false

  def render(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(color)
    shapeRenderer.rect(position.x, position.y, width, height)
  }

  // Method to apply building's effect to nearby troops
  def applyEffect(): Unit

  def closestPoint(point: Vector2): Vector2 = {
    val clampedX = math.max(position.x, math.min(point.x, position.x + width))
    val clampedY = math.max(position.y, math.min(point.y, position.y + height))
    new Vector2(clampedX.toFloat, clampedY.toFloat)
  }

  def collisionBounds(margin: Float): (Float, Float, Float, Float) = {
    val minX = position.x - margin
    val minY = position.y - margin
    val maxX = position.x + width + margin
    val maxY = position.y + height + margin
    (minX, minY, maxX, maxY)
  }

  def bufferMargin(baseBuffer: Float): Float = if (isHQ) 0f else baseBuffer

  def gridBlockMargin(cellSize: Float, baseBuffer: Float): Float =
    if (isHQ) 0f else baseBuffer + cellSize

  def resolveCollision(point: Vector2, radius: Float, buffer: Float): Vector2 = {
    val margin = radius + buffer
    val (minX, minY, maxX, maxY) = collisionBounds(margin)

    if (point.x < minX || point.x > maxX || point.y < minY || point.y > maxY) {
      return point
    }

    val leftPenetration = point.x - minX
    val rightPenetration = maxX - point.x
    val bottomPenetration = point.y - minY
    val topPenetration = maxY - point.y

    val (axis, _) = List(
      ("left", leftPenetration),
      ("right", rightPenetration),
      ("bottom", bottomPenetration),
      ("top", topPenetration)
    ).minBy(_._2)

    val resolved = new Vector2(point)
    val epsilon = 0.5f

    axis match {
      case "left"   => resolved.x = minX - epsilon
      case "right"  => resolved.x = maxX + epsilon
      case "bottom" => resolved.y = minY - epsilon
      case "top"    => resolved.y = maxY + epsilon
    }

    resolved
  }

  def renderCollisionBounds(shapeRenderer: ShapeRenderer, buffer: Float): Unit = {
    val (minX, minY, maxX, maxY) = collisionBounds(buffer)
    val widthWithBuffer = maxX - minX
    val heightWithBuffer = maxY - minY
    shapeRenderer.rect(minX, minY, widthWithBuffer, heightWithBuffer)
  }
}
