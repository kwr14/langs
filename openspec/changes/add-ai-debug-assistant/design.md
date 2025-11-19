# AI Debugging Assistant – Design

## Architecture Overview

- AssistantService: orchestrates context collection, prompt building, model invocation, and suggestion formatting.
- ContextCollector: gathers run, jobs, steps, selected job logs, recent workflow history, environment metadata.
- RedactionService: removes secrets/tokens/URLs per policy before sending to model.
- PromptBuilder: converts context into compact, structured prompt (fail summary, logs excerpts, system constraints).
- ModelClient: pluggable interface to AI providers (OpenAI-compatible REST, local server). Retries, timeouts, streaming.
- SuggestionParser: parses model output into a typed `AssistantSuggestion` list.
- Dashboard UI: AssistantPanel renderer and interactions (open/close, refresh, copy suggestions).
- Cache: per (owner, repo, runId, jobId) suggestions to minimize repeated calls.

## Data Flow

1. User presses `a` on a failed run/job.
2. AssistantService collects context -> PromptBuilder -> ModelClient.
3. Response parsed into suggestions -> stored in cache -> rendered in AssistantPanel.
4. User can copy commands/patches or open links. No automatic mutations.

## Key Interfaces

### Core Types

```scala
case class AssistantInput(
  owner: String,
  repo: String,
  run: WorkflowRun,
  job: Option[Job],
  logs: Option[String]
)

case class AssistantSuggestion(
  title: String,
  rationale: String,
  actions: List[AssistantAction],
  references: List[String],
  confidence: Option[Double]
)

sealed trait AssistantAction
case class CommandAction(cmd: String, description: String) extends AssistantAction
case class PatchAction(diff: String, description: String) extends AssistantAction
case class LinkAction(url: String, description: String) extends AssistantAction
```

### ModelClient

```scala
trait ModelClient[F[_]] {
  def complete(prompt: String, config: ModelConfig): F[String]
}

case class ModelConfig(
  provider: String, // openai, local, custom
  endpoint: String,
  apiKey: Option[String],
  model: String,
  temperature: Double = 0.2,
  maxTokens: Int = 2048,
  timeoutSeconds: Int = 20
)
```

### AssistantService

```scala
trait AssistantService[F[_]] {
  def analyze(input: AssistantInput): F[List[AssistantSuggestion]]
}
```

## UI Integration

- New view: ViewMode.Assistant.
- Keybinding: `a` toggles Assistant panel from RunDetail or JobDetail.
- Panel Layout: Title, Summary, Suggestions (expandable), Actions (copy), References.
- Footer: include `a:Assistant` hint when suggestions available or disabled message.

## Configuration

- CLI config file / env vars:
  - `AI_ASSISTANT_ENABLED=true|false`
  - `AI_ASSISTANT_PROVIDER=openai|local`
  - `AI_ASSISTANT_ENDPOINT=https://...`
  - `AI_ASSISTANT_API_KEY=...`
  - `AI_ASSISTANT_MODEL=gpt-4o-mini`
  - `AI_ASSISTANT_REDACTION=standard|strict`

## Redaction

- Default rules: mask `GITHUB_TOKEN`, `AWS_*` keys, URIs with credentials, secrets from env.
- Log scrubbing via regex patterns; configurable allow/deny lists.

## Error Handling

- Show non-blocking errors in panel; allow retry (`r`) within Assistant panel.
- Timeouts render partial context suggestions.
- Disabled state shows configuration instructions.

## Performance & Caching

- Cache suggestions for a run/job for 15 minutes.
- Debounce repeated opens; manual refresh triggers a re-query.

## Security

- Never send raw secrets; redact at source.
- Do not store model responses containing sensitive data.
- Allow disabling completely via config.