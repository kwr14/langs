import sbt._
import sbt.Keys._

// Project metadata
ThisBuild / organization := "com.github.actions"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"

// Compiler options
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-language:higherKinds",
  "-language:implicitConversions"
)

// Dependency versions
lazy val catsEffectVersion = "3.5.4"
lazy val fs2Version = "3.10.2"
lazy val http4sVersion = "0.23.27"
lazy val circeVersion = "0.14.10"
lazy val declineVersion = "2.4.1"
lazy val fansiVersion = "0.5.0"
lazy val scalatestVersion = "3.2.18"
lazy val scalacheckVersion = "1.18.0"

// Common settings
lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test
  ),
  testFrameworks += new TestFramework("org.scalatest.tools.Framework")
)

// Root project
lazy val root = (project in file("."))
  .aggregate(core, apiClient, terminalUi, cli)
  .settings(
    name := "github-actions-cli",
    publish / skip := true
  )

// Core module - domain models and business logic
lazy val core = (project in file("core"))
  .settings(
    name := "github-actions-cli-core",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  )

// API Client module - GitHub API integration
lazy val apiClient = (project in file("api-client"))
  .dependsOn(core)
  .settings(
    name := "github-actions-cli-api-client",
    commonSettings,
    scalacOptions ~= { opts => opts.filterNot(_ == "-Xfatal-warnings") },
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  )

// Terminal UI module - TUI components
lazy val terminalUi = (project in file("terminal-ui"))
  .dependsOn(core, apiClient)
  .settings(
    name := "github-actions-cli-terminal-ui",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "com.lihaoyi" %% "fansi" % fansiVersion
    )
  )

// CLI module - main entry point
lazy val cli = (project in file("cli"))
  .dependsOn(core, apiClient, terminalUi)
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "github-actions-cli",
    commonSettings,
    scalacOptions ~= { opts => opts.filterNot(_ == "-Xfatal-warnings") },
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "com.monovore" %% "decline-effect" % declineVersion
    ),
    // Assembly settings for fat JAR
    assembly / mainClass := Some("com.github.actions.cli.Main"),
    assembly / assemblyJarName := "github-actions-cli.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", "9", "module-info.class") =>
        MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "module-info.class"           => MergeStrategy.discard
      case x                             => MergeStrategy.first
    },
    // Native image settings
    nativeImageOptions ++= Seq(
      "--no-fallback",
      "--initialize-at-build-time",
      "--enable-http",
      "--enable-https",
      "-H:+ReportExceptionStackTraces"
    ),
    nativeImageVersion := "22.3.0"
  )

// Global settings
Global / onChangedBuildSource := ReloadOnSourceChanges
