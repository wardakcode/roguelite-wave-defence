package game.entities.buildings

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class HQ extends Building {
  var position: Vector2 = new Vector2(100, 360)
  var width: Float = 60f
  var height: Float = 60f
  var color: Color = Color.WHITE
  override val isHQ: Boolean = true

  override def applyEffect(): Unit = {
    // Will add HP boost to nearby troops
  }
}

class Barracks extends Building {
  var position: Vector2 = new Vector2(200, 200)
  var width: Float = 40f
  var height: Float = 40f
  var color: Color = Color.LIGHT_GRAY

  override def applyEffect(): Unit = {
    // Will add damage boost to nearby troops
  }
}

class Tavern extends Building {
  var position: Vector2 = new Vector2(300, 500)
  var width: Float = 30f
  var height: Float = 30f
  var color: Color = Color.BROWN

  override def applyEffect(): Unit = {
    // Will add attack speed boost to nearby troops
  }
}

class Wall extends Building {
  var position: Vector2 = new Vector2(400, 300)
  var width: Float = 10f
  var height: Float = 80f
  var color: Color = Color.GRAY

  override def applyEffect(): Unit = {
    // Walls might not have an effect, or could provide defense bonus
  }
}