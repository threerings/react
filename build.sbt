name := "react"

version := "1.2-SNAPSHOT"

organization := "com.threerings"

crossPaths := false

scalaVersion := "2.9.1"

javacOptions ++= Seq(
  "-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"
)

autoScalaLibrary := false // no scala-library dependency

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.+" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default"
)

// filter the super-source directory from the build
unmanagedSources in Compile ~= (_.filterNot(_.getPath.indexOf("react/super") != -1))

// add our sources to the main jar file
unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java"
