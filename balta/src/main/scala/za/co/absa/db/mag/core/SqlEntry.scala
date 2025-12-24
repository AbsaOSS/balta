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

import za.co.absa.db.mag.core.SqlEntry.concat

import scala.language.implicitConversions

class SqlEntry(val entry: String) extends AnyVal {
  def +(other: SqlEntry): SqlEntry = concat(this.toOption, other.toOption).toSqlEntry

  def +(other: Option[SqlEntry]): SqlEntry = {
    other match {
      case None => this
      case _ => concat(this.toOption, other).toSqlEntry
    }
  }

  def ==(other: String): Boolean = this.entry == other
  def :=(other: SqlEntry): SqlEntry = this + SqlEntry(":=") + other
  def apply(params: String*): SqlEntry = {
    val paramsStr = params.mkString("(", ",", ")")
    this + SqlEntry(paramsStr)
  }

  /** Translates a sequence of SqlEntry entries into a single SqlEntry formatted as a parameter list
    *
    * @param params         - A sequence of SqlEntry to be included as parameters.
    * @param disambiguator  - Unused parameter to differentiate this method from `apply(params: String*)`
   *                          after JVM type erasure (both would have signature `apply(Seq)`)
    * @return               - A new SqlEntry that combines the input as a list of parameters/columns for a function or table.
    */
  def apply(params: Seq[SqlEntry], disambiguator: String = ""): SqlEntry = {
    val paramsEntry = params.mkSqlEntry("(", ",", ")")
    this + paramsEntry
  }

  def toOption: Option[SqlEntry] = {
    if (this.entry.trim.isEmpty) None else Some(this)
  }

  override def toString: String = entry

}

object SqlEntry {
  def apply(entry: String): SqlEntry = new SqlEntry(entry)
  def apply(firstColumn: ColumnReference, otherColumns: ColumnReference*): SqlEntry = {
    val allColumns = (firstColumn +: otherColumns).map(_.sqlEntry).mkString(", ")
    SqlEntry(allColumns)
  }

  def apply(maybeEntry: Option[String]): Option[SqlEntry] = maybeEntry.map(SqlEntry(_))

  implicit class SqlEntryOptionEnhancement(val sqlEntry: Option[SqlEntry]) extends AnyVal {
    def prefix(withEntry: SqlEntry): Option[SqlEntry] = sqlEntry.map(withEntry + _)

    def + (other: Option[SqlEntry]): Option[SqlEntry] = concat(sqlEntry, other)

    def + (other: SqlEntry): SqlEntry = concat(sqlEntry, other.toOption).toSqlEntry

    def toSqlEntry: SqlEntry = {
      sqlEntry.getOrElse(SqlEntry(""))
    }
  }

  implicit class SqlEntryListEnhancement(val sqlEntries: Seq[SqlEntry]) extends AnyVal {
    def mkSqlEntry(separator: String): SqlEntry = {
      mkSqlEntry("", separator, "")
    }
    def mkSqlEntry(start: String, separator: String = ", ", end: String): SqlEntry = {
      val entriesStr = sqlEntries.map(_.entry).mkString(start, separator, end)
      SqlEntry(entriesStr)
    }
  }

  implicit def sqlEntryToString(sqlEntry: SqlEntry): String = sqlEntry.entry

  private def concat(first: Option[SqlEntry], second: Option[SqlEntry]): Option[SqlEntry] = {
    (first, second) match {
      case (None, None) => None
      case (None, Some(_)) => second
      case (Some(_), None) => first
      case (Some(e1), Some(e2)) =>
        val first = e1.entry
        val second = e2.entry
        val e2Start = e2.entry(0) // first character of second.entry, can never be empty here because of the previous cases
        val e1End = e1.entry.last // last character of the first.entry, can never be empty here because of the previous cases
        val noSpaceStartChars = Set(',', '(', ')', '.')
        val noSpaceEndChars = Set('(')
        if (noSpaceStartChars.contains(e2Start) || noSpaceEndChars.contains(e1End)) {
          // It looks better to have no space before characters like , ( )
          Some(SqlEntry(s"$first$second"))
        } else {
          Some(SqlEntry(s"$first $second"))
        }

    }
  }

}
