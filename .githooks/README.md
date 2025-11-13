# Git Hooks

This directory contains Git hooks for the repository.

## Setup

To use these hooks, run:

```bash
git config core.hooksPath .githooks
```

## Available Hooks

### pre-push

Automatically formats Gleam code before pushing to ensure code quality and consistency.

- Runs `gleam format` on all Gleam projects
- Prevents push if code needs formatting
- Provides clear instructions on how to fix formatting issues

