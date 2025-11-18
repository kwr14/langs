package com.github.actions.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.syntax._
import io.circe.parser._

class WorkflowStatusSpec extends AnyFlatSpec with Matchers:

  "WorkflowStatus.fromString" should "parse valid status strings" in {
    WorkflowStatus.fromString("queued") shouldBe Some(WorkflowStatus.Queued)
    WorkflowStatus.fromString("in_progress") shouldBe Some(WorkflowStatus.InProgress)
    WorkflowStatus.fromString("completed") shouldBe Some(WorkflowStatus.Completed)
  }

  it should "be case insensitive" in {
    WorkflowStatus.fromString("QUEUED") shouldBe Some(WorkflowStatus.Queued)
    WorkflowStatus.fromString("In_Progress") shouldBe Some(WorkflowStatus.InProgress)
  }

  it should "return None for invalid strings" in {
    WorkflowStatus.fromString("invalid") shouldBe None
    WorkflowStatus.fromString("") shouldBe None
  }

  "WorkflowStatus JSON encoding" should "encode to correct strings" in {
    WorkflowStatus.Queued.asJson.noSpaces shouldBe "\"queued\""
    WorkflowStatus.InProgress.asJson.noSpaces shouldBe "\"in_progress\""
    WorkflowStatus.Completed.asJson.noSpaces shouldBe "\"completed\""
  }

  "WorkflowStatus JSON decoding" should "decode from correct strings" in {
    decode[WorkflowStatus]("\"queued\"") shouldBe Right(WorkflowStatus.Queued)
    decode[WorkflowStatus]("\"in_progress\"") shouldBe Right(WorkflowStatus.InProgress)
    decode[WorkflowStatus]("\"completed\"") shouldBe Right(WorkflowStatus.Completed)
  }

  it should "fail for invalid strings" in {
    decode[WorkflowStatus]("\"invalid\"").isLeft shouldBe true
  }

