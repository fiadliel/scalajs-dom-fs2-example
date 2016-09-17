enablePlugins(ScalaJSPlugin)

name := "scalajs-dom-fs2-example"

scalaVersion := "2.11.8"

scalaJSUseRhino in Global := false

libraryDependencies ++= Seq(
  "co.fs2" %%% "fs2-core" % "0.9.1",
  "org.scala-js" %%% "scalajs-dom" % "0.9.0"
)
