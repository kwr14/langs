package cass4io

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** The Utils object provides utility functions for working with case classes
  * and their fields.
  */
object Utils {
  final case class ColumnInfo(
      columnName: String,
      columnType: Type,
      columnValue: Any
  )

  /** Lists the fields of a case class along with their corresponding column
    * information.
    *
    * @param caseClass
    *   The case class instance.
    * @tparam A
    *   The type of the case class.
    * @return
    *   A list of ColumnInfo objects representing the fields of the case class.
    */
  def listCaseClassFields[A: TypeTag: ClassTag](
      caseClass: A
  ): List[ColumnInfo] = {
    val mirror = runtimeMirror(caseClass.getClass.getClassLoader)
    val instanceMirror = mirror.reflect(caseClass)

    val fields = typeOf[A].members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }

    fields.map { field =>
      val name = field.name.toString
      val fieldType = field.returnType
      val value = instanceMirror.reflectMethod(field.asMethod).apply()

      ColumnInfo(name, fieldType, value)
    }.toList
  }
}
