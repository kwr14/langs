package com.github.actions.ui

/** Layout primitives for terminal UI */

/** Rectangle representing a region of the terminal */
case class Rect(x: Int, y: Int, width: Int, height: Int):
  /** Check if this rectangle contains a point */
  def contains(px: Int, py: Int): Boolean =
    px >= x && px < x + width && py >= y && py < y + height
  
  /** Get the area of this rectangle */
  def area: Int = width * height
  
  /** Split horizontally into two rectangles */
  def splitHorizontal(leftWidth: Int): (Rect, Rect) =
    val left = Rect(x, y, leftWidth, height)
    val right = Rect(x + leftWidth, y, width - leftWidth, height)
    (left, right)
  
  /** Split vertically into two rectangles */
  def splitVertical(topHeight: Int): (Rect, Rect) =
    val top = Rect(x, y, width, topHeight)
    val bottom = Rect(x, y + topHeight, width, height - topHeight)
    (top, bottom)
  
  /** Add padding to all sides */
  def padding(amount: Int): Rect =
    Rect(x + amount, y + amount, width - 2 * amount, height - 2 * amount)
  
  /** Add margin to all sides */
  def margin(amount: Int): Rect =
    Rect(x + amount, y + amount, width - 2 * amount, height - 2 * amount)

object Rect:
  /** Create a rectangle from terminal size */
  def fromSize(width: Int, height: Int): Rect =
    Rect(0, 0, width, height)

/** Layout direction */
enum Direction:
  case Horizontal
  case Vertical

/** Layout constraint */
enum Constraint:
  case Length(n: Int)           // Fixed length
  case Percentage(n: Int)       // Percentage of available space (0-100)
  case Min(n: Int)              // Minimum length
  case Max(n: Int)              // Maximum length
  case Ratio(numerator: Int, denominator: Int)  // Ratio of available space

/** Layout utilities */
object Layout:
  /** Split a rectangle into multiple parts based on constraints */
  def split(rect: Rect, direction: Direction, constraints: List[Constraint]): List[Rect] =
    if constraints.isEmpty then
      List.empty
    else
      val totalSpace = direction match
        case Direction.Horizontal => rect.width
        case Direction.Vertical => rect.height
      
      // Calculate sizes for each constraint
      val sizes = calculateSizes(totalSpace, constraints)
      
      // Create rectangles
      direction match
        case Direction.Horizontal =>
          var currentX = rect.x
          sizes.map { width =>
            val r = Rect(currentX, rect.y, width, rect.height)
            currentX += width
            r
          }
        case Direction.Vertical =>
          var currentY = rect.y
          sizes.map { height =>
            val r = Rect(rect.x, currentY, rect.width, height)
            currentY += height
            r
          }
  
  /** Calculate sizes for constraints */
  private def calculateSizes(totalSpace: Int, constraints: List[Constraint]): List[Int] =
    // First pass: calculate fixed sizes
    val (fixedSizes, remainingSpace, flexibleIndices) = constraints.zipWithIndex.foldLeft((List.empty[Int], totalSpace, List.empty[Int])) {
      case ((sizes, remaining, flexible), (constraint, idx)) =>
        constraint match
          case Constraint.Length(n) =>
            (sizes :+ n, remaining - n, flexible)
          case _ =>
            (sizes :+ 0, remaining, flexible :+ idx)
    }
    
    // Second pass: distribute remaining space among flexible constraints
    if flexibleIndices.isEmpty then
      fixedSizes
    else
      val flexibleSpace = remainingSpace
      val flexibleCount = flexibleIndices.length
      val baseSize = flexibleSpace / flexibleCount
      val remainder = flexibleSpace % flexibleCount
      
      fixedSizes.zipWithIndex.map { case (size, idx) =>
        if flexibleIndices.contains(idx) then
          val extra = if idx < remainder then 1 else 0
          constraints(idx) match
            case Constraint.Percentage(pct) =>
              (flexibleSpace * pct / 100).max(0)
            case Constraint.Min(n) =>
              (baseSize + extra).max(n)
            case Constraint.Max(n) =>
              (baseSize + extra).min(n)
            case Constraint.Ratio(num, denom) =>
              (flexibleSpace * num / denom).max(0)
            case _ =>
              baseSize + extra
        else
          size
      }
  
  /** Create a vertical layout */
  def vertical(rect: Rect, constraints: List[Constraint]): List[Rect] =
    split(rect, Direction.Vertical, constraints)
  
  /** Create a horizontal layout */
  def horizontal(rect: Rect, constraints: List[Constraint]): List[Rect] =
    split(rect, Direction.Horizontal, constraints)
  
  /** Create a grid layout */
  def grid(rect: Rect, rows: Int, cols: Int): List[List[Rect]] =
    val rowHeight = rect.height / rows
    val colWidth = rect.width / cols
    
    (0 until rows).map { row =>
      (0 until cols).map { col =>
        Rect(
          rect.x + col * colWidth,
          rect.y + row * rowHeight,
          colWidth,
          rowHeight
        )
      }.toList
    }.toList

