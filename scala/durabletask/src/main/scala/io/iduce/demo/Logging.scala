package io.iduce.demo

import org.slf4j.LoggerFactory
import cats.effect.IO

object Logging:
  private val logger = LoggerFactory.getLogger(Logging.getClass)

  def logInfo(message: String): IO[Unit] =
    IO.delay(logger.info(message))

  def logError(message: String, error: Throwable): IO[Unit] =
    IO.delay(logger.error(message, error))

  def logDebug(message: String): IO[Unit] =
    IO.delay(logger.debug(message))

  def logTrace(message: String): IO[Unit] =
    IO.delay(logger.trace(message))
