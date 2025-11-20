package game.entities.buildings

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

trait Building {
  var position: Vector2
  var width: Float
  var height: Float
  var color: Color

  def render(shapeRenderer: ShapeRenderer): Unit = {
    shapeRenderer.setColor(color)
    shapeRenderer.rect(position.x, position.y, width, height)
  }

  // Method to apply building's effect to nearby troops
  def applyEffect(): Unit
}
