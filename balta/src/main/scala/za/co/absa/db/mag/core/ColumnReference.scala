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

trait ColumnReference extends SqlItem

case class ColumnName (enteredName: String,
                       sqlEntry: String,
                       quoteLess: String
                      ) extends ColumnReference{
  override def equals(obj: Any): Boolean = {
    obj match {
      case that: ColumnName => this.sqlEntry == that.sqlEntry
      case _ => false
    }
  }
  override def hashCode(): Int = sqlEntry.hashCode
}

object ColumnReference {
  private val standardColumnNamePattern = "^[a-z_][a-z0-9_]*$".r
  private val mixedCaseColumnNamePattern = "^[a-zA-Z_][a-zA-Z0-9_]*$".r
  private val quotedRegularColumnNamePattern = "^\"([a-z_][a-z0-9_]*)\"$".r
  private val quotedColumnNamePattern = "^\"(.+)\"$".r

  private[core] def quote(stringToQuote: String): String = s""""$stringToQuote""""
  private[core] def escapeQuote(stringToEscape: String): String = stringToEscape.replace("\"", "\"\"")
  private[core] def hasUnescapedQuotes(name: String): Boolean = {
    val reduced = name.replace("\"\"", "")
    reduced.contains('"')
  }

  def apply(name: String): ColumnName = {
    val trimmedName = name.trim
    trimmedName match {
      case standardColumnNamePattern() =>
        ColumnName(trimmedName, trimmedName, trimmedName) // column name per SQL standard, no quoting needed
      case mixedCaseColumnNamePattern() =>
        val loweredColumnName = trimmedName.toLowerCase
        ColumnName(trimmedName, loweredColumnName, loweredColumnName) // mixed case name, turn to lower case for sql entry (per standard)
      case quotedRegularColumnNamePattern(columnName) =>
        ColumnName(trimmedName, columnName, columnName) // quoted but regular name, remove quotes
      case quotedColumnNamePattern(actualColumnName)  =>
        if (hasUnescapedQuotes(actualColumnName)) {
          throw new IllegalArgumentException(s"Column name '$actualColumnName' has unescaped quotes. Use double quotes as escape sequence.")
        }
        val unescapedColumnName = actualColumnName.replace("\"\"", "\"")
        ColumnName(trimmedName, trimmedName, unescapedColumnName) // quoted name, use as is
      case _ =>
        ColumnName(trimmedName, quote(escapeQuote(trimmedName)), trimmedName) // needs quoting and perhaps escaping
    }
  }

  def apply(index: Int): ColumnReference = {
    ColumnIndex(index)
  }

  final case class ColumnIndex private(index: Int) extends ColumnReference {
    val sqlEntry: String = index.toString
  }
}

object ColumnName {
  def apply(name: String): ColumnName = ColumnReference(name)
}
