package game.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Input.Keys

class GameCamera extends OrthographicCamera {
  private val ZOOM_SPEED = 0.1f
  private val MIN_ZOOM = 0.5f
  private val MAX_ZOOM = 3f
  private val PAN_SPEED = 500f

  def update(delta: Float): Unit = {
    // Handle zoom
    if (Gdx.input.isKeyPressed(Keys.Q) && zoom < 3f) {
      zoom = math.min(MAX_ZOOM, zoom + ZOOM_SPEED)
    }
    if (Gdx.input.isKeyPressed(Keys.E)) {
      zoom = math.max(MIN_ZOOM, zoom - ZOOM_SPEED)
    }

    // Handle pan
    val panVector = new Vector3(0, 0, 0)
    if (Gdx.input.isKeyPressed(Keys.W)) panVector.y += PAN_SPEED * delta
    if (Gdx.input.isKeyPressed(Keys.S)) panVector.y -= PAN_SPEED * delta
    if (Gdx.input.isKeyPressed(Keys.A)) panVector.x -= PAN_SPEED * delta
    if (Gdx.input.isKeyPressed(Keys.D)) panVector.x += PAN_SPEED * delta

    translate(panVector)

    // Update camera matrices
    super.update()
  }
}