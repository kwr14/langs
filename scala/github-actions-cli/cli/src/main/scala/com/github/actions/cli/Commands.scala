package com.github.actions.cli

import com.monovore.decline.*
import cats.syntax.all.*
import com.github.actions.domain.WorkflowStatus

/** CLI commands */
sealed trait Command

object Command:

  /** Dashboard command - interactive TUI */
  case class Dashboard(
      owner: String,
      repo: String,
      autoRefresh: Boolean = true,
      refreshInterval: Int = 30
  ) extends Command

  /** List workflow runs */
  case class List(
      owner: String,
      repo: String,
      status: Option[WorkflowStatus] = None,
      branch: Option[String] = None,
      limit: Int = 20
  ) extends Command

  /** Show workflow run details */
  case class Show(
      owner: String,
      repo: String,
      runId: Long
  ) extends Command

  /** Rerun a workflow */
  case class Rerun(
      owner: String,
      repo: String,
      runId: Long,
      failedOnly: Boolean = false
  ) extends Command

  /** Cancel a workflow run */
  case class Cancel(
      owner: String,
      repo: String,
      runId: Long
  ) extends Command

  /** Initialize configuration */
  case object Init extends Command

  /** Show version */
  case object Version extends Command

/** Decline options and commands */
object CliOpts:

  // Common options
  val ownerOpt: Opts[Option[String]] =
    Opts.option[String]("owner", short = "o", help = "Repository owner").orNone

  val repoOpt: Opts[Option[String]] =
    Opts.option[String]("repo", short = "r", help = "Repository name").orNone

  val tokenOpt: Opts[Option[String]] =
    Opts
      .option[String](
        "token",
        short = "t",
        help = "GitHub personal access token"
      )
      .orNone

  val formatOpt: Opts[OutputFormat] =
    Opts
      .option[String](
        "format",
        short = "f",
        help = "Output format (json, table, plain)"
      )
      .mapValidated { s =>
        OutputFormat
          .fromString(s)
          .toValidNel(s"Invalid format: $s. Must be one of: json, table, plain")
      }
      .withDefault(OutputFormat.Table)

  val verboseOpt: Opts[Boolean] =
    Opts.flag("verbose", short = "v", help = "Verbose output").orFalse

  val commonOpts: Opts[CommonOpts] =
    (ownerOpt, repoOpt, tokenOpt, formatOpt, verboseOpt).mapN(CommonOpts.apply)

  // Dashboard command
  val dashboardCmd: Opts[Command.Dashboard] =
    Opts.subcommand("dashboard", "Interactive dashboard (TUI)") {
      (
        Opts.option[String]("owner", short = "o", help = "Repository owner"),
        Opts.option[String]("repo", short = "r", help = "Repository name"),
        Opts
          .flag("no-auto-refresh", help = "Disable auto-refresh")
          .as(false)
          .withDefault(true),
        Opts
          .option[Int](
            "refresh-interval",
            help = "Auto-refresh interval in seconds"
          )
          .withDefault(30)
      ).mapN(Command.Dashboard.apply)
    }

  // List command
  val listCmd: Opts[Command.List] =
    Opts.subcommand("list", "List workflow runs") {
      (
        Opts.option[String]("owner", short = "o", help = "Repository owner"),
        Opts.option[String]("repo", short = "r", help = "Repository name"),
        Opts
          .option[String](
            "status",
            short = "s",
            help = "Filter by status (queued, in_progress, completed)"
          )
          .mapValidated { s =>
            WorkflowStatus
              .fromString(s)
              .toValidNel(
                s"Invalid status: $s. Must be one of: queued, in_progress, completed"
              )
          }
          .orNone,
        Opts
          .option[String]("branch", short = "b", help = "Filter by branch")
          .orNone,
        Opts
          .option[Int](
            "limit",
            short = "n",
            help = "Maximum number of runs to show"
          )
          .withDefault(20)
      ).mapN(Command.List.apply)
    }

  // Show command
  val showCmd: Opts[Command.Show] =
    Opts.subcommand("show", "Show workflow run details") {
      (
        Opts.option[String]("owner", short = "o", help = "Repository owner"),
        Opts.option[String]("repo", short = "r", help = "Repository name"),
        Opts.argument[Long]("run-id")
      ).mapN(Command.Show.apply)
    }

  // Rerun command
  val rerunCmd: Opts[Command.Rerun] =
    Opts.subcommand("rerun", "Rerun a workflow") {
      (
        Opts.option[String]("owner", short = "o", help = "Repository owner"),
        Opts.option[String]("repo", short = "r", help = "Repository name"),
        Opts.argument[Long]("run-id"),
        Opts.flag("failed-only", help = "Rerun only failed jobs").orFalse
      ).mapN(Command.Rerun.apply)
    }

  // Cancel command
  val cancelCmd: Opts[Command.Cancel] =
    Opts.subcommand("cancel", "Cancel a workflow run") {
      (
        Opts.option[String]("owner", short = "o", help = "Repository owner"),
        Opts.option[String]("repo", short = "r", help = "Repository name"),
        Opts.argument[Long]("run-id")
      ).mapN(Command.Cancel.apply)
    }

  // Init command
  val initCmd: Opts[Command.Init.type] =
    Opts.subcommand("init", "Initialize configuration file") {
      Opts(Command.Init)
    }

  // Version command
  val versionCmd: Opts[Command.Version.type] =
    Opts.subcommand("version", "Show version information") {
      Opts(Command.Version)
    }

  // Main command parser
  val command: Opts[Command] =
    dashboardCmd orElse
      listCmd orElse
      showCmd orElse
      rerunCmd orElse
      cancelCmd orElse
      initCmd orElse
      versionCmd
