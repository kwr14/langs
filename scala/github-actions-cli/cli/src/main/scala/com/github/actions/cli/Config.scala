package com.github.actions.cli

import cats.effect.kernel.Sync
import cats.syntax.all.*
import scala.io.Source
import java.nio.file.{Files, Path, Paths}

/** CLI configuration */
case class CliConfig(
  githubToken: String,
  defaultOwner: Option[String] = None,
  defaultRepo: Option[String] = None,
  autoRefreshInterval: Int = 30,  // seconds
  apiBaseUrl: String = "https://api.github.com"
)

object CliConfig:
  
  /** Load configuration from environment variables and config file */
  def load[F[_]: Sync]: F[Option[CliConfig]] =
    for
      envToken <- Sync[F].delay(sys.env.get("GITHUB_TOKEN"))
      configFile <- loadConfigFile[F]
      fileToken = configFile.flatMap(_.get("github.token"))
      fileOwner = configFile.flatMap(_.get("github.default_owner"))
      fileRepo = configFile.flatMap(_.get("github.default_repo"))
      fileInterval = configFile.flatMap(_.get("refresh.interval")).flatMap(_.toIntOption)
      fileBaseUrl = configFile.flatMap(_.get("api.base_url"))
      
      // Environment variables take precedence over config file
      token = envToken.orElse(fileToken)
      
      config = token.map { t =>
        CliConfig(
          githubToken = t,
          defaultOwner = fileOwner,
          defaultRepo = fileRepo,
          autoRefreshInterval = fileInterval.getOrElse(30),
          apiBaseUrl = fileBaseUrl.getOrElse("https://api.github.com")
        )
      }
    yield config
  
  /** Load configuration from file */
  private def loadConfigFile[F[_]: Sync]: F[Option[Map[String, String]]] =
    Sync[F].delay {
      val configPath = getConfigPath
      if Files.exists(configPath) then
        val source = Source.fromFile(configPath.toFile)
        try
          val lines = source.getLines().toList
          val config = parseConfigFile(lines)
          Some(config)
        finally
          source.close()
      else
        None
    }.handleError(_ => None)
  
  /** Get config file path */
  private def getConfigPath: Path =
    val home = sys.env.getOrElse("HOME", sys.env.getOrElse("USERPROFILE", "."))
    Paths.get(home, ".github-actions-cli.conf")
  
  /** Parse simple key=value config file */
  private def parseConfigFile(lines: List[String]): Map[String, String] =
    lines
      .map(_.trim)
      .filterNot(line => line.isEmpty || line.startsWith("#"))
      .flatMap { line =>
        line.split("=", 2) match
          case Array(key, value) => Some(key.trim -> value.trim)
          case _ => None
      }
      .toMap
  
  /** Create a sample config file */
  def createSampleConfig[F[_]: Sync]: F[Unit] =
    Sync[F].delay {
      val configPath = getConfigPath
      if !Files.exists(configPath) then
        val sampleConfig = """# GitHub Actions CLI Configuration
          |# 
          |# GitHub personal access token (required)
          |# You can also set this via GITHUB_TOKEN environment variable
          |github.token=ghp_your_token_here
          |
          |# Default repository owner (optional)
          |# github.default_owner=octocat
          |
          |# Default repository name (optional)
          |# github.default_repo=Hello-World
          |
          |# Auto-refresh interval in seconds (default: 30)
          |# refresh.interval=30
          |
          |# GitHub API base URL (default: https://api.github.com)
          |# api.base_url=https://api.github.com
          |""".stripMargin
        
        Files.writeString(configPath, sampleConfig)
        println(s"Created sample config file at: $configPath")
        println("Please edit the file and add your GitHub token.")
      else
        println(s"Config file already exists at: $configPath")
    }

/** Output format for CLI commands */
enum OutputFormat:
  case Json
  case Table
  case Plain

object OutputFormat:
  def fromString(s: String): Option[OutputFormat] =
    s.toLowerCase match
      case "json" => Some(Json)
      case "table" => Some(Table)
      case "plain" => Some(Plain)
      case _ => None

/** Common CLI options */
case class CommonOpts(
  owner: Option[String] = None,
  repo: Option[String] = None,
  token: Option[String] = None,
  format: OutputFormat = OutputFormat.Table,
  verbose: Boolean = false
):
  /** Resolve owner from options or config */
  def resolveOwner(config: CliConfig): Option[String] =
    owner.orElse(config.defaultOwner)
  
  /** Resolve repo from options or config */
  def resolveRepo(config: CliConfig): Option[String] =
    repo.orElse(config.defaultRepo)
  
  /** Resolve token from options or config */
  def resolveToken(config: CliConfig): String =
    token.getOrElse(config.githubToken)
  
  /** Get repository in owner/repo format */
  def getRepository(config: CliConfig): Option[(String, String)] =
    for
      o <- resolveOwner(config)
      r <- resolveRepo(config)
    yield (o, r)

