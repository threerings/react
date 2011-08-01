name := "react"

version := "1.0-SNAPSHOT"

organization := "com.threerings"

crossPaths := false

javacOptions ++= Seq("-Xlint", "-Xlint:-serial")

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.+" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default"
)
