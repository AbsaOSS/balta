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

package za.co.absa.db.balta.classes

import za.co.absa.db.balta.classes.QueryResultRow.{Extractors, FieldNames}

import java.sql.{ResultSet, ResultSetMetaData, SQLException}

/**
 *  This is an iterator over the result of a query.
 *
 * @param resultSet - the JDBC result of a query
 */
class QueryResult(resultSet: ResultSet) extends Iterator[QueryResultRow] {
  val resultSetMetaData: ResultSetMetaData = resultSet.getMetaData
  val columnCount: Int = resultSetMetaData.getColumnCount

  private [this] var nextRow: Option[QueryResultRow] = None

  private [this] implicit val fieldNames: FieldNames = QueryResultRow.fieldNamesFromMetdata(resultSetMetaData)
  private [this] implicit val extractors: Extractors = QueryResultRow.createExtractors(resultSetMetaData)

  override def hasNext: Boolean = {
    if (nextRow.isEmpty) {
      try {
        if (resultSet.next()) {
          nextRow = Some(QueryResultRow(resultSet))
        }
      } catch {
        case e: SQLException => throw e // TODO Do nothing
      }
    }
    nextRow.nonEmpty
  }

  override def next(): QueryResultRow = {
    if (hasNext) {
      val row = nextRow.get
      nextRow = None
      row
    } else {
      throw new NoSuchElementException("No more rows in the result set")
    }
  }
}
