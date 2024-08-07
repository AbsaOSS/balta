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

import java.sql.ResultSet

/**
 *  This is an iterator over the result of a query.
 *
 * @param resultSet - the JDBC result of a query
 */
class QueryResult(resultSet: ResultSet) extends Iterator[QueryResultRow] {
  private [this] var resultSetHasNext: Option[Boolean] = Some(resultSet.next())

  override def hasNext: Boolean = {
    resultSetHasNext.getOrElse {
      val result = resultSet.next()
      resultSetHasNext = Some(result)
      result
    }
  }

  override def next(): QueryResultRow = {
    if (resultSetHasNext.isEmpty) {
      resultSet.next()
      new QueryResultRow(resultSet)
    } else {
      resultSetHasNext = None
      new QueryResultRow(resultSet)

    }
  }
}
