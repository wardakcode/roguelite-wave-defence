name := "roguelite-wave-defence"
version := "0.1"
scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx" % "1.14.0",
  "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % "1.14.0",
  "com.badlogicgames.gdx" % "gdx-platform" % "1.14.0" classifier "natives-desktop",
  "com.badlogicgames.gdx" % "gdx-box2d" % "3.1.1-0",
  "com.badlogicgames.gdx" % "gdx-box2d-platform" % "3.1.1-0" classifier "natives-desktop",
  "org.typelevel" %% "cats-effect" % "3.6.3",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)