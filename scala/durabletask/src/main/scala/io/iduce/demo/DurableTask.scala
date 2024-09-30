package io.iduce.demo

import io.iduce.repo.TaskRepository

import cats.effect.IO
import java.sql.DriverManager

enum TaskResult:
  case SuccessfulResult(output: String)
  case FailedResult(error: Throwable)

case class TaskState(state: String)

trait DurableTask:
  def run(): IO[TaskResult]
  def taskId: String
  def metadata: Map[String, String]

class DurableTaskImpl(taskRepository: TaskRepository, taskId: String) extends DurableTask:

  override def run(): IO[TaskResult] =
    taskRepository.createTableIfNotExists().flatMap { _ =>
      Logging.logInfo(s"Executing $taskId").flatMap { _ =>
        taskRepository.retrieveState(taskId).flatMap {
          case Some(state) =>
            Logging.logInfo(s"Resuming task from state: ${state.state}").map {
              _ =>
                TaskResult
                  .SuccessfulResult(s"Resumed from state: ${state.state}")
            }
          case None =>
            val initialState = TaskState("initial")
            Logging.logInfo("Starting new task execution").flatMap { _ =>
              taskRepository.persistState(taskId, initialState).map { _ =>
                TaskResult.SuccessfulResult("Task executed successfully")
              }
            }
        }
      }
    }

  override def taskId: String = taskId
  override def metadata: Map[String, String] = Map.empty
