package com.github.actions.ui

import fansi.Str
import cats.effect.kernel.Async
import cats.syntax.all.*

/** Rendered content for a component */
case class RenderTree(lines: List[Str]):
  /** Get a specific line */
  def line(index: Int): Option[Str] =
    lines.lift(index)

  /** Get the height of the rendered content */
  def height: Int = lines.length

  /** Get the maximum width of the rendered content */
  def width: Int = lines.map(_.length).maxOption.getOrElse(0)

  /** Pad lines to a specific width */
  def padTo(width: Int): RenderTree =
    RenderTree(lines.map { line =>
      val padding = width - line.length
      if padding > 0 then Str(line.plainText + (" " * padding))
      else line
    })

  /** Truncate lines to a specific width */
  def truncate(width: Int): RenderTree =
    RenderTree(lines.map { line =>
      if line.length > width then Str(line.plainText.take(width - 3) + "...")
      else line
    })

  /** Combine with another render tree vertically */
  def ++(other: RenderTree): RenderTree =
    RenderTree(lines ++ other.lines)

  /** Take first n lines */
  def take(n: Int): RenderTree =
    RenderTree(lines.take(n))

  /** Drop first n lines */
  def drop(n: Int): RenderTree =
    RenderTree(lines.drop(n))

object RenderTree:
  /** Empty render tree */
  val empty: RenderTree = RenderTree(List.empty)

  /** Create from a single line */
  def line(str: Str): RenderTree = RenderTree(List(str))

  /** Create from plain text */
  def text(text: String): RenderTree = RenderTree(List(Str(text)))

  /** Create from multiple lines */
  def lines(strs: List[Str]): RenderTree = RenderTree(strs)

/** Base trait for UI components */
trait Component[F[_], S]:
  /** Render the component with the given state and bounds */
  def render(state: S, bounds: Rect): F[RenderTree]

/** Stateless component */
trait StatelessComponent[F[_]] extends Component[F, Unit]:
  /** Render the component with the given bounds */
  def render(bounds: Rect): F[RenderTree]

  override def render(state: Unit, bounds: Rect): F[RenderTree] =
    render(bounds)

/** Common UI components */
object Components:

  /** Text component */
  class Text[F[_]: Async](text: String) extends StatelessComponent[F]:
    override def render(bounds: Rect): F[RenderTree] =
      val lines = text.split("\n").toList
      val rendered = lines.map(Str(_))
      Async[F].pure(RenderTree(rendered))

  /** Styled text component */
  class StyledText[F[_]: Async](text: Str) extends StatelessComponent[F]:
    override def render(bounds: Rect): F[RenderTree] =
      Async[F].pure(RenderTree.line(text))

  /** List component */
  class ItemList[F[_]: Async, A](
      items: scala.List[A],
      renderItem: (A, Int, Boolean) => Str,
      selectedIndex: Int = 0
  ) extends StatelessComponent[F]:
    override def render(bounds: Rect): F[RenderTree] =
      val rendered = items.zipWithIndex.map { case (item, idx) =>
        val isSelected = idx == selectedIndex
        renderItem(item, idx, isSelected)
      }
      Async[F].pure(RenderTree(rendered))

  /** Progress bar component */
  class ProgressBar[F[_]: Async](
      current: Int,
      total: Int,
      width: Int = 20,
      showPercentage: Boolean = true
  ) extends StatelessComponent[F]:
    override def render(bounds: Rect): F[RenderTree] =
      val percentage = if total > 0 then (current * 100) / total else 0
      val filled = (width * current) / total.max(1)
      val empty = width - filled

      val bar = Str("█" * filled + "░" * empty).overlay(Style.Colors.info)
      val text =
        if showPercentage then Str(s" $percentage%")
        else Str("")

      Async[F].pure(RenderTree.line(bar ++ text))

  /** Spinner component */
  class Spinner[F[_]: Async](frame: Int = 0) extends StatelessComponent[F]:
    private val frames = List("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")

    override def render(bounds: Rect): F[RenderTree] =
      val symbol = frames(frame % frames.length)
      Async[F].pure(RenderTree.line(Str(symbol).overlay(Style.Colors.info)))

  /** Box component */
  class Box[F[_]: Async](
      title: Option[String] = None,
      content: RenderTree = RenderTree.empty
  ) extends StatelessComponent[F]:
    override def render(bounds: Rect): F[RenderTree] =
      val border = Style.box(bounds.width, bounds.height, title)

      // Insert content into the box
      val contentLines = content.lines.take(bounds.height - 2)
      val paddedContent = contentLines.map { line =>
        val padding = bounds.width - line.length - 2
        Str(
          s"${Style.Symbols.verticalLine} ${line.plainText}${" " * padding}${Style.Symbols.verticalLine}"
        ).overlay(Style.Colors.dim)
      }

      val result =
        border.head :: paddedContent ::: border.tail.drop(contentLines.length)
      Async[F].pure(RenderTree(result))
