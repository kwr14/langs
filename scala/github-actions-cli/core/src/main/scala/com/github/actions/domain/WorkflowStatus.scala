package com.github.actions.domain

import io.circe.{Decoder, Encoder}

/** Represents the current status of a workflow run or job */
enum WorkflowStatus:
  case Queued
  case InProgress
  case Completed

object WorkflowStatus:
  given Decoder[WorkflowStatus] = Decoder[String].emap {
    case "queued"      => Right(Queued)
    case "in_progress" => Right(InProgress)
    case "completed"   => Right(Completed)
    case other         => Left(s"Unknown workflow status: $other")
  }

  given Encoder[WorkflowStatus] = Encoder[String].contramap {
    case Queued     => "queued"
    case InProgress => "in_progress"
    case Completed  => "completed"
  }

  def fromString(s: String): Option[WorkflowStatus] = s.toLowerCase match
    case "queued"      => Some(Queued)
    case "in_progress" => Some(InProgress)
    case "completed"   => Some(Completed)
    case _             => None

