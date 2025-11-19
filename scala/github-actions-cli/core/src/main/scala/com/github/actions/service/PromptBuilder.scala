package com.github.actions.service

import com.github.actions.domain.{WorkflowRun, Job}

object PromptBuilder:
  def build(run: WorkflowRun, job: Option[Job], logLines: List[String]): String =
    val title = s"Run: ${run.name} (${run.headBranch})"
    val jobLine = job.map(j => s"Job: ${j.name}").getOrElse("")
    val excerpt = logLines.takeRight(200).mkString("\n")
    s"""You are an expert CI assistant.
       |Analyze the failure and propose concise actionable fixes.
       |%s
       |%s
       |Recent logs:
       |%s""".stripMargin.format(title, jobLine, excerpt)