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

package za.co.absa.db.balta.classes.inner

import scala.collection.immutable.ListMap
import za.co.absa.db.balta.typeclasses.{QueryParamValue, QueryParamType}

/**
 * This class represents a list of parameters for a prepared statement. It cannot be instantiated directly, only via
 * provided factory methods.
 *
 * @param items - a list of parameter values, eventually with their names or positions
 */
sealed abstract class Params private(items: ListMap[String, QueryParamValue]) {
  def keys: Option[List[String]]
  def values: List[QueryParamValue] = items.values.toList

  def apply(paramName: String): QueryParamValue = {
    items(paramName)
  }

  def size: Int = items.size

}
object Params {

  /**
   * This is a factory method for creating a list of named parameters consisting of one parameter identified by its
   * name.
   *
   * @param paramName - the name of the parameter
   * @param value     - the value of the parameter
   * @tparam T        - the type of the parameter value
   * @return          - a list parameters to be used in an SQL prepared statement
   */
  def add[T: QueryParamType](paramName: String, value: T): NamedParams = {
    new NamedParams().add(paramName, value)
  }

  /**
   * This is a factory method for creating a list of named parameters consisting of one parameter identified by its
   * name. The parameter value is NULL.
   *
   * @param paramName - the name of the parameter
   * @return          - a list parameters to be used in an SQL prepared statement
   */
  def addNull(paramName: String): NamedParams = {
    new NamedParams().add(paramName, QueryParamType.NULL)
  }

  /**
   * This is a factory method for creating a list of ordered parameters consisting of one parameter identified by its
   * position.
   *
   * @param value - the value of the parameter
   * @tparam T    - the type of the parameter value
   * @return      - a list parameters to be used in an SQL prepared statement
   */
  def add[T: QueryParamType](value: T): OrderedParams = {
    new OrderedParams().add(value)
  }

  /**
   * This is a factory method for creating a list of ordered parameters consisting of one parameter identified by its
   * position. The parameter value is NULL.
   *
   * @tparam T - the type of the parameter value
   * @return   - a list parameters to be used in an SQL prepared statement
   */
  def addNull[T: QueryParamType](): OrderedParams = {
    new OrderedParams().add(QueryParamType.NULL)
  }

  /**
   * This is a class representing a list of named parameters.
   *
   * @param items - a map of parameter names and their values
   */
  sealed class NamedParams private[Params](items: ListMap[String, QueryParamValue] = ListMap.empty) extends Params(items) {
    /**
     * This method adds a new parameter to the list. It actually creates a new list with the new parameter added.
     *
     * @param paramName - the name of the parameter
     * @param value     - the value of the parameter
     * @tparam T        - the type of the parameter value
     * @return          - a list parameters to be used in an SQL prepared statement
     */
    def add[T: QueryParamType](paramName: String, value: T): NamedParams = {
      val queryValue = implicitly[QueryParamType[T]].toQueryParamValue(value)
      new NamedParams(items + (paramName -> queryValue)) // TODO https://github.com/AbsaOSS/balta/issues/1
    }

    /**
     * This method adds a new parameter to the list. It actually creates a new list with the new parameter added.
     * The parameter value is NULL.
     *
     * @param paramName - the name of the parameter
     * @return          - a list parameters to be used in an SQL prepared statement
     */
    @deprecated("Use add(NULL)", "balta 0.3.0")
    def addNull(paramName: String): NamedParams = {
      add(paramName, QueryParamType.NULL)
    }

    def pairs: List[(String, QueryParamValue)] = items.toList

    override def keys: Option[List[String]] = Some(items.keys.toList)
  }

  /**
   * This is a class representing a list of parameters represented by their position
   *
   * @param items - a list of parameters identified by their positions
   */
  sealed class OrderedParams private[Params](items: ListMap[String, QueryParamValue] = ListMap.empty) extends Params(items) {
    /**
     * This method adds a new parameter to the end of the list. It actually creates a new list with the new parameter added.
     *
     * @param value - the value of the parameter
     * @tparam T    - the type of the parameter value
     * @return      - a list parameters to be used in an SQL prepared statement
     */
    def add[T: QueryParamType](value: T): OrderedParams = {
      val key = items.size.toString
      val queryValue = implicitly[QueryParamType[T]].toQueryParamValue(value)
      new OrderedParams(items + (key -> queryValue))
    }

    /**
     * This method adds a new parameter to the end of the list. It actually creates a new list with the new parameter added.
     * The parameter value is NULL.
     *
     * @tparam T - the type of the parameter value
     * @return   - a list parameters to be used in an SQL prepared statement
     */
    @deprecated("Use add(NULL)", "balta 0.3.0")
    def addNull[T: QueryParamType](): OrderedParams = {
      add(QueryParamType.NULL)
    }

    override val keys: Option[List[String]] = None
  }
}

