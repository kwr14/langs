package com.github.actions.cli

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import com.monovore.decline.{Opts, Command => DeclineCommand}
import com.monovore.decline.effect.*

object Main
    extends CommandIOApp(
      name = "gh-actions",
      header = "GitHub Actions Workflow CLI Dashboard",
      version = "0.1.0"
    ):

  override def main: Opts[IO[ExitCode]] =
    CliOpts.command.map { cmd =>
      (for
        configOpt <- CliConfig.load[IO]

        // Handle init command specially (doesn't need config)
        result <- cmd match
          case Command.Init =>
            CliConfig.createSampleConfig[IO].as(ExitCode.Success)

          case Command.Version =>
            IO.println("GitHub Actions CLI v0.1.0") *>
              IO.println("Built with Scala 3.5.0 and Typelevel stack") *>
              IO.pure(ExitCode.Success)

          case _ =>
            configOpt match
              case Some(config) =>
                val executor = CommandExecutor[IO](config)
                executor.execute(cmd).as(ExitCode.Success).handleErrorWith {
                  err =>
                    IO.println(s"Error: ${err.getMessage}") *>
                      IO.pure(ExitCode.Error)
                }

              case None =>
                IO.println("Error: GitHub token not found") *>
                  IO.println("") *>
                  IO.println(
                    "Please set GITHUB_TOKEN environment variable or run:"
                  ) *>
                  IO.println("  gh-actions init") *>
                  IO.println("") *>
                  IO.println(
                    "Then edit ~/.github-actions-cli.conf and add your token"
                  ) *>
                  IO.pure(ExitCode.Error)
      yield result).handleErrorWith { err =>
        IO.println(s"Fatal error: ${err.getMessage}") *>
          IO.pure(ExitCode.Error)
      }
    }
