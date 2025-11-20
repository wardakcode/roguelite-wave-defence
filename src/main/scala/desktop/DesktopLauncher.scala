package desktop

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import game.core.GameEngine

object DesktopLauncher extends App{
  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle("Your Game")
  config.setWindowedMode(1280, 720)  // Initial window size
  config.useVsync(true)

  new Lwjgl3Application(new GameEngine, config)
}
