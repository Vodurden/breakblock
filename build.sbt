name := "breakblock"

version := "0.1"

scalaVersion in ThisBuild := "2.12.1"

val os = sys.props("os.name").toLowerCase
val lwjglNativesClasifier = os match {
  case os if os.startsWith("windows") => "natives-windows"
  case os if os.startsWith("linux") => "natives-linux"
}

fork in run := true

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "io.monix" %% "monix" % "2.3.0",

  "org.lwjgl" % "lwjgl" % "3.1.1",
  "org.lwjgl" % "lwjgl-glfw" % "3.1.1",
  "org.lwjgl" % "lwjgl-jemalloc" % "3.1.1",
  "org.lwjgl" % "lwjgl-openal" % "3.1.1",
  "org.lwjgl" % "lwjgl-opengl" % "3.1.1",
  "org.lwjgl" % "lwjgl-stb" % "3.1.1",

  "org.lwjgl" % "lwjgl" % "3.1.1" classifier lwjglNativesClasifier,
  "org.lwjgl" % "lwjgl-glfw" % "3.1.1" classifier lwjglNativesClasifier,
  "org.lwjgl" % "lwjgl-jemalloc" % "3.1.1" classifier lwjglNativesClasifier,
  "org.lwjgl" % "lwjgl-openal" % "3.1.1" classifier lwjglNativesClasifier,
  "org.lwjgl" % "lwjgl-opengl" % "3.1.1" classifier lwjglNativesClasifier,
  "org.lwjgl" % "lwjgl-stb" % "3.1.1" classifier lwjglNativesClasifier
)
