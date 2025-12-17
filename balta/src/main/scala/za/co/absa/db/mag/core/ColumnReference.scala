/*
 * Copyright 2023 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.db.mag.core

trait ColumnReference extends SqlItem {
  override def equals(obj: Any): Boolean = {
    obj match {
      case that: ColumnReference => this.sqlEntry == that.sqlEntry
      case _ => false
    }
  }
  override def hashCode(): Int = sqlEntry.hashCode
}

abstract class ColumnName extends ColumnReference{
  def enteredName: String
  def sqlEntry: SqlEntry
}

object ColumnReference {
  private val regularColumnNamePattern = "^([a-z_][a-z0-9_]*)$".r
  private val quotedRegularColumnNamePattern = "^\"([a-z_][a-z0-9_]*)\"$".r
  private val quotedColumnNamePattern = "^\"(.+)\"$".r

  private def quote(stringToQuote: String): String = s""""$stringToQuote""""
  private def escapeQuote(stringToEscape: String): String = stringToEscape.replace("\"", "\"\"")

  def apply(name: String): ColumnName = {
    val trimmedName = name.trim
    trimmedName match {
      case regularColumnNamePattern(columnName) => ColumnNameSimple(columnName) // column name per SQL standard, no quoting needed
      case quotedRegularColumnNamePattern(columnName) => ColumnNameExact(trimmedName, SqlEntry(columnName)) // quoted but regular name, remove quotes
      case quotedColumnNamePattern(_)  => ColumnNameSimple(trimmedName) // quoted name, use as is
      case _ => ColumnNameExact(trimmedName, SqlEntry(quote(escapeQuote(trimmedName)))) // needs quoting and perhaps escaping
    }
  }

  def apply(index: Int): ColumnReference = {
    ColumnIndex(index)
  }

  def unapply(columnName: ColumnName): String = columnName.enteredName

  final case class ColumnNameSimple private(enteredName: String) extends ColumnName {
    override def sqlEntry: SqlEntry = SqlEntry(enteredName)
  }

  final case class ColumnNameExact private(enteredName: String, sqlEntry: SqlEntry) extends ColumnName

  final case class ColumnIndex private(index: Int) extends ColumnReference {
    val sqlEntry: SqlEntry = SqlEntry(index.toString)
  }
}

object ColumnName {
  def apply(name: String): ColumnName = ColumnReference(name)
  def unapply(columnName: ColumnName): String = columnName.enteredName
}
