val scala3Version = "3.5.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "effects",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies :=
      Seq("org.scalameta" %% "munit" % "0.7.29" % Test) ++ kyo
  )

lazy val kyo = {
  val version = "0.11.1"
  Seq(
libraryDependencies += "io.getkyo" %% "kyo-prelude"       % version,
libraryDependencies += "io.getkyo" %% "kyo-core"          % version,
libraryDependencies += "io.getkyo" %% "kyo-direct"        % version,
libraryDependencies += "io.getkyo" %% "kyo-combinators"   % version,
libraryDependencies += "io.getkyo" %% "kyo-sttp"          % version,
libraryDependencies += "io.getkyo" %% "kyo-tapir"         % version,
libraryDependencies += "io.getkyo" %% "kyo-zio"           % version,
libraryDependencies += "io.getkyo" %% "kyo-caliban"       % version,
libraryDependencies += "io.getkyo" %% "kyo-cache"         % version,
libraryDependencies += "io.getkyo" %% "kyo-stats-otel"    % version,
libraryDependencies += "io.getkyo" %% "kyo-tag"           % version,
libraryDependencies += "io.getkyo" %% "kyo-data"          % version,
libraryDependencies += "io.getkyo" %% "kyo-scheduler"     % version,
libraryDependencies += "io.getkyo" %% "kyo-scheduler-zio" % version,
  )
}
