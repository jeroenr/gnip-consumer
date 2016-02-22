import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._

name := "gnip-consumer"

version := "0.1"

organization := "com.github"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2",
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

fork in run := true
