name := "react"

version := "1.0-SNAPSHOT"

organization := "com.threerings"

crossPaths := false

autoScalaLibrary := false

javacOptions ++= Seq("-Xlint", "-Xlint:-serial")

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.+" % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default"
)

// this hackery causes publish-local to install to ~/.m2/repository instead of ~/.ivy
otherResolvers := Seq(Resolver.file("dotM2", file(Path.userHome + "/.m2/repository")))

publishLocalConfiguration <<= (packagedArtifacts, deliverLocal, ivyLoggingLevel) map {
  (arts, _, level) => new PublishConfiguration(None, "dotM2", arts, level)
}
