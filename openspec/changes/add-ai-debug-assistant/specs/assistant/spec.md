# Assistant Specification

## Feature: AI Assistant for Failed Runs

### Triggers
- `a` key in RunDetail view when `run.isFailed` or any job `isFailed`.
- `a` key in JobDetail view when selected job `isFailed`.

### Inputs
- Repository owner/name
- Selected run with jobs, steps
- Selected job logs (last 200 lines by default; configurable)
- Recent run summary (last 10 runs outcome)
- Environment constraints (rate limit, retries)

### Processing
1. Collect context and redact secrets.
2. Build compact prompt including:
   - Failure summary and relevant log excerpts
   - Job/step metadata (names, durations)
   - System constraints and desired outcome
3. Call `ModelClient.complete(prompt, config)`.
4. Parse response to `AssistantSuggestion` list.
5. Cache results keyed by `(owner, repo, runId, jobId)`.

### Output Schema
```json
[
  {
    "title": "Pin flaky dependency version",
    "rationale": "Log shows timeout in step 'Install deps' due to mirror instability.",
    "actions": [
      {"type": "command", "cmd": "gh-actions rerun --failed-only", "description": "Retry failed jobs"},
      {"type": "patch", "diff": "--- a/.github/workflows/build.yml\n+++ b/.github/workflows/build.yml\n@@\n- uses: actions/setup-node@v4\n+ uses: actions/setup-node@v4\n+ with:\n+   node-version: '20.11.1'\n", "description": "Pin Node version"}
    ],
    "references": [
      "https://docs.github.com/actions/", 
      "https://docs.npmjs.com/"
    ],
    "confidence": 0.68
  }
]
```

### UI Behavior
- Assistant panel shows:
  - Header: "Assistant – <run name> / <job name>"
  - Sections: Summary, Causes, Suggested Fixes, References
  - Actions are list items; user can copy with `y` (yank) or open link with `o`.
- Footer in Assistant view: `r:Retry a:Close y:Copy o:Open q:Quit`.

### Keybindings
- Global: `a` opens/closes assistant in eligible views.
- Assistant panel:
  - `r` re-run analysis (refresh suggestions)
  - `y` copy selected action (command/patch) to clipboard
  - `o` open selected reference in browser
  - `Esc`/`a` close, `q` quit

### Limits
- Logs: default 200 lines, configurable up to 1000
- Max suggestions: 5
- Timeout: 20s default

### Configuration & Disable
- If `AI_ASSISTANT_ENABLED=false` or missing provider, show disabled message with setup instructions.

### Telemetry (optional)
- Count assistant opens, duration, suggestion types returned (no payloads)

### Error States
- Model timeout -> show partial context, allow retry
- Provider error -> show message, link to config docs