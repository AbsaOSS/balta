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
import za.co.absa.db.balta.typeclasses.{QueryParamValue, QueryParamType}

/**
  * This class represents a database table. It allows to perform INSERT, SELECT and COUNT operations on the table easily.
  *
  * @param tableName  The name of the table
  */
case class DBTable(tableName: String) extends DBQuerySupport{

  /**
    * Inserts a new row into the table.
    *
    * @param values     - a map of column names and values to be inserted.
    * @param connection - a database connection used for the INSERT operation.
    * @return           - the inserted row.
    */
  def insert(values: Params)(implicit connection: DBConnection): QueryResultRow = {
    val columns = values.keys.map {keys =>
      val keysString = keys.mkString(",") // TODO https://github.com/AbsaOSS/balta/issues/2
      s"($keysString)"
    }.getOrElse("")
    val paramStr = values.values.map(_.sqlEntry).mkString(",")
    val sql = s"INSERT INTO $tableName $columns VALUES($paramStr) RETURNING *;"
    runQuery(sql, values.values){_.next()}
  }

  /**
   * Returns a value of a field of a row selected by a key.
   *
   * @param keyName     - the name of the key column
   * @param keyValue    - the value of the key column
   * @param fieldName   - the name of the field to be returned
   * @param connection  - a database connection used for the SELECT operation.
   * @tparam K          - the type of the key value
   * @tparam T          - the type of the returned field value
   * @return            - the value of the field, if the value is NULL, then `Some(None)` is returned; if no row is found,
   *                    then `None` is returned.
   */
  def fieldValue[K: QueryParamType, T](keyName: String, keyValue: K, fieldName: String)
                                         (implicit connection: DBConnection): Option[Option[T]] = {
    where(Params.add(keyName, keyValue)){resultSet =>
      if (resultSet.hasNext) {
        Some(resultSet.next().getAs[T](fieldName))
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
    composeSelectAndRun(strToOption(paramsToWhereCondition(params)), None, params.values)(verify)
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
    composeSelectAndRun(strToOption(paramsToWhereCondition(params)), strToOption(orderBy), params.values)(verify)
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
    composeSelectAndRun(strToOption(condition), None)(verify)
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
    composeSelectAndRun(strToOption(condition), strToOption(orderBy))(verify)
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
    composeSelectAndRun(None, strToOption(orderBy))(verify)
  }

  def deleteWithCheck[R](verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(None)(verify)
  }

  def deleteWithCheck[R](whereParams: NamedParams)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(strToOption(paramsToWhereCondition(whereParams)), whereParams.values)(verify)
  }

  def deleteWithCheck[R](whereCondition: String)(verify: QueryResult => R)(implicit connection: DBConnection): R = {
    composeDeleteAndRun(strToOption(whereCondition))(verify)
  }

  def delete(whereParams: NamedParams)(implicit connection: DBConnection): Unit = {
    composeDeleteAndRun(strToOption(paramsToWhereCondition(whereParams)), whereParams.values)(_ => ())
  }

  def delete(whereCondition: String = "")(implicit connection: DBConnection): Unit = {
    composeDeleteAndRun(strToOption(whereCondition))(_ => ())
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
    composeCountAndRun(strToOption(paramsToWhereCondition(params)), params.values)
  }

  /**
   * Counts the rows in the table based on the provided condition.
   * @param condition   - the condition used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  @deprecated("Use countOnCondition instead", "0.2.0")
  def count(condition: String)(implicit connection: DBConnection): Long = {
    composeCountAndRun(strToOption(condition))
  }

  /**
   * Counts the rows in the table based on the provided parameters that form a condition.
   * @param params      - the parameters used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  def countOnCondition(params: NamedParams)(implicit connection: DBConnection): Long = {
    composeCountAndRun(strToOption(paramsToWhereCondition(params)), params.values)
  }

  /**
   * Counts the rows in the table based on the provided condition.
   * @param condition   - the condition used for the WHERE clause
   * @param connection  - a database connection used for the SELECT operation.
   * @return            - the number of rows
   */
  def countOnCondition(condition: String)(implicit connection: DBConnection): Long = {
    composeCountAndRun(strToOption(condition))
  }

  private def composeSelectAndRun[R](whereCondition: Option[String], orderByExpr: Option[String], values: List[QueryParamValue] = List.empty)
                              (verify: QueryResult => R)
                              (implicit connection: DBConnection): R = {
    val where = whereCondition.map("WHERE " + _).getOrElse("")
    val orderBy = orderByExpr.map("ORDER BY " + _).getOrElse("")
    val sql = s"SELECT * FROM $tableName $where $orderBy;"
    runQuery(sql, values)(verify)
  }

  private def composeDeleteAndRun[R](whereCondition: Option[String], values: List[QueryParamValue] = List.empty)
                                    (verify: QueryResult => R)
                                    (implicit connection: DBConnection): R = {
    val where = whereCondition.map("WHERE " + _).getOrElse("")
    val sql = s"DELETE FROM $tableName $where RETURNING *;"
    runQuery(sql, values)(verify)
  }

  private def composeCountAndRun[R](whereCondition: Option[String], values: List[QueryParamValue] = List.empty)
                                   (implicit connection: DBConnection): Long = {
    val where = whereCondition.map("WHERE " + _).getOrElse("")
    val sql = s"SELECT count(1) AS cnt FROM $tableName $where;"
    runQuery(sql, values) {resultSet =>
      resultSet.next().getLong("cnt").getOrElse(0)
    }
  }

  private def strToOption(str: String): Option[String] = {
    if (str.isEmpty) {
      None
    } else {
      Option(str)
    }
  }

  private def paramsToWhereCondition(params: NamedParams): String = {
    params.pairs.foldRight(List.empty[String]) {case ((fieldName, value), acc) =>
      // TODO https://github.com/AbsaOSS/balta/issues/2
      val condition = s"$fieldName ${value.equalityOperator} ${value.sqlEntry}"
      condition :: acc
    }.mkString(" AND ")
  }
}
