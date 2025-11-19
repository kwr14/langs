package com.github.actions.client

import cats.effect.kernel.Async

class OllamaModelClient[F[_]: Async] extends ModelClient[F]:
  override def complete(prompt: String, config: ModelConfig): F[String] =
    Async[F].blocking {
      val uri = java.net.URI.create(config.endpoint + "/api/generate")
      val body = s"{" + s"\"model\":\"${config.model}\",\"prompt\":${"""" + prompt.replace("\"", "\\\"") + """"},\"stream\":false}"
      val req = java.net.http.HttpRequest.newBuilder(uri)
        .header("Content-Type", "application/json")
        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
        .build()
      val client = java.net.http.HttpClient.newHttpClient()
      val resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString())
      val txt = resp.body()
      val idx = txt.indexOf("\"response\":")
      if idx >= 0 then
        val after = txt.substring(idx + 11)
        val start = after.indexOf('"') + 1
        val end = after.indexOf('"', start)
        if start > 0 && end > start then after.substring(start, end) else ""
      else ""
    }