package com.github.actions.domain

import io.circe.{Decoder, Encoder}

/** Represents the conclusion of a completed workflow run or job */
enum WorkflowConclusion:
  case Success
  case Failure
  case Cancelled
  case Skipped
  case TimedOut
  case ActionRequired
  case Neutral

object WorkflowConclusion:
  given Decoder[WorkflowConclusion] = Decoder[String].emap {
    case "success"          => Right(Success)
    case "failure"          => Right(Failure)
    case "cancelled"        => Right(Cancelled)
    case "skipped"          => Right(Skipped)
    case "timed_out"        => Right(TimedOut)
    case "action_required"  => Right(ActionRequired)
    case "neutral"          => Right(Neutral)
    case other              => Left(s"Unknown workflow conclusion: $other")
  }

  given Encoder[WorkflowConclusion] = Encoder[String].contramap {
    case Success         => "success"
    case Failure         => "failure"
    case Cancelled       => "cancelled"
    case Skipped         => "skipped"
    case TimedOut        => "timed_out"
    case ActionRequired  => "action_required"
    case Neutral         => "neutral"
  }

  def fromString(s: String): Option[WorkflowConclusion] = s.toLowerCase match
    case "success"         => Some(Success)
    case "failure"         => Some(Failure)
    case "cancelled"       => Some(Cancelled)
    case "skipped"         => Some(Skipped)
    case "timed_out"       => Some(TimedOut)
    case "action_required" => Some(ActionRequired)
    case "neutral"         => Some(Neutral)
    case _                 => None

