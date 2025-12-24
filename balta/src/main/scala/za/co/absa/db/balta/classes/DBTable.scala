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

import za.co.absa.db.balta.classes.inner.Params
import za.co.absa.db.balta.classes.inner.Params.NamedParams
import za.co.absa.db.balta.typeclasses.{QueryParamType, QueryParamValue}
import za.co.absa.db.balta.classes.inner.Params.OrderedParams
import za.co.absa.db.mag.core.SqlEntry
import za.co.absa.db.mag.core.SqlEntryComposition._

/**
  * This class represents a database table. It allows to perform INSERT, SELECT and COUNT operations on the table easily.
  *
  * @param tableName  The name of the table
  */
case class DBTable(tableName: String) extends DBQuerySupport{
  private val table: SqlEntry = SqlEntry(tableName)
  /**
    * Inserts a new row into the table.
    *
    * @param values     - a map of column names and values to be inserted.
    * @param connection - a database connection used for the INSERT operation.
    * @return           - the inserted row.
    */
  def insert(values: Params)(implicit connection: DBConnection): QueryResultRow = {
    val columns = values match {
      case namedParams: NamedParams =>
        val x = namedParams.paramNames.map(_.sqlEntry)
        x
      case _: OrderedParams => Vector.empty
    }

    val paramValues = values.values.map(_.sqlEntry)
    val sql = INSERT INTO table(columns) VALUES(paramValues) RETURNING ALL
    runQuery(sql, values.values){_.next()}
  }

  /**
   * Returns a value of a field of a row selected by a key.
   *
   * @param keyName     - the name of the key column
   * @param keyValue    - the value of the key column
   * @param columnName  - the name of the field to be returned
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam K          - the type of the key value
   * @tparam T          - the type of the returned field value
   * @return            - the value of the field, if the value is NULL, then `Some(None)` is returned; if no row is found,
   *                    then `None` is returned.
   */
  def fieldValue[K: QueryParamType, T](keyName: String, keyValue: K, columnName: String)
                                         (implicit connection: DBConnection): Option[Option[T]] = {
    where(Params.add(keyName, keyValue)){resultSet =>
      if (resultSet.hasNext) {
        Some(resultSet.next().getAs[T](columnName))
      } else {
        None
      }
    }
  }

  /**
   * Selects the rows from the table based on the provided parameters and verifies the result via the verify function.
   * @param params      - the parameters used for the WHERE clause
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def where[R](params: NamedParams)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(paramsToWhereCondition(params).toOption, None, params.values)(verify)
  }

  /**
   * Selects the rows from the table based on the provided parameters and verifies the result via the verify function.
   * @param params      - the parameters used for the WHERE clause
   * @param orderBy     - the clause how to order the result
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def where[R](params: NamedParams, orderBy: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(paramsToWhereCondition(params).toOption, SqlEntry(orderBy).toOption, params.values)(verify)
  }

  /**
   * Selects the rows from the table based on the provided condition and verifies the result via the verify function.
   * @param condition   - the condition used for the WHERE clause
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def where[R](condition: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(SqlEntry(condition).toOption, None)(verify)
  }

  /**
   * Selects the rows from the table based on the provided condition and verifies the result via the verify function.
   * @param condition   - the condition used for the WHERE clause
   * @param orderBy     - the clause how to order the result
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def where[R](condition: String, orderBy: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(SqlEntry(condition).toOption, SqlEntry(orderBy).toOption)(verify)
  }

  /**
   * Returns all rows from the table and verifies the result via the verify function.
   *
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def all[R]()(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(None, None)(verify)
  }

  /**
   * Returns all rows from the table and verifies the result via the verify function.
   *
   * @param orderBy     - the clause how to order the result
   * @param verify      - the function that verifies the result
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def all[R](orderBy: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeSelectAndRun(None, SqlEntry(orderBy).toOption)(verify)
  }

  def deleteWithCheck[R](verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(None)(verify)
  }

  def deleteWithCheck[R](whereParams: NamedParams)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(paramsToWhereCondition(whereParams).toOption, whereParams.values)(verify)
  }

  def deleteWithCheck[R](whereCondition: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(SqlEntry(whereCondition).toOption)(verify)
  }

  def delete(whereParams: NamedParams)(implicit connection: DBConnection): Unit = {
    composeDeleteAndRun(paramsToWhereCondition(whereParams).toOption, whereParams.values)(_ => ())
  }

  def delete(whereCondition: String = "")(implicit connection: DBConnection): Unit = {
    composeDeleteAndRun(SqlEntry(whereCondition).toOption)(_ => ())
  }
  /**
   * Counts the rows in the table.
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  def count()(implicit connection: DBConnection): Long = {
    composeCountAndRun(None)
  }

  /**
   * Counts the rows in the table based on the provided parameters.
   * @param params      - the parameters used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  @deprecated("Use countOnCondition instead", "0.2.0")
  def count(params: NamedParams)(implicit connection: DBConnection): Long = {
    composeCountAndRun(paramsToWhereCondition(params).toOption, params.values)
  }

  /**
   * Counts the rows in the table based on the provided condition.
   * @param condition   - the condition used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  @deprecated("Use countOnCondition instead", "0.2.0")
  def count(condition: String)(implicit connection: DBConnection): Long = {
    composeCountAndRun(SqlEntry(condition).toOption)
  }

  /**
   * Counts the rows in the table based on the provided parameters that form a condition.
   * @param params      - the parameters used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  def countOnCondition(params: NamedParams)(implicit connection: DBConnection): Long = {
    composeCountAndRun(paramsToWhereCondition(params).toOption, params.values)
  }

  /**
   * Counts the rows in the table based on the provided condition.
   * @param condition   - the condition used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  def countOnCondition(condition: String)(implicit connection: DBConnection): Long = {
    composeCountAndRun(SqlEntry(condition).toOption)
  }

  private def composeSelectAndRun[R](whereCondition: Option[SqlEntry], orderBy: Option[SqlEntry], values: Vector[QueryParamValue] = Vector.empty)
                              (verify: QueryResult => R)
                              (implicit connection: DBConnection): R = {
    val sql = SELECT(ALL) FROM table WHERE whereCondition ORDER BY(orderBy)
    runQuery(sql, values)(verify)
  }

  private def composeDeleteAndRun[R](whereCondition: Option[SqlEntry], values: Vector[QueryParamValue] = Vector.empty)
                                    (verify: QueryResult => R)
                                    (implicit connection: DBConnection): R = {
    val sql = DELETE FROM table WHERE whereCondition RETURNING ALL
    runQuery(sql, values)(verify)
  }

  private def composeCountAndRun(whereCondition: Option[SqlEntry], values: Vector[QueryParamValue] = Vector.empty)
                                   (implicit connection: DBConnection): Long = {
    val sql = SELECT(COUNT_ALL) FROM table WHERE whereCondition
    runQuery(sql, values) {resultSet =>
      resultSet.next().getLong("cnt").getOrElse(0)
    }
  }

  private def paramsToWhereCondition(params: NamedParams): SqlEntry = {
    val resultList = params.items.foldRight(List.empty[SqlEntry]) {case ((columnName, value), acc) =>
      val condition = columnName.sqlEntry + value.equalityOperator + value.sqlEntry
      condition :: acc
    }
    resultList.mkSqlEntry(" AND ")
  }
}
