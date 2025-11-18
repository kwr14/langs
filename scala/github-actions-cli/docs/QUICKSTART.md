# Quick Start Guide

Get up and running with GitHub Actions CLI in 5 minutes.

## Prerequisites

- **Java 11+** (for JAR version) or download native binary
- **GitHub Account** with access to repositories
- **GitHub Personal Access Token** with `repo` and `workflow` scopes

## Installation

### macOS / Linux (Quick Install)

```bash
curl -fsSL https://raw.githubusercontent.com/kwr14/langs/main/scala/github-actions-cli/scripts/install.sh | bash
```

### Manual Download

Download the latest release from [GitHub Releases](https://github.com/kwr14/langs/releases):

**Native Binary (Recommended):**
```bash
# Linux
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-linux-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/

# macOS
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-macos-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/
```

**JAR (Universal):**
```bash
curl -L -o github-actions-cli.jar https://github.com/kwr14/langs/releases/latest/download/github-actions-cli.jar
```

## Configuration

### 1. Create Configuration File

```bash
gh-actions init
```

This creates `~/.github-actions-cli.conf`.

### 2. Get GitHub Token

1. Visit https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Select scopes: `repo` and `workflow`
4. Copy the token

### 3. Add Token to Config

Edit `~/.github-actions-cli.conf`:

```properties
github.token=ghp_your_token_here
```

Or set environment variable:

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

## Basic Usage

### Interactive Dashboard

Monitor workflows in real-time:

```bash
gh-actions dashboard -o <owner> -r <repo>
```

**Example:**
```bash
gh-actions dashboard -o octocat -r Hello-World
```

**Controls:**
- `↑/↓` or `j/k` - Navigate
- `Enter` - View details
- `Esc` - Go back
- `r` - Refresh
- `q` - Quit

### List Workflow Runs

```bash
gh-actions list -o <owner> -r <repo>
```

**Filter by status:**
```bash
gh-actions list -o octocat -r Hello-World --status completed
```

**Filter by branch:**
```bash
gh-actions list -o octocat -r Hello-World --branch main
```

### Show Run Details

```bash
gh-actions show -o <owner> -r <repo> <run-id>
```

**Example:**
```bash
gh-actions show -o octocat -r Hello-World 1234567890
```

### Rerun Workflow

```bash
# Rerun entire workflow
gh-actions rerun -o <owner> -r <repo> <run-id>

# Rerun only failed jobs
gh-actions rerun -o <owner> -r <repo> <run-id> --failed-only
```

### Cancel Workflow

```bash
gh-actions cancel -o <owner> -r <repo> <run-id>
```

## Common Workflows

### Monitor CI/CD Pipeline

```bash
# Launch dashboard with 10-second refresh
gh-actions dashboard -o myorg -r myrepo --refresh-interval 10
```

### Check Recent Failures

```bash
# List last 10 completed runs
gh-actions list -o myorg -r myrepo --status completed --limit 10
```

### Rerun Failed Build

```bash
# 1. Find the run ID
gh-actions list -o myorg -r myrepo --status completed --limit 5

# 2. Rerun failed jobs
gh-actions rerun -o myorg -r myrepo 1234567890 --failed-only
```

### Cancel Stuck Workflow

```bash
# 1. List in-progress runs
gh-actions list -o myorg -r myrepo --status in_progress

# 2. Cancel specific run
gh-actions cancel -o myorg -r myrepo 1234567890
```

## Tips

### Set Default Repository

Add to `~/.github-actions-cli.conf`:

```properties
github.default_owner=myorg
github.default_repo=myrepo
```

Then use shorter commands:
```bash
gh-actions dashboard  # Uses defaults
```

### Create Aliases

Add to `~/.bashrc` or `~/.zshrc`:

```bash
alias ghd='gh-actions dashboard'
alias ghl='gh-actions list'
alias ghs='gh-actions show'
```

Usage:
```bash
ghd -o myorg -r myrepo
ghl -o myorg -r myrepo --status completed
```

### Multiple Repositories

Use tmux to monitor multiple repos:

```bash
tmux new-session \; \
  send-keys 'gh-actions dashboard -o org1 -r repo1' C-m \; \
  split-window -h \; \
  send-keys 'gh-actions dashboard -o org2 -r repo2' C-m
```

## Troubleshooting

### "Error: GitHub token not found"

**Solution:** Set `GITHUB_TOKEN` environment variable or add to config file.

### "Error: 401 Unauthorized"

**Solution:** Generate a new token with `repo` and `workflow` scopes.

### "Error: 404 Not Found"

**Solution:** Verify repository exists and token has access.

### Terminal display issues

**Solution:** Use a modern terminal that supports ANSI colors.

## Next Steps

- Read the [User Guide](USER_GUIDE.md) for detailed documentation
- Check the [API Documentation](API.md) for developers
- Report issues on [GitHub](https://github.com/kwr14/langs/issues)

## Getting Help

- **Documentation**: [User Guide](USER_GUIDE.md)
- **Issues**: https://github.com/kwr14/langs/issues
- **Discussions**: https://github.com/kwr14/langs/discussions

Happy monitoring! 🚀

