# Arrow Key Navigation Fix - v0.1.1

## Problem

Arrow keys did not work for navigation in the interactive dashboard. Users could not move up/down through the workflow run list using arrow keys (↑↓).

## Root Cause

The `ConsoleKeyReader.readKey` method in `KeyEvent.scala` was using `StdIn.readLine()` which:

1. **Operates in canonical (line-buffered) mode** - waits for Enter key before returning input
2. **Cannot capture individual key presses** - requires full line input
3. **Cannot read ANSI escape sequences in real-time** - arrow keys send escape sequences like `\u001b[A` that need character-by-character reading

When a user pressed an arrow key:
- Terminal sends: `\u001b[A` (for Up arrow)
- `StdIn.readLine()` waits for Enter key
- User must press Enter after arrow key, defeating the purpose
- Even with Enter, the input might not be properly captured

## Solution

Implemented **raw terminal input reading** with the following changes:

### 1. Updated `KeyEvent.scala` - ConsoleKeyReader

**Before:**
```scala
override def readKey: F[KeyEvent] =
  Async[F].blocking {
    val input = StdIn.readLine()
    KeyEvent.parse(input).getOrElse(KeyEvent.Unknown)
  }
```

**After:**
```scala
override def readKey: F[KeyEvent] =
  Async[F].blocking {
    readRawInput()
  }

private def readRawInput(): KeyEvent =
  val firstByte = stdin.read()
  if firstByte == 27 then // ESC character
    // Read ANSI escape sequence
    Thread.sleep(10) // Allow sequence to arrive
    if stdin.available() > 0 then
      val secondByte = stdin.read()
      if secondByte == 91 then // '[' - ANSI escape sequence
        readAnsiSequence()
      else
        KeyEvent.Alt(secondByte.toChar)
    else
      KeyEvent.Escape
  else
    // Regular character handling
    ...
```

### 2. Updated `Terminal.scala` - Raw Mode Support

Added `stty` command integration to enable/disable raw terminal mode:

```scala
override def enableRawMode: F[Unit] =
  Async[F].blocking {
    // Save current settings
    originalSttySettings = Some("stty -g".!!.trim)
    
    // Enable raw mode: disable canonical mode, echo, and signals
    "stty -icanon -echo -isig min 1 time 0".!
  }

override def disableRawMode: F[Unit] =
  Async[F].blocking {
    originalSttySettings match
      case Some(settings) =>
        // Restore original settings
        s"stty $settings".!
      case None =>
        "stty sane".!
  }
```

**stty flags explained:**
- `-icanon`: Disable canonical mode (line buffering) - read character-by-character
- `-echo`: Disable echo - don't show typed characters
- `-isig`: Disable interrupt signals - prevent Ctrl+C from killing process immediately
- `min 1`: Read returns after 1 character is available
- `time 0`: No timeout - read blocks until character available

### 3. Updated `Dashboard.scala` - Resource Management

Used `bracket` to ensure terminal cleanup happens even on error:

```scala
def run(autoRefreshInterval: Option[FiniteDuration]): F[Unit] =
  Async[F].bracket(
    // Acquire: setup terminal
    for
      _ <- terminal.enableRawMode
      _ <- terminal.enterAlternateScreen
      _ <- terminal.clear
      _ <- terminal.hideCursor
      _ <- refresh
      _ <- render
    yield ()
  )(
    // Use: run the dashboard
    _ => eventLoop.both(autoRefresh(interval).compile.drain).void
  )(
    // Release: cleanup terminal
    _ =>
      for
        _ <- terminal.showCursor
        _ <- terminal.exitAlternateScreen
        _ <- terminal.disableRawMode
        _ <- terminal.clear
      yield ()
  )
```

## Testing

1. **Build the updated JAR:**
   ```bash
   cd scala/github-actions-cli
   sbt "cli/assembly"
   ```

2. **Run the dashboard:**
   ```bash
   GITHUB_TOKEN=$(gh auth token) java -jar cli/target/scala-3.5.0/github-actions-cli.jar dashboard -o kwr14 -r langs
   ```

3. **Test arrow key navigation:**
   - Press ↑ (Up arrow) - should move selection up
   - Press ↓ (Down arrow) - should move selection down
   - Press Enter - should select item
   - Press q or Escape - should exit cleanly

4. **Verify vim-style keys still work:**
   - Press k - should move up
   - Press j - should move down

## Platform Compatibility

- ✅ **macOS/Linux**: Full support using `stty` command
- ⚠️ **Windows**: `stty` not available, falls back to graceful degradation
  - Arrow keys may not work on Windows
  - Vim-style keys (j/k) still work as alternative

## Files Changed

1. `terminal-ui/src/main/scala/com/github/actions/ui/KeyEvent.scala`
   - Replaced `StdIn.readLine()` with `System.in.read()`
   - Added `readRawInput()` method for character-by-character reading
   - Added `readAnsiSequence()` method for parsing escape sequences

2. `terminal-ui/src/main/scala/com/github/actions/ui/Terminal.scala`
   - Implemented `enableRawMode` using `stty` command
   - Implemented `disableRawMode` to restore terminal settings
   - Added terminal size detection using `tput`

3. `terminal-ui/src/main/scala/com/github/actions/ui/Dashboard.scala`
   - Updated `run` method to use `bracket` for resource management
   - Ensures raw mode is enabled on start and disabled on exit

## Version

This fix will be released as **v0.1.1** - a patch release addressing a critical usability bug.

