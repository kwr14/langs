package com.github.actions.service

object RedactionService:
  private val patterns = List(
    "ghp_[A-Za-z0-9]{36}",
    "GITHUB_TOKEN=[^\n]+",
    "AWS_ACCESS_KEY_ID=[A-Z0-9]{16,20}",
    "AWS_SECRET_ACCESS_KEY=[A-Za-z0-9/+=]{30,40}"
  ).map(_.r)

  def redact(text: String): String =
    patterns.foldLeft(text) { (acc, r) => r.replaceAllIn(acc, "[REDACTED]") }

  def redactLines(lines: List[String]): List[String] =
    lines.map(redact)