import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._

name := "gnip-consumer"

version := "1.1"

organization := "com.oxyme"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.2",
  "org.parboiled" % "parboiled-core" % "1.1.7",
  "net.fehmicansaglam" %% "tepkin" % "0.5",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

scalariformSettings

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("spring.tooling") => MergeStrategy.last
  case "application.conf"                => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

fork in run := true
