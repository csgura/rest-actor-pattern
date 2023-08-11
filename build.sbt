name := "rest-actor-pattern"

version := "0.1"


lazy val root = (project in file(".")).enablePlugins(PlayScala).
  settings(
    scalaSource in Compile := baseDirectory(_ / "src/main/scala").value ,
  )

scalaVersion := "2.13.11"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test


