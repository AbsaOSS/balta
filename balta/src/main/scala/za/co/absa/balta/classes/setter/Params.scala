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

package za.co.absa.balta.classes.setter

import scala.collection.immutable.ListMap

/**
 * This class represents a list of parameters for a prepared statement. It cannot be instantiated directly, only via
 * provided factory methods.
 *
 * @param items - a list of parameter values, eventually with their names or positions
 */
sealed abstract class Params private(items: ListMap[String, SetterFnc]) {
  def keys: Option[List[String]]
  def setters: List[SetterFnc] = items.values.toList

  def apply(paramName: String): SetterFnc = {
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
  def add[T: AllowedParamTypes](paramName: String, value: T): NamedParams = {
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
    new NamedParams().addNull(paramName)
  }

  /**
   * This is a factory method for creating a list of ordered parameters consisting of one parameter identified by its
   * position.
   *
   * @param value - the value of the parameter
   * @tparam T    - the type of the parameter value
   * @return      - a list parameters to be used in an SQL prepared statement
   */
  def add[T: AllowedParamTypes](value: T): OrderedParams = {
    new OrderedParams().add(value)
  }

  /**
   * This is a factory method for creating a list of ordered parameters consisting of one parameter identified by its
   * position. The parameter value is NULL.
   *
   * @tparam T - the type of the parameter value
   * @return   - a list parameters to be used in an SQL prepared statement
   */
  def addNull[T: AllowedParamTypes](): OrderedParams = {
    new OrderedParams().addNull()
  }

  /**
   * This is a class representing a list of named parameters.
   *
   * @param items - a map of parameter names and their values
   */
  sealed class NamedParams private[setter](items: ListMap[String, SetterFnc] = ListMap.empty) extends Params(items) {
    /**
     * This method adds a new parameter to the list. It actually creates a new list with the new parameter added.
     *
     * @param paramName - the name of the parameter
     * @param value     - the value of the parameter
     * @tparam T        - the type of the parameter value
     * @return          - a list parameters to be used in an SQL prepared statement
     */
    def add[T: AllowedParamTypes](paramName: String, value: T): NamedParams = {
      val setter = SetterFnc.createSetterFnc(value)
      new NamedParams(items + (paramName -> setter)) // TODO https://github.com/AbsaOSS/balta/issues/1
    }

    /**
     * This method adds a new parameter to the list. It actually creates a new list with the new parameter added.
     * The parameter value is NULL.
     *
     * @param paramName - the name of the parameter
     * @return          - a list parameters to be used in an SQL prepared statement
     */
    def addNull(paramName: String): NamedParams = {
      val setter = SetterFnc.nullSetterFnc
      new NamedParams(items + (paramName -> setter)) // TODO https://github.com/AbsaOSS/balta/issues/1
    }

    def pairs: List[(String, SetterFnc)] = items.toList

    override def keys: Option[List[String]] = Some(items.keys.toList)
  }

  /**
   * This is a class representing a list of parameters represented by their position
   *
   * @param items - a list of parameters identified by their positions
   */
  sealed class OrderedParams private[setter](items: ListMap[String, SetterFnc] = ListMap.empty) extends Params(items) {
    /**
     * This method adds a new parameter to the end of the list. It actually creates a new list with the new parameter added.
     *
     * @param value - the value of the parameter
     * @tparam T    - the type of the parameter value
     * @return      - a list parameters to be used in an SQL prepared statement
     */
    def add[T: AllowedParamTypes](value: T): OrderedParams = {
      val key = items.size.toString
      val setter = SetterFnc.createSetterFnc(value)
      new OrderedParams(items + (key -> setter))
    }

    /**
     * This method adds a new parameter to the end of the list. It actually creates a new list with the new parameter added.
     * The parameter value is NULL.
     *
     * @tparam T - the type of the parameter value
     * @return   - a list parameters to be used in an SQL prepared statement
     */
    def addNull[T: AllowedParamTypes](): OrderedParams = {
      val key = items.size.toString
      val setter = SetterFnc.nullSetterFnc
      new OrderedParams(items + (key -> setter))
    }

    override val keys: Option[List[String]] = None
  }
}
