import Dependencies._

ThisBuild / scalaVersion := "3.5.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "uk.sky"
ThisBuild / organizationName := "sky"

lazy val root = (project in file("."))
  .settings(
    name := "Cassandra Best Practise",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "com.datastax.oss" % "java-driver-core" % "4.15.0",
      "com.datastax.oss" % "java-driver-query-builder" % "4.15.0",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "co.fs2" %% "fs2-core" % "3.7.0",
      "co.fs2" %% "fs2-io" % "3.7.0",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      munit % Test
    )
  )
