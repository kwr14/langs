# langs

A small monorepo for experiments across Python, Scala, Java, and LLM/RAG.

## Overview
- Central place to prototype ideas in multiple languages.
- GitHub Actions keep basic CI coverage and publish a consolidated status in `CI_STATUS.md`.
- The CI Build Monitor adds a "Last Run" timestamp per workflow.

## Repository Structure
- `python/p0/` — minimal Python project with `pyproject.toml` and `main.py`
- `scala/` — several Scala projects:
  - `effects/` — functional effects playground
  - `durabletask/` — durable task patterns
  - `cassandra-best-practise/` — Cassandra experiments
- `java/continuations/` — Java continuations exploration
- `llm-rag/` — retrieval-augmented generation experiments
- `.github/workflows/` — CI workflows

## Workflows
- `build-monitor.yml` — CI Build Monitor that writes `CI_STATUS.md`
- `monorepo-basic.yml` — Monorepo Common CI for general checks
- `python-p0.yml` — Python p0 CI
- `scala-*.yml` — Scala workflows (some are placeholders / n/a)

## CI Status
- Latest consolidated CI snapshot: see `CI_STATUS.md` in repo root.
- To refresh the monitor manually (requires GitHub CLI and repo access):
  - `gh workflow run 205117073 --ref main`
  - `gh run list --workflow "build-monitor.yml"`
  - After it finishes, `git pull --rebase origin main` to fetch the updated `CI_STATUS.md`.

## Getting Started
- Python p0
  - Using `uv`: `cd python/p0 && uv sync && uv run python main.py`
  - Using venv: `cd python/p0 && python -m venv .venv && source .venv/bin/activate && pip install -e . && python main.py`
- Scala projects
  - Requires `sbt`. Example: `cd scala/effects && sbt test`
- Java continuations
  - Build tooling is exploratory; specifics TBD per subfolder.

## Development Notes
- Requires GitHub CLI (`gh`) for manual CI monitor dispatch; already installed locally.
- Prefer small, focused changes; CI should remain green.
- If the monitor table wraps in your viewer, open `CI_STATUS.md` in a wide pane.

## Run CI Locally with `act`
Run workflows locally using Docker with [`nektos/act`](https://github.com/nektos/act).

Prerequisites (macOS):
- Install Docker Desktop and keep it running.
- Install act: `brew install act`

Useful runner image mapping (for `ubuntu-latest`):
- `-P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-22.04`

Basics:
- List available workflows/jobs: `act -l`
- Run a specific workflow file: `act -W .github/workflows/<file>.yml`
- Run a specific job: `act -W .github/workflows/<file>.yml -j <job>`

Examples:
- Build Monitor (`workflow_dispatch`):
  - Create an event payload (optional): `mkdir -p .github/act && echo '{}' > .github/act/workflow_dispatch.json`
  - Run: `act -W .github/workflows/build-monitor.yml -e .github/act/workflow_dispatch.json -P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-22.04`
  - If the workflow reads GitHub APIs, provide a token via secrets:
    - Create `.secrets` containing `GITHUB_TOKEN=<your-gh-token>`
    - Run with secrets: `act -W .github/workflows/build-monitor.yml --secret-file .secrets -P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-22.04`
- Monorepo Common CI repo overview:
  - Run: `act -W .github/workflows/monorepo-basic.yml -j repo-overview -P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-22.04`

Notes:
- `act` executes jobs in containers; ensure sufficient CPU/RAM in Docker settings.
- Some steps may behave slightly differently than GitHub-hosted runners (network/file permissions). Use `--container-options` if needed.
- For heavy language toolchains (Scala/JDK), the first runs may take time due to downloads.
