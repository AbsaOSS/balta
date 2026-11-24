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

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.typeclasses.QueryParamType

class ParamsUnitTests extends AnyFunSuiteLike {

  test("NamedParams add creates a single-entry list with correct key") {
    val params = Params.add("name", "Alice")
    assert(params.size == 1)
    assert(params.keys.contains(List("name")))
  }

  test("NamedParams add preserves insertion order across multiple adds") {
    val params = Params.add("a", 1).add("b", 2).add("c", 3)
    assert(params.size == 3)
    assert(params.keys.contains(List("a", "b", "c")))
  }

  test("NamedParams values returns parameter values in insertion order") {
    val params = Params.add("x", 10).add("y", 20)
    val values = params.values
    assert(values.size == 2)
    assert(values.head.sqlEntry == "?")
    assert(values(1).sqlEntry == "?")
  }

  test("NamedParams addNull creates a NULL parameter") {
    val params = Params.addNull("missing")
    assert(params.size == 1)
    assert(params("missing").sqlEntry == "NULL")
    assert(params("missing").equalityOperator == "IS")
  }

  test("NamedParams instance addNull appends NULL parameter") {
    val params = Params.add("id", 42).add("missing", QueryParamType.NULL)
    assert(params.size == 2)
    assert(params("missing").sqlEntry == "NULL")
    assert(params("missing").equalityOperator == "IS")
  }

  test("NamedParams pairs returns key-value list") {
    val params = Params.add("k1", "v1").add("k2", "v2")
    val pairs = params.pairs
    assert(pairs.size == 2)
    assert(pairs.head._1 == "k1")
    assert(pairs(1)._1 == "k2")
  }

  test("NamedParams apply retrieves value by parameter name") {
    val params = Params.add("id", 42)
    val value = params("id")
    assert(value.sqlEntry == "?")
  }

  test("NamedParams treats parameter names case-insensitively") {
    val params = Params.add("Param1", 1).add("PARAM1", 2)
    assert(params.size == 1)
    assert(params.keys.contains(List("param1")))
    assert(params("param1").sqlEntry == "?")
    assert(params("PARAM1").sqlEntry == "?")
  }

  test("NamedParams apply throws NoSuchElementException for missing key") {
    val params = Params.add("id", 42)
    assertThrows[NoSuchElementException] {
      params("nonexistent")
    }
  }

  test("OrderedParams add creates a single-entry list") {
    val params = Params.add(100)
    assert(params.size == 1)
    assert(params.keys.isEmpty)
  }

  test("OrderedParams add preserves order across multiple adds") {
    val params = Params.add(1).add(2).add(3)
    assert(params.size == 3)
    assert(params.values.size == 3)
  }

  test("OrderedParams addNull creates a NULL parameter") {
    val params = Params.addNull[QueryParamType.NULL.type]()
    assert(params.size == 1)
    assert(params.values.head.sqlEntry == "NULL")
  }

  test("OrderedParams instance addNull appends NULL parameter") {
    val params = Params.add(1).add(QueryParamType.NULL)
    assert(params.size == 2)
    assert(params.values.last.sqlEntry == "NULL")
    assert(params.values.last.equalityOperator == "IS")
  }

  test("OrderedParams keys returns None") {
    val params = Params.add("hello")
    assert(params.keys.isEmpty)
  }
}
