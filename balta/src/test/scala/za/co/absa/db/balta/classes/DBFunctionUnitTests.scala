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

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.classes.DBFunction.{DBFunctionWithNamedParamsToo, DBFunctionWithPositionedParamsOnly}

class DBFunctionUnitTests extends AnyFunSuiteLike {

  test("DBFunction.apply creates a positioned-params instance with no params") {
    val fn = DBFunction("my_schema.my_function")
    assert(fn.isInstanceOf[DBFunctionWithPositionedParamsOnly])
    assert(fn.params.isEmpty)
    assert(fn.functionName == "my_schema.my_function")
  }

  test("setParam by position auto-increments the key") {
    val fn = DBFunction("fn")
      .setParam(10)
      .setParam("hello")
      .setParam(true)
    assert(fn.params.size == 3)
    assert(fn.params.keys.toList == List(Left(1), Left(2), Left(3)))
  }

  test("setParam by position returns DBFunctionWithPositionedParamsOnly") {
    val fn = DBFunction("fn").setParam(42)
    assert(fn.isInstanceOf[DBFunctionWithPositionedParamsOnly])
  }

  test("setParam by name creates a named-params instance") {
    val fn = DBFunction("fn").setParam("p_name", "value")
    assert(fn.isInstanceOf[DBFunctionWithNamedParamsToo])
    assert(fn.params.size == 1)
    assert(fn.params.keys.head == Right("p_name"))
  }

  test("mixing positioned and named params preserves order") {
    val fn = DBFunction("fn")
      .setParam(1)
      .setParam(2)
      .setParam("named", "val")
    assert(fn.params.size == 3)
    assert(fn.params.keys.toList == List(Left(1), Left(2), Right("named")))
  }

  test("clear resets params to empty") {
    val fn = DBFunction("fn").setParam(1).setParam(2).clear()
    assert(fn.params.isEmpty)
    assert(fn.functionName == "fn")
  }

  test("clear returns DBFunctionWithPositionedParamsOnly") {
    val fn = DBFunction("fn").setParam("p", "v").clear()
    assert(fn.isInstanceOf[DBFunctionWithPositionedParamsOnly])
  }

  test("setParamNull by name adds NULL param with named key") {
    val fn = DBFunction("fn").setParamNull("p_null")
    assert(fn.params.size == 1)
    assert(fn.params.keys.head == Right("p_null"))
    assert(fn.params.values.head.sqlEntry == "NULL")
    assert(fn.params.values.head.equalityOperator == "IS")
  }

  test("setParamNull by position appends NULL as next index") {
    val fn = DBFunction("fn").setParam(10).setParamNull()
    assert(fn.params.size == 2)
    assert(fn.params.keys.toList == List(Left(1), Left(2)))
    assert(fn.params.values.toList.last.sqlEntry == "NULL")
    assert(fn.params.values.toList.last.equalityOperator == "IS")
  }
}
