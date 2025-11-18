package com.github.actions.ui

import cats.effect.kernel.Async
import cats.syntax.all.*

/** Terminal algebra for effect-based terminal operations */
trait Terminal[F[_]]:
  /** Clear the entire terminal screen */
  def clear: F[Unit]

  /** Get terminal size as (width, height) */
  def size: F[(Int, Int)]

  /** Move cursor to position (x, y) where (0, 0) is top-left */
  def moveCursor(x: Int, y: Int): F[Unit]

  /** Hide the cursor */
  def hideCursor: F[Unit]

  /** Show the cursor */
  def showCursor: F[Unit]

  /** Write text at current cursor position */
  def write(text: String): F[Unit]

  /** Write text and move to next line */
  def writeLine(text: String): F[Unit]

  /** Flush output buffer */
  def flush: F[Unit]

  /** Enter alternate screen buffer (for full-screen apps) */
  def enterAlternateScreen: F[Unit]

  /** Exit alternate screen buffer */
  def exitAlternateScreen: F[Unit]

  /** Enable raw mode (disable line buffering, echo, etc.) */
  def enableRawMode: F[Unit]

  /** Disable raw mode */
  def disableRawMode: F[Unit]

object Terminal:
  /** ANSI escape codes for terminal control */
  object ANSI:
    val ESC = "\u001b"
    val CSI = s"$ESC["

    // Cursor control
    def moveTo(x: Int, y: Int): String = s"$CSI${y + 1};${x + 1}H"
    val hideCursor: String = s"${CSI}?25l"
    val showCursor: String = s"${CSI}?25h"

    // Screen control
    val clearScreen: String = s"${CSI}2J"
    val clearLine: String = s"${CSI}2K"
    val enterAltScreen: String = s"${CSI}?1049h"
    val exitAltScreen: String = s"${CSI}?1049l"

    // Colors (foreground)
    val resetColor: String = s"${CSI}0m"
    val black: String = s"${CSI}30m"
    val red: String = s"${CSI}31m"
    val green: String = s"${CSI}32m"
    val yellow: String = s"${CSI}33m"
    val blue: String = s"${CSI}34m"
    val magenta: String = s"${CSI}35m"
    val cyan: String = s"${CSI}36m"
    val white: String = s"${CSI}37m"

    // Bright colors
    val brightBlack: String = s"${CSI}90m"
    val brightRed: String = s"${CSI}91m"
    val brightGreen: String = s"${CSI}92m"
    val brightYellow: String = s"${CSI}93m"
    val brightBlue: String = s"${CSI}94m"
    val brightMagenta: String = s"${CSI}95m"
    val brightCyan: String = s"${CSI}96m"
    val brightWhite: String = s"${CSI}97m"

    // Styles
    val bold: String = s"${CSI}1m"
    val dim: String = s"${CSI}2m"
    val italic: String = s"${CSI}3m"
    val underline: String = s"${CSI}4m"
    val blink: String = s"${CSI}5m"
    val reverse: String = s"${CSI}7m"

    // Reset styles
    val resetBold: String = s"${CSI}22m"
    val resetDim: String = s"${CSI}22m"
    val resetItalic: String = s"${CSI}23m"
    val resetUnderline: String = s"${CSI}24m"
    val resetBlink: String = s"${CSI}25m"
    val resetReverse: String = s"${CSI}27m"

  /** Console-based terminal implementation using System.out */
  class ConsoleTerminal[F[_]: Async] extends Terminal[F]:
    import ANSI.*

    override def clear: F[Unit] =
      Async[F].delay(print(clearScreen))

    override def size: F[(Int, Int)] =
      // Default size, can be improved with JLine or similar
      Async[F].pure((80, 24))

    override def moveCursor(x: Int, y: Int): F[Unit] =
      Async[F].delay(print(moveTo(x, y)))

    override def hideCursor: F[Unit] =
      Async[F].delay(print(ANSI.hideCursor))

    override def showCursor: F[Unit] =
      Async[F].delay(print(ANSI.showCursor))

    override def write(text: String): F[Unit] =
      Async[F].delay(print(text))

    override def writeLine(text: String): F[Unit] =
      Async[F].delay(println(text))

    override def flush: F[Unit] =
      Async[F].delay(System.out.flush())

    override def enterAlternateScreen: F[Unit] =
      Async[F].delay(print(enterAltScreen))

    override def exitAlternateScreen: F[Unit] =
      Async[F].delay(print(exitAltScreen))

    override def enableRawMode: F[Unit] =
      // Raw mode requires platform-specific code or JLine
      Async[F].unit

    override def disableRawMode: F[Unit] =
      Async[F].unit

  /** Create a console terminal */
  def console[F[_]: Async]: Terminal[F] =
    new ConsoleTerminal[F]
