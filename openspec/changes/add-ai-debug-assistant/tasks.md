# Tasks – AI Debugging Assistant

## Phase 1: Foundations
- Add config keys and loader for assistant provider and redaction policy
- Define `AssistantInput`, `AssistantSuggestion`, `AssistantAction` types
- Implement `ModelClient` trait with mock implementation
- Implement `RedactionService` with default patterns
- Implement `PromptBuilder` (compact, structured prompt)
- Add cache module keyed by (owner, repo, runId, jobId)

## Phase 2: Services & UI
- Implement `AssistantService.analyze(...)` orchestrating collection, redaction, model call, parse
- Extend `GitHubClient` usage to fetch job logs (reuse existing `getJobLogs`)
- Add `ViewMode.Assistant` and `a` keybinding in dashboard
- Implement `AssistantPanel` renderer (summary, fixes, actions, references)
- Footer updates for assistant availability and hints

## Phase 3: Providers & Polish
- Implement OpenAI-compatible HTTP client (`ModelClient` impl)
- Optional local provider client (configurable endpoint)
- Add retry/backoff and timeouts
- Add clipboard support for `y` to copy actions
- Add link open for `o` on references
- Add log line limit configuration and UI toggles

## Phase 4: Security & Docs
- Harden redaction: environment secrets, token patterns, URL creds
- Add configuration docs and troubleshooting
- Add unit tests for redaction, prompt builder, suggestion parser
- Add integration test with mock model client