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

import za.co.absa.db.balta.classes.DBFunction.{DBFunctionWithNamedParamsToo, DBFunctionWithPositionedParamsOnly}
import za.co.absa.db.balta.typeclasses.QueryParamType
import za.co.absa.db.balta.classes.inner.Params.{NamedParams, OrderedParams}

/**
 * A class that represents a database function call. It can be used to execute a function and verify the result.
 * THere are two implementations of this class:
 * - DBFunctionWithPositionedParamsOnly - the parameters are defined by their position solely
 * - DBFunctionWithNamedParamsToo - there can be parameters defined by their position and and others defined by their
 * name; note that the position defined parameters can be added only at the beginning of the parameter list
 *
 * @param functionName  - the name of the function
 * @param orderedParams - the list of parameters identified by their position (preceding the named parameters)
 * @param namedParams   - the list of parameters identified by their name (following the positioned parameters)
 *
 */
sealed abstract class DBFunction private(functionName: String,
                                         orderedParams: OrderedParams,
                                         namedParams: NamedParams) extends DBQuerySupport {

  private def sql(orderBy: String): String = {
    val positionedParamEntries = orderedParams.values.map(_.sqlEntry)
    val namedParamEntries = namedParams.items.map{ case (columnName, queryParamValue) =>
      columnName.sqlEntry + " := " + queryParamValue.sqlEntry
    }
    val paramEntries = positionedParamEntries ++ namedParamEntries
    val paramsLine = paramEntries.mkString(",")
    s"SELECT * FROM $functionName($paramsLine) $orderBy"
  }

  /**
   * Executes the function without caring about the result. The goal is the side-effect of the function.
   * @param connection  - the database connection
   */
  def perform()(implicit connection: DBConnection): Unit = {
    execute("")(_ => ())
  }

  /**
   * Executes the function without any verification procedure. It instantiates the function result(s) and returns them in
   * a list.
   * @param orderBy     - the clause how to order the function result, if empty, default ordering is preserved
   *                    examples:
   *                      * "product_id DESC"
   *                      * "product_name, import_date DESC"
   * @param connection  - the database connection
   */
  def getResult(orderBy: String = "")(implicit connection: DBConnection): List[QueryResultRow] = {
    execute(orderBy)(_.toList)
  }

  /**
   *  Executes the function and verifies the result via the verify function.
   *
   * @param verify      - the function that verifies the result
   * @param connection  - the database connection
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def execute[R](verify: QueryResult => R /* Assertion */)(implicit connection: DBConnection): R = {
    execute("")(verify)
  }

  /**
   *  Executes the function and verifies the result via the verify function.
   *
   * @param orderBy     - the clause how to order the function result
   *                    examples:
   *                      * "product_id DESC"
   *                      * "product_name, import_date DESC"
   * @param verify      - the function that verifies the result
   * @param connection  - the database connection
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def execute[R](orderBy: String)(verify: QueryResult => R /* Assertion */)(implicit connection: DBConnection): R = {
    val orderByPart = if (orderBy.nonEmpty) {s"ORDER BY $orderBy"} else ""
    runQuery(sql(orderByPart), orderedParams.values ++ namedParams.values)(verify)
  }

  /**
    * Sets a parameter for the function call. It actually creates a new instance of the DBFunction class with the new
    * parameter.
    *
    * @param paramName  - the name of the parameter to set
    * @param value      - the value of the parameter
    * @return           - a new instance of the DBFunction class with the new parameter
    */
  def setParam[T: QueryParamType](paramName: String, value: T): DBFunctionWithNamedParamsToo = {
    DBFunctionWithNamedParamsToo(functionName, orderedParams, namedParams.add(paramName, value))
  }

  /**
    * Sets a parameter to NULL for the function call. It actually creates a new instance of the DBFunction class with
    * the new parameter.
    *
    * @param paramName  - the name of the parameter to set
    * @return           - a new instance of the DBFunction class with the new parameter
    */
  @deprecated("Use setParam(NULL)", "balta 0.3.0")
  def setParamNull(paramName: String): DBFunctionWithNamedParamsToo = {
    setParam(paramName, QueryParamType.NULL)
  }

  /**
   * Clears all parameters. It actually creates a new instance of the DBFunction class without any parameters.
   *
   * @return - a new instance of the DBFunction class without any parameters set
   */
  def clear(): DBFunctionWithPositionedParamsOnly = {
    DBFunctionWithPositionedParamsOnly(functionName, OrderedParams())
  }
}


object DBFunction {

  /**
   * Creates a new instance of the DBFunction class with the given function name without any parameters set.
   *
   * @param functionName  - the name of the function
   * @return              - a new instance of the DBFunction class
   */
  def apply(functionName: String): DBFunctionWithPositionedParamsOnly = {
    DBFunctionWithPositionedParamsOnly(functionName)
  }

  def apply(functionName: String, params: NamedParams): DBFunctionWithNamedParamsToo = {
    DBFunctionWithNamedParamsToo(functionName, OrderedParams(), params)
  }

  def apply(functionName: String, params: OrderedParams): DBFunctionWithPositionedParamsOnly = {
    DBFunctionWithPositionedParamsOnly(functionName, params)
  }

  /**
   * Class that represents a database function call with parameters defined by their position only. It's the default
   * class when creating a new instance of the DBFunction class without any parameters set.
   *
   * @param functionName  - the name of the function
   * @param orderedParams - the list of parameters identified by their position (preceding the named parameters)
   */
  sealed case class DBFunctionWithPositionedParamsOnly private(functionName: String,
                                                               orderedParams: OrderedParams = OrderedParams()
                                                              ) extends DBFunction(functionName, orderedParams, namedParams = NamedParams()) {
    /**
     * Sets a parameter for the function call. It actually creates a new instance of the DBFunction class with the new
     * parameter. The new parameter is the last in the parameter list.
     *
     * @param value  - the value of the parameter
     * @return       - a new instance of the DBFunction class with the new parameter
     */
    def setParam[T: QueryParamType](value: T): DBFunctionWithPositionedParamsOnly = {
      DBFunctionWithPositionedParamsOnly(functionName, orderedParams.add(value))
    }

    /**
     * Sets a parameter to NULL for the function call. It actually creates a new instance of the DBFunction class with
     * the new parameter. The new parameter is the last in the parameter list.
     *
     * @return       - a new instance of the DBFunction class with the new parameter
     */
    @deprecated("Use setParam(NULL)", "balta 0.3.0")
    def setParamNull(): DBFunctionWithPositionedParamsOnly = {
      setParam(QueryParamType.NULL)
    }

  }

  /**
   * Class that represents a database function call with parameters list where the paremeters can be defined by their
   * position (at the beginning of the list) and by their name (for the rest of the list).
   *
   * @param functionName  - the name of the function
   * @param orderedParams - the list of parameters identified by their position (preceding the named parameters)
   * @param namedParams   - the list of parameters identified by their name (following the positioned parameters)
   */
  sealed case class DBFunctionWithNamedParamsToo private(functionName: String,
                                                         orderedParams: OrderedParams = OrderedParams(),
                                                         namedParams: NamedParams = NamedParams()
                                                        ) extends DBFunction(functionName, orderedParams, namedParams)
}
