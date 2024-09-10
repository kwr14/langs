package io.iduce.repo

import cats.effect.IO
import io.iduce.demo.TaskState

trait TaskRepository:
  def persistState(taskId: String, state: TaskState): IO[Unit]
  def retrieveState(taskId: String): IO[Option[TaskState]]
  def createTableIfNotExists(): IO[Unit]
