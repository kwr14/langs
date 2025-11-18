# GitHub Actions CLI - User Guide

Complete guide to using the GitHub Actions CLI for monitoring and managing your workflows.

## Table of Contents

- [Installation](#installation)
- [Configuration](#configuration)
- [Commands](#commands)
- [Interactive Dashboard](#interactive-dashboard)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)
- [Tips & Tricks](#tips--tricks)

## Installation

### Quick Install (Recommended)

For Unix/Linux/macOS:

```bash
curl -fsSL https://raw.githubusercontent.com/kwr14/langs/main/scala/github-actions-cli/scripts/install.sh | bash
```

This script will:
- Detect your OS and architecture
- Download the appropriate binary or JAR
- Install to `~/.local/bin/gh-actions`
- Provide instructions for adding to PATH

### Manual Installation

#### Option 1: Native Binary (Fastest)

Download the latest release for your platform:

**Linux:**
```bash
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-linux-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/
```

**macOS:**
```bash
curl -L -o gh-actions https://github.com/kwr14/langs/releases/latest/download/gh-actions-macos-x86_64
chmod +x gh-actions
sudo mv gh-actions /usr/local/bin/
```

#### Option 2: JAR (Universal)

Requires Java 11 or higher:

```bash
curl -L -o github-actions-cli.jar https://github.com/kwr14/langs/releases/latest/download/github-actions-cli.jar

# Run with:
java -jar github-actions-cli.jar --help
```

#### Option 3: Build from Source

Requires Java 11+ and sbt 1.10+:

```bash
git clone https://github.com/kwr14/langs.git
cd langs/scala/github-actions-cli

# Build fat JAR
sbt "cli/assembly"
# Output: cli/target/scala-3.5.0/github-actions-cli.jar

# Or build native image (requires GraalVM)
sbt "cli/nativeImage"
# Output: cli/target/native-image/gh-actions
```

### Verify Installation

```bash
gh-actions version
# Output: GitHub Actions CLI v0.1.0
```

## Configuration

### Step 1: Initialize Configuration

```bash
gh-actions init
```

This creates `~/.github-actions-cli.conf` with a sample configuration.

### Step 2: Get a GitHub Token

1. Go to https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Give it a descriptive name (e.g., "GitHub Actions CLI")
4. Select scopes:
   - ✅ `repo` - Full control of private repositories
   - ✅ `workflow` - Update GitHub Action workflows
5. Click **"Generate token"**
6. Copy the token (starts with `ghp_`)

### Step 3: Configure the Token

**Option A: Configuration File (Recommended)**

Edit `~/.github-actions-cli.conf`:

```properties
# GitHub personal access token (required)
github.token=ghp_your_token_here

# Default repository owner (optional)
github.default_owner=octocat

# Default repository name (optional)
github.default_repo=Hello-World

# Auto-refresh interval in seconds (default: 30)
refresh.interval=30

# GitHub API base URL (default: https://api.github.com)
# api.base_url=https://api.github.com
```

**Option B: Environment Variable**

```bash
export GITHUB_TOKEN=ghp_your_token_here

# Add to your shell profile for persistence:
echo 'export GITHUB_TOKEN=ghp_your_token_here' >> ~/.bashrc  # or ~/.zshrc
```

**Note:** Environment variables take precedence over the configuration file.

### Step 4: Test Configuration

```bash
gh-actions list -o octocat -r Hello-World
```

If configured correctly, you should see a list of workflow runs.

## Commands

### `dashboard` - Interactive Dashboard

Launch the interactive TUI dashboard:

```bash
gh-actions dashboard -o <owner> -r <repo>
```

**Options:**
- `-o, --owner <owner>` - Repository owner (required)
- `-r, --repo <repo>` - Repository name (required)
- `--no-auto-refresh` - Disable automatic refresh
- `--refresh-interval <seconds>` - Set refresh interval (default: 30)

**Examples:**
```bash
# Basic usage
gh-actions dashboard -o octocat -r Hello-World

# Disable auto-refresh
gh-actions dashboard -o octocat -r Hello-World --no-auto-refresh

# Custom refresh interval (60 seconds)
gh-actions dashboard -o octocat -r Hello-World --refresh-interval 60
```

### `list` - List Workflow Runs

List workflow runs with optional filtering:

```bash
gh-actions list -o <owner> -r <repo> [options]
```

**Options:**
- `-o, --owner <owner>` - Repository owner (required)
- `-r, --repo <repo>` - Repository name (required)
- `-s, --status <status>` - Filter by status: `queued`, `in_progress`, `completed`
- `-b, --branch <branch>` - Filter by branch name
- `-n, --limit <number>` - Limit number of results (default: 20)

**Examples:**
```bash
# List all workflow runs
gh-actions list -o octocat -r Hello-World

# List only completed runs
gh-actions list -o octocat -r Hello-World --status completed

# List runs on main branch
gh-actions list -o octocat -r Hello-World --branch main

# List last 10 runs
gh-actions list -o octocat -r Hello-World --limit 10

# Combine filters
gh-actions list -o octocat -r Hello-World --status completed --branch main --limit 5
```

### `show` - Show Workflow Run Details

Display detailed information about a specific workflow run:

```bash
gh-actions show -o <owner> -r <repo> <run-id>
```

**Arguments:**
- `<run-id>` - Workflow run ID (required)

**Options:**
- `-o, --owner <owner>` - Repository owner (required)
- `-r, --repo <repo>` - Repository name (required)

**Examples:**
```bash
# Show workflow run details
gh-actions show -o octocat -r Hello-World 1234567890
```

### `rerun` - Rerun Workflow

Rerun a workflow or only failed jobs:

```bash
gh-actions rerun -o <owner> -r <repo> <run-id> [options]
```

**Arguments:**
- `<run-id>` - Workflow run ID (required)

**Options:**
- `-o, --owner <owner>` - Repository owner (required)
- `-r, --repo <repo>` - Repository name (required)
- `--failed-only` - Rerun only failed jobs

**Examples:**
```bash
# Rerun entire workflow
gh-actions rerun -o octocat -r Hello-World 1234567890

# Rerun only failed jobs
gh-actions rerun -o octocat -r Hello-World 1234567890 --failed-only
```

### `cancel` - Cancel Workflow Run

Cancel a running workflow:

```bash
gh-actions cancel -o <owner> -r <repo> <run-id>
```

**Arguments:**
- `<run-id>` - Workflow run ID (required)

**Options:**
- `-o, --owner <owner>` - Repository owner (required)
- `-r, --repo <repo>` - Repository name (required)

**Examples:**
```bash
# Cancel a running workflow
gh-actions cancel -o octocat -r Hello-World 1234567890
```

### `init` - Initialize Configuration

Create a sample configuration file:

```bash
gh-actions init
```

This creates `~/.github-actions-cli.conf` with default settings and comments.

### `version` - Show Version

Display version information:

```bash
gh-actions version
```

## Interactive Dashboard

The interactive dashboard provides real-time monitoring of your workflows.

### Keyboard Controls

**Navigation:**
- `↑` or `k` - Move up
- `↓` or `j` - Move down
- `←` or `h` - Go back / Exit detail view
- `→` or `l` - Enter detail view (same as Enter)
- `PgUp` - Page up
- `PgDn` - Page down
- `Home` or `g` - Go to top
- `End` or `G` - Go to bottom

**Actions:**
- `Enter` or `Space` - Select / Drill down into details
- `r` or `F5` - Refresh now
- `Esc` or `Backspace` - Go back to previous view
- `q` - Quit from main view
- `Ctrl+C` or `Ctrl+D` - Force quit

**Help:**
- `?` or `F1` - Show help (planned)

### Dashboard Views

**1. Workflow Runs List (Main View)**
- Shows recent workflow runs
- Color-coded status indicators:
  - 🟢 Green - Success
  - 🔴 Red - Failure
  - 🟡 Yellow - In Progress
  - ⚪ Gray - Queued/Cancelled
- Displays: workflow name, branch, actor, duration, time ago

**2. Workflow Run Detail View**
- Shows jobs for selected workflow run
- Job status and duration
- Press Enter to drill down into job details

**3. Job Detail View**
- Shows steps for selected job
- Step-by-step execution details
- Step status and duration

### Auto-Refresh

The dashboard automatically refreshes at the configured interval (default: 30 seconds).

**Indicators:**
- `[●]` - Auto-refresh enabled
- `[○]` - Auto-refresh disabled

**Control:**
- Use `--no-auto-refresh` flag to disable
- Use `--refresh-interval <seconds>` to customize interval
- Press `r` or `F5` to manually refresh

## Examples

### Common Workflows

**Monitor a specific repository:**
```bash
gh-actions dashboard -o myorg -r myrepo
```

**Check recent failures:**
```bash
gh-actions list -o myorg -r myrepo --status completed | grep "✗"
```

**Rerun failed CI build:**
```bash
# Find the run ID
gh-actions list -o myorg -r myrepo --status completed --limit 5

# Rerun failed jobs
gh-actions rerun -o myorg -r myrepo 1234567890 --failed-only
```

**Monitor deployment:**
```bash
# Watch deployment workflow
gh-actions dashboard -o myorg -r myrepo --refresh-interval 10
```

**Cancel stuck workflow:**
```bash
# List in-progress runs
gh-actions list -o myorg -r myrepo --status in_progress

# Cancel specific run
gh-actions cancel -o myorg -r myrepo 1234567890
```

### Integration with Other Tools

**Use with jq for JSON processing:**
```bash
# (Future: when JSON output is implemented)
gh-actions list -o myorg -r myrepo --format json | jq '.[] | select(.conclusion == "failure")'
```

**Use in shell scripts:**
```bash
#!/bin/bash
# Check if latest workflow passed

OWNER="myorg"
REPO="myrepo"

# Get latest run status (placeholder - will work when JSON output is added)
STATUS=$(gh-actions list -o $OWNER -r $REPO --limit 1 --format json | jq -r '.[0].conclusion')

if [ "$STATUS" = "success" ]; then
    echo "✅ Latest workflow passed!"
    exit 0
else
    echo "❌ Latest workflow failed!"
    exit 1
fi
```

## Troubleshooting

### Common Issues

**1. "Error: GitHub token not found"**

**Solution:**
- Set `GITHUB_TOKEN` environment variable, or
- Run `gh-actions init` and add token to `~/.github-actions-cli.conf`

**2. "Error: 401 Unauthorized"**

**Causes:**
- Invalid or expired token
- Token doesn't have required scopes

**Solution:**
- Generate a new token with `repo` and `workflow` scopes
- Update your configuration

**3. "Error: 404 Not Found"**

**Causes:**
- Repository doesn't exist
- Token doesn't have access to the repository
- Incorrect owner or repo name

**Solution:**
- Verify repository exists: `https://github.com/<owner>/<repo>`
- Check token has access to the repository
- Verify owner and repo names are correct

**4. "Error: Rate limit exceeded"**

**Cause:**
- GitHub API rate limit reached (5000 requests/hour for authenticated users)

**Solution:**
- Wait for rate limit to reset
- Reduce auto-refresh frequency
- Check rate limit: `curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/rate_limit`

**5. Terminal display issues**

**Causes:**
- Terminal doesn't support ANSI colors
- Terminal size too small

**Solution:**
- Use a modern terminal (iTerm2, Terminal.app, GNOME Terminal, etc.)
- Increase terminal size (minimum 80x24 recommended)
- Check `TERM` environment variable is set correctly

### Debug Mode

For troubleshooting, you can run with verbose output (planned feature):

```bash
gh-actions --verbose dashboard -o myorg -r myrepo
```

### Getting Help

If you encounter issues:

1. Check this troubleshooting section
2. Search existing issues: https://github.com/kwr14/langs/issues
3. Open a new issue with:
   - Command you ran
   - Error message
   - OS and Java version
   - Steps to reproduce

## Tips & Tricks

### Aliases

Add shell aliases for frequently used commands:

```bash
# Add to ~/.bashrc or ~/.zshrc
alias ghd='gh-actions dashboard'
alias ghl='gh-actions list'
alias ghs='gh-actions show'

# Usage:
ghd -o myorg -r myrepo
ghl -o myorg -r myrepo --status completed
```

### Default Repository

Set default owner and repo in config to avoid typing them every time:

```properties
# ~/.github-actions-cli.conf
github.default_owner=myorg
github.default_repo=myrepo
```

Then use:
```bash
gh-actions dashboard  # Uses defaults
```

### Quick Status Check

Create a function to quickly check workflow status:

```bash
# Add to ~/.bashrc or ~/.zshrc
ghstatus() {
    gh-actions list -o ${1:-myorg} -r ${2:-myrepo} --limit 5
}

# Usage:
ghstatus              # Uses defaults
ghstatus octocat Hello-World
```

### Watch Mode

Use `watch` command for continuous monitoring:

```bash
watch -n 30 'gh-actions list -o myorg -r myrepo --limit 10'
```

### Multiple Repositories

Monitor multiple repositories with tmux or screen:

```bash
# Create tmux session with multiple panes
tmux new-session \; \
  send-keys 'gh-actions dashboard -o org1 -r repo1' C-m \; \
  split-window -h \; \
  send-keys 'gh-actions dashboard -o org2 -r repo2' C-m
```

---

## Next Steps

- Explore the [API Documentation](API.md) for developers
- Read the [Contributing Guide](../CONTRIBUTING.md) to contribute
- Check the [Changelog](../CHANGELOG.md) for updates
- Report issues on [GitHub](https://github.com/kwr14/langs/issues)

Happy monitoring! 🚀

