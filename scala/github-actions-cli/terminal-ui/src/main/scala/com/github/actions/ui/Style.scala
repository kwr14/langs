package com.github.actions.ui

import fansi.{Str, Bold, Underlined}
import com.github.actions.domain.{WorkflowStatus, WorkflowConclusion}

/** Color scheme and styling utilities for the terminal UI */
object Style:

  /** Color palette for the dashboard */
  object Colors:
    val success = fansi.Color.Green
    val failure = fansi.Color.Red
    val warning = fansi.Color.Yellow
    val info = fansi.Color.Blue
    val neutral = fansi.Color.White
    val dim = fansi.Color.LightGray
    val highlight = fansi.Color.Cyan
    val error = fansi.Color.Red

    // Status-specific colors
    val queued = fansi.Color.Yellow
    val inProgress = fansi.Color.Blue
    val completed = fansi.Color.Green
    val cancelled = fansi.Color.LightGray
    val skipped = fansi.Color.LightGray

  /** Get color for workflow status */
  def statusColor(status: WorkflowStatus): fansi.EscapeAttr =
    status match
      case WorkflowStatus.Queued     => Colors.queued
      case WorkflowStatus.InProgress => Colors.inProgress
      case WorkflowStatus.Completed  => Colors.completed

  /** Get color for workflow conclusion */
  def conclusionColor(conclusion: WorkflowConclusion): fansi.EscapeAttr =
    conclusion match
      case WorkflowConclusion.Success        => Colors.success
      case WorkflowConclusion.Failure        => Colors.failure
      case WorkflowConclusion.Cancelled      => Colors.cancelled
      case WorkflowConclusion.Skipped        => Colors.skipped
      case WorkflowConclusion.TimedOut       => Colors.warning
      case WorkflowConclusion.ActionRequired => Colors.warning
      case WorkflowConclusion.Neutral        => Colors.neutral

  /** Status indicator symbols */
  object Symbols:
    val success = "✓"
    val failure = "✗"
    val warning = "⚠"
    val info = "ℹ"
    val running = "●"
    val queued = "○"
    val cancelled = "⊘"
    val skipped = "⊝"

    // UI elements
    val arrow = "→"
    val bullet = "•"
    val verticalLine = "│"
    val horizontalLine = "─"
    val cornerTopLeft = "┌"
    val cornerTopRight = "┐"
    val cornerBottomLeft = "└"
    val cornerBottomRight = "┘"
    val teeLeft = "├"
    val teeRight = "┤"
    val teeTop = "┬"
    val teeBottom = "┴"
    val cross = "┼"

  /** Get symbol for workflow status */
  def statusSymbol(status: WorkflowStatus): String =
    status match
      case WorkflowStatus.Queued     => Symbols.queued
      case WorkflowStatus.InProgress => Symbols.running
      case WorkflowStatus.Completed  => Symbols.success

  /** Get symbol for workflow conclusion */
  def conclusionSymbol(conclusion: WorkflowConclusion): String =
    conclusion match
      case WorkflowConclusion.Success        => Symbols.success
      case WorkflowConclusion.Failure        => Symbols.failure
      case WorkflowConclusion.Cancelled      => Symbols.cancelled
      case WorkflowConclusion.Skipped        => Symbols.skipped
      case WorkflowConclusion.TimedOut       => Symbols.warning
      case WorkflowConclusion.ActionRequired => Symbols.warning
      case WorkflowConclusion.Neutral        => Symbols.info

  /** Format a status badge */
  def statusBadge(status: WorkflowStatus): Str =
    val color = statusColor(status)
    val symbol = statusSymbol(status)
    Str(s"$symbol ${status.toString.toUpperCase}").overlay(color)

  /** Format a conclusion badge */
  def conclusionBadge(conclusion: WorkflowConclusion): Str =
    val color = conclusionColor(conclusion)
    val symbol = conclusionSymbol(conclusion)
    Str(s"$symbol ${conclusion.toString.toUpperCase}").overlay(color)

  /** Format a title with bold styling */
  def title(text: String): Str =
    Str(text).overlay(Bold.On)

  /** Format a subtitle with dim styling */
  def subtitle(text: String): Str =
    Str(text).overlay(Colors.dim)

  /** Format highlighted text */
  def highlight(text: String): Str =
    Str(text).overlay(Colors.highlight).overlay(Bold.On)

  /** Format error text */
  def error(text: String): Str =
    Str(text).overlay(Colors.error).overlay(Bold.On)

  /** Format success text */
  def success(text: String): Str =
    Str(text).overlay(Colors.success).overlay(Bold.On)

  /** Format warning text */
  def warning(text: String): Str =
    Str(text).overlay(Colors.warning).overlay(Bold.On)

  /** Format info text */
  def info(text: String): Str =
    Str(text).overlay(Colors.info)

  /** Format dim text */
  def dim(text: String): Str =
    Str(text).overlay(Colors.dim)

  /** Format a horizontal line */
  def horizontalLine(width: Int): Str =
    Str(Symbols.horizontalLine * width).overlay(Colors.dim)

  /** Format a box border */
  def box(width: Int, height: Int, title: Option[String] = None): List[Str] =
    val top = title match
      case Some(t) =>
        val titleStr = s" $t "
        val leftPad = (width - titleStr.length - 2) / 2
        val rightPad = width - titleStr.length - 2 - leftPad
        Str(
          s"${Symbols.cornerTopLeft}${Symbols.horizontalLine * leftPad}$titleStr${Symbols.horizontalLine * rightPad}${Symbols.cornerTopRight}"
        ).overlay(Colors.dim)
      case None =>
        Str(
          s"${Symbols.cornerTopLeft}${Symbols.horizontalLine * (width - 2)}${Symbols.cornerTopRight}"
        ).overlay(Colors.dim)

    val middle = List.fill(height - 2)(
      Str(s"${Symbols.verticalLine}${" " * (width - 2)}${Symbols.verticalLine}")
        .overlay(Colors.dim)
    )

    val bottom = Str(
      s"${Symbols.cornerBottomLeft}${Symbols.horizontalLine * (width - 2)}${Symbols.cornerBottomRight}"
    ).overlay(Colors.dim)

    top :: middle ::: List(bottom)
