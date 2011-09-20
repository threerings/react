name := "react"

version := "1.1-SNAPSHOT"

organization := "com.threerings"

crossPaths := false

javacOptions ++= Seq(
  "-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"
)

autoScalaLibrary := false // no scala-library dependency

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.+" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default"
)

// add our sources to the main jar file
unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java"

// work around SBT bug
unmanagedResources in Compile ~= (_.filterNot(_.isDirectory))
