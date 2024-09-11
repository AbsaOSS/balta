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

import DBFunction.{DBFunctionWithNamedParamsToo, DBFunctionWithPositionedParamsOnly, ParamsMap}
import za.co.absa.db.balta.classes.setter.{AllowedParamTypes, SetterFnc}

import scala.collection.immutable.ListMap

/**
 * A class that represents a database function call. It can be used to execute a function and verify the result.
 * THere are two implementations of this class:
 * - DBFunctionWithPositionedParamsOnly - the parameters are defined by their position solely
 * - DBFunctionWithNamedParamsToo - there can be parameters defined by their position and and others defined by their
 * name; note that the position defined parameters can be added only at the beginning of the parameter list
 *
 * @param functionName  - the name of the function
 * @param params        - the list of parameters
 */
sealed abstract class DBFunction private(functionName: String,
                                         params: ParamsMap) extends DBQuerySupport {

  private def sql(orderBy: String): String = {
    val paramEntries = params.map{case(key, setterFnc) =>
      key match {
        case Left(_) => setterFnc.sqlEntry
        case Right(name) => s"$name := ${setterFnc.sqlEntry}" // TODO https://github.com/AbsaOSS/balta/issues/2
      }
    }
    val paramsLine = paramEntries.mkString(",")
    s"SELECT * FROM $functionName($paramsLine) $orderBy"
  }

  /**
   * Executes the function.
   * @param connection  - the database connection
   */
  def execute()(implicit connection: DBConnection): Unit = {
    execute("")(_ => Unit)
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
   * @param verify      - the function that verifies the result
   * @param connection  - the database connection
   * @tparam R          - the type of the result that is returned by the verify function
   * @return            - the result of the verify function
   */
  def execute[R](orderBy: String)(verify: QueryResult => R /* Assertion */)(implicit connection: DBConnection): R = {
    val orderByPart = if (orderBy.nonEmpty) {s"ORDER BY $orderBy"} else ""
    runQuery(sql(orderByPart), params.values.toList)(verify)
  }

  /**
    * Sets a parameter for the function call. It actually creates a new instance of the DBFunction class with the new
    * parameter.
    *
    * @param paramName  - the name of the parameter to set
    * @param value      - the value of the parameter
    * @return           - a new instance of the DBFunction class with the new parameter
    */
  def setParam[T: AllowedParamTypes](paramName: String, value: T): DBFunctionWithNamedParamsToo = {
    val key = Right(paramName) // TODO normalization TODO https://github.com/AbsaOSS/balta/issues/1
    val fnc = SetterFnc.createSetterFnc(value)
    DBFunctionWithNamedParamsToo(functionName, params + (key -> fnc))
  }

  /**
    * Sets a parameter to NULL for the function call. It actually creates a new instance of the DBFunction class with
    * the new parameter.
    *
    * @param paramName  - the name of the parameter to set
    * @return           - a new instance of the DBFunction class with the new parameter
    */
  def setParamNull(paramName: String): DBFunctionWithPositionedParamsOnly = {
    val key = Right(paramName) // TODO normalization TODO https://github.com/AbsaOSS/balta/issues/1
    val fnc = SetterFnc.nullSetterFnc
    DBFunctionWithPositionedParamsOnly(functionName, params + (key -> fnc))
  }

  /**
   * Clears all parameters. It actually creates a new instance of the DBFunction class without any parameters.
   *
   * @return - a new instance of the DBFunction class without any parameters set
   */
  def clear(): DBFunctionWithPositionedParamsOnly = {
    DBFunctionWithPositionedParamsOnly(functionName)
  }
}


object DBFunction {

  type ParamsMap = ListMap[Either[Int, String], SetterFnc]

  /**
   * Creates a new instance of the DBFunction class with the given function name without any parameters set.
   *
   * @param functionName  - the name of the function
   * @return              - a new instance of the DBFunction class
   */
  def apply(functionName: String): DBFunctionWithPositionedParamsOnly = {
    DBFunctionWithPositionedParamsOnly(functionName)
  }

  /**
   * Class that represents a database function call with parameters defined by their position only. It's the default
   * class when creating a new instance of the DBFunction class without any parameters set.
   *
   * @param functionName  - the name of the function
   * @param params        - the list of parameters
   */
  sealed case class DBFunctionWithPositionedParamsOnly private(functionName: String,
                                                               params: ParamsMap = ListMap.empty
                                                              ) extends DBFunction(functionName, params) {
    /**
     * Sets a parameter for the function call. It actually creates a new instance of the DBFunction class with the new
     * parameter. The new parameter is the last in the parameter list.
     *
     * @param value  - the value of the parameter
     * @return       - a new instance of the DBFunction class with the new parameter
     */
    def setParam[T: AllowedParamTypes](value: T): DBFunctionWithPositionedParamsOnly = {
      val key = Left(params.size + 1)
      val fnc = SetterFnc.createSetterFnc(value)
      DBFunctionWithPositionedParamsOnly(functionName, params + (key -> fnc))
    }

    /**
     * Sets a parameter to NULL for the function call. It actually creates a new instance of the DBFunction class with
     * the new parameter. The new parameter is the last in the parameter list.
     *
     * @return       - a new instance of the DBFunction class with the new parameter
     */
    def setParamNull(): DBFunctionWithPositionedParamsOnly = {
      val key = Left(params.size + 1)
      val fnc = SetterFnc.nullSetterFnc
      DBFunctionWithPositionedParamsOnly(functionName, params + (key -> fnc))
    }

  }

  /**
   * Class that represents a database function call with parameters list where the paremeters can be defined by their
   * position (at the beginning of the list) and by their name (for the rest of the list).
   *
   * @param functionName  - the name of the function
   * @param params        - the list of parameters
   */
  sealed case class DBFunctionWithNamedParamsToo private(functionName: String,
                                                         params: ParamsMap = ListMap.empty
                                                        ) extends DBFunction(functionName, params)
}
