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

package za.co.absa.db.balta.typeclasses
// jacoco-touch: simulate change

import QueryParamValue.AssignFunc
import java.sql.PreparedStatement

trait QueryParamValue {
  def assign: Option[AssignFunc]
  def sqlEntry: String = "?"
  def equalityOperator: String = "="
}

object QueryParamValue {
  type AssignFunc = (PreparedStatement, Int) => Unit

  class ObjectQueryParamValue(obj: Object) extends QueryParamValue {
    private def assignFunc(prep: PreparedStatement, position: Int): Unit = {
      prep.setObject(position, obj)
    }
    override val assign: Option[AssignFunc] = Some(assignFunc)
  }

  class SimpleQueryParamValue(assignFunc: AssignFunc) extends QueryParamValue {
    override val assign: Option[AssignFunc] = Some(assignFunc)
  }

  object NullParamValue extends QueryParamValue {
    override val assign: Option[AssignFunc] = None
    override val sqlEntry: String = "NULL"
    override val equalityOperator: String = "IS"
  }

}
