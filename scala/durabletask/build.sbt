val scala3Version = "3.4.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "durableTask",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,

    // Define library dependencies
    libraryDependencies ++= Seq(
      // Testing libraries
      "org.scalatest" %% "scalatest" % "3.3.0-alpha.1" % Test,

      // Database interaction libraries
      "com.lihaoyi" %% "scalasql" % "0.1.4",
      "com.lihaoyi" %% "scalasql-core" % "0.1.4",
      "com.lihaoyi" %% "scalasql-operations" % "0.1.4",
      "com.lihaoyi" %% "scalasql-query" % "0.1.4",
      "org.xerial" % "sqlite-jdbc" % "3.36.0",

      // Functional programming libraries
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.6-e1b1d37",

      // Logging libraries
      "org.slf4j" % "slf4j-api" % "2.1.0-alpha1",
      "ch.qos.logback" % "logback-classic" % "1.5.6"
    )
  )
