import Dependencies._

// ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "uk.sky"
ThisBuild / organizationName := "sky"

val http4sVersion = "0.23.27"
val jsoniterVersion = "2.20.0"
val catsEffectVersion = "3.4.8"
val fs2Version = "3.6.1"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

lazy val core = (project in file("core"))
  .settings(
    name := "Core Project",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "co.fs2" %% "fs2-core" % "3.10.2",
      "co.fs2" %% "fs2-io" % "3.10.2",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      // http4s
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.datastax.oss" % "java-driver-core" % "4.17.0",
      "com.datastax.oss" % "java-driver-query-builder" % "4.17.0",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      munit % Test,
    //   "com.banno" % "kafka4s_2.13" % "6.0.1",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.13",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.13" % "provided",
      "org.xerial" % "sqlite-jdbc" % "3.46.0.0"
    )
  )

lazy val root = (project in file("."))
  .aggregate(core) // Include the core project in the root project
  .settings(
    name := "Cassandra Best Practise",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "com.datastax.oss" % "java-driver-core" % "4.17.0",
      "com.datastax.oss" % "java-driver-query-builder" % "4.17.0",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "co.fs2" %% "fs2-core" % "3.10.2",
      "co.fs2" %% "fs2-io" % "3.10.2",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      munit % Test,
      "com.banno" % "kafka4s_2.13" % "6.0.1",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.13",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.13" % "provided",
      "org.xerial" % "sqlite-jdbc" % "3.46.0.0"
    )
  )
