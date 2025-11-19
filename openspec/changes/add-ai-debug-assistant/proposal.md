# Add AI Debugging Assistant for Failed Runs

## Summary

Introduce an AI assistant integrated into the GitHub Actions CLI dashboard that analyzes failed workflow runs, explains likely root causes, and suggests actionable fixes. The assistant presents guidance directly in the TUI with links, commands, and optional patch suggestions, accelerating triage and resolution.

## Goals

- Provide quick, context-aware debugging help for failed runs and jobs
- Suggest concrete fixes (config changes, retries, dependency pins, test updates)
- Integrate seamlessly in the dashboard: open assistant panel for selected failed run/job
- Respect privacy by redacting secrets and sensitive data before sending to AI
- Work with pluggable model providers (cloud or local) and be disabled when unconfigured

## Non-Goals

- Automatic code changes or direct repo modifications
- Full CI reconfiguration automation
- Replacing human review; suggestions are guidance, not authoritative

## User Stories

1. As a developer, when a run fails, I can press `a` to open an assistant panel that explains the failure and suggests a fix.
2. As a release engineer, I can get command suggestions (e.g., rerun failed jobs, increase timeouts) and links to docs.
3. As a platform engineer, I can configure a model provider and redaction policy globally; when unconfigured, the assistant is visibly disabled.

## UX Overview

- In Run Detail/Job Detail views, `a` opens the “Assistant” panel.
- The panel shows: Summary, Likely Causes, Suggested Fixes, References, and Commands.
- Footer updates with `a:Assistant` hint when applicable.
- Suggestions are copyable strings or patch previews; users take action manually.

## Risks & Mitigations

- Data exposure: redact tokens/secrets from logs; configurable allowlist/denylist.
- Incorrect suggestions: show confidence and references; keep actions manual.
- Latency/cost: cache per run/job; lightweight prompt; configurable timeouts.

## Rollout

- Phase 1: Local prompt builder, mock model client, TUI panel
- Phase 2: Model provider integration (OpenAI-compatible, local server)
- Phase 3: Redaction policies and caching; docs and examples