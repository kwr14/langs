package com.github.actions.ui

/** Represents a keyboard event */
enum KeyEvent:
  case Char(c: scala.Char)
  case Up
  case Down
  case Left
  case Right
  case Enter
  case Escape
  case Tab
  case Backspace
  case Delete
  case Home
  case End
  case PageUp
  case PageDown
  case F(n: Int) // F1-F12
  case Ctrl(c: scala.Char)
  case Alt(c: scala.Char)
  case Unknown

object KeyEvent:
  /** Parse ANSI escape sequence into KeyEvent */
  def parse(input: String): Option[KeyEvent] =
    if input.isEmpty then None
    else if input.length == 1 then
      input.charAt(0) match
        case '\n' | '\r'                => Some(Enter)
        case '\u001b'                   => Some(Escape)
        case '\t'                       => Some(Tab)
        case '\u007f'                   => Some(Backspace)
        case c if c.isControl && c < 32 =>
          // Ctrl+A = 1, Ctrl+B = 2, etc.
          Some(Ctrl((c + 96).toChar))
        case c => Some(Char(c))
    else if input.startsWith("\u001b[") then
      // ANSI escape sequence
      parseAnsiSequence(input.drop(2))
    else if input.startsWith("\u001b") then
      // Alt+key
      input.drop(1).headOption.map(c => Alt(c))
    else Some(Char(input.charAt(0)))

  private def parseAnsiSequence(seq: String): Option[KeyEvent] =
    seq match
      case "A"                       => Some(Up)
      case "B"                       => Some(Down)
      case "C"                       => Some(Right)
      case "D"                       => Some(Left)
      case "H"                       => Some(Home)
      case "F"                       => Some(End)
      case "5~"                      => Some(PageUp)
      case "6~"                      => Some(PageDown)
      case "3~"                      => Some(Delete)
      case s if s.matches("1[0-9]~") =>
        // F1-F10: 11~, 12~, ..., 21~
        val n = s.dropRight(1).toInt - 10
        Some(F(n))
      case s if s.matches("2[0-4]~") =>
        // F11-F12: 23~, 24~
        val n = s.dropRight(1).toInt - 12
        Some(F(n))
      case _ => Some(Unknown)

/** Keyboard input reader */
trait KeyReader[F[_]]:
  /** Read next key event (blocking) */
  def readKey: F[KeyEvent]

  /** Read next key event with timeout */
  def readKeyTimeout(timeoutMs: Long): F[Option[KeyEvent]]

  /** Check if a key is available without blocking */
  def available: F[Boolean]

object KeyReader:
  import cats.effect.kernel.Async
  import cats.syntax.all.*
  import java.io.{InputStream, IOException}

  /** Console-based key reader with raw terminal input */
  class ConsoleKeyReader[F[_]: Async] extends KeyReader[F]:
    private val stdin = System.in

    override def readKey: F[KeyEvent] =
      Async[F].blocking {
        readRawInput()
      }

    override def readKeyTimeout(timeoutMs: Long): F[Option[KeyEvent]] =
      // Simplified: just use readKey for now
      // A real implementation would use non-blocking I/O with timeout
      readKey.map(Some(_))

    override def available: F[Boolean] =
      Async[F].blocking {
        stdin.available() > 0
      }

    /** Read raw input from stdin, handling ANSI escape sequences */
    private def readRawInput(): KeyEvent =
      try
        val firstByte = stdin.read()
        if firstByte == -1 then KeyEvent.Unknown
        else if firstByte == 27 then // ESC
          // Check if there's more input (escape sequence)
          Thread.sleep(10) // Small delay to allow escape sequence to arrive
          if stdin.available() > 0 then
            val secondByte = stdin.read()
            if secondByte == 91 then // '[' - ANSI escape sequence
              readAnsiSequence()
            else
              // Alt+key
              KeyEvent.Alt(secondByte.toChar)
          else
            // Just ESC
            KeyEvent.Escape
        else
          // Regular character
          val c = firstByte.toChar
          c match
            case '\n' | '\r'                => KeyEvent.Enter
            case '\t'                       => KeyEvent.Tab
            case '\u007f'                   => KeyEvent.Backspace
            case c if c.isControl && c < 32 =>
              // Ctrl+A = 1, Ctrl+B = 2, etc.
              KeyEvent.Ctrl((c + 96).toChar)
            case c => KeyEvent.Char(c)
      catch case _: IOException => KeyEvent.Unknown

    /** Read ANSI escape sequence after ESC[ */
    private def readAnsiSequence(): KeyEvent =
      try
        val buffer = new StringBuilder
        var done = false

        while !done && stdin.available() > 0 do
          val b = stdin.read()
          val c = b.toChar
          buffer.append(c)

          // ANSI sequences end with a letter or ~
          if c.isLetter || c == '~' then done = true

        // Parse the sequence
        val seq = buffer.toString
        seq match
          case "A"  => KeyEvent.Up
          case "B"  => KeyEvent.Down
          case "C"  => KeyEvent.Right
          case "D"  => KeyEvent.Left
          case "H"  => KeyEvent.Home
          case "F"  => KeyEvent.End
          case "5~" => KeyEvent.PageUp
          case "6~" => KeyEvent.PageDown
          case "3~" => KeyEvent.Delete
          case s if s.matches("1[0-9]~") =>
            val n = s.dropRight(1).toInt - 10
            KeyEvent.F(n)
          case s if s.matches("2[0-4]~") =>
            val n = s.dropRight(1).toInt - 12
            KeyEvent.F(n)
          case _ => KeyEvent.Unknown
      catch case _: IOException => KeyEvent.Unknown

  /** Create a console key reader */
  def console[F[_]: Async]: KeyReader[F] =
    new ConsoleKeyReader[F]

/** Navigation actions derived from key events */
enum NavigationAction:
  case MoveUp
  case MoveDown
  case MoveLeft
  case MoveRight
  case PageUp
  case PageDown
  case GoToTop
  case GoToBottom
  case Select
  case Back
  case Quit
  case Refresh
  case Filter
  case Help
  case OpenInBrowser
  case ToggleAutoRefresh
  case ToggleAssistant
  case Copy
  case ToggleAssistantVerbose
  case ToggleAssistantActionsOnly
  case Unknown

object NavigationAction:
  /** Map key event to navigation action */
  def fromKeyEvent(key: KeyEvent): NavigationAction =
    key match
      case KeyEvent.Up | KeyEvent.Char('k')        => MoveUp
      case KeyEvent.Down | KeyEvent.Char('j')      => MoveDown
      case KeyEvent.Left | KeyEvent.Char('h')      => MoveLeft
      case KeyEvent.Right | KeyEvent.Char('l')     => MoveRight
      case KeyEvent.PageUp                         => PageUp
      case KeyEvent.PageDown                       => PageDown
      case KeyEvent.Home | KeyEvent.Char('g')      => GoToTop
      case KeyEvent.End | KeyEvent.Char('G')       => GoToBottom
      case KeyEvent.Enter | KeyEvent.Char(' ')     => Select
      case KeyEvent.Escape                         => Back
      case KeyEvent.Char('q')                      => Quit
      case KeyEvent.Ctrl('c') | KeyEvent.Ctrl('d') => Quit
      case KeyEvent.Char('r') | KeyEvent.F(5)      => Refresh
      case KeyEvent.Char('/') | KeyEvent.Char('f') => Filter
      case KeyEvent.Char('?') | KeyEvent.F(1)      => Help
      case KeyEvent.Char('o') | KeyEvent.Char('O') => OpenInBrowser
      case KeyEvent.Char('p')                      => ToggleAutoRefresh
      case KeyEvent.Char('a')                      => ToggleAssistant
      case KeyEvent.Char('y')                      => Copy
      case KeyEvent.Char('v')                      => ToggleAssistantVerbose
      case KeyEvent.Char('s')                      => ToggleAssistantActionsOnly
      case _                                       => Unknown
