package com.github.actions.client

import cats.effect.kernel.Async

case class ModelConfig(
  provider: String,
  endpoint: String,
  apiKey: Option[String],
  model: String,
  temperature: Double = 0.2,
  maxTokens: Int = 2048,
  timeoutSeconds: Int = 20
)

trait ModelClient[F[_]]:
  def complete(prompt: String, config: ModelConfig): F[String]