package com.github.actions.domain

case class AssistantActionCommand(cmd: String, description: String)
case class AssistantActionPatch(diff: String, description: String)
case class AssistantActionLink(url: String, description: String)

sealed trait AssistantAction
object AssistantAction:
  case class Command(action: AssistantActionCommand) extends AssistantAction
  case class Patch(action: AssistantActionPatch) extends AssistantAction
  case class Link(action: AssistantActionLink) extends AssistantAction

case class AssistantSuggestion(
  title: String,
  rationale: String,
  actions: List[AssistantAction],
  references: List[String],
  confidence: Option[Double]
)