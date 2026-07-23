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

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.testing.classes.RecordingPreparedStatement
import za.co.absa.db.balta.typeclasses.QueryParamValue._

class QueryParamValueUnitTests extends AnyFunSuiteLike {

  test("ObjectQueryParamValue.assign calls setObject with the wrapped object") {
    val obj = java.lang.Integer.valueOf(42)
    val qpv = new ObjectQueryParamValue(obj)
    assert(qpv.assign.isDefined)
    val ps = new RecordingPreparedStatement
    qpv.assign.get(ps, 3)
    assert(ps.lastCall == ("setObject", 3, obj))
  }

  test("ObjectQueryParamValue uses default sqlEntry and equalityOperator") {
    val qpv = new ObjectQueryParamValue("test")
    assert(qpv.sqlEntry == "?")
    assert(qpv.equalityOperator == "=")
  }

  test("SimpleQueryParamValue.assign invokes the provided function") {
    var called = false
    val qpv = new SimpleQueryParamValue((ps, pos) => { called = true })
    assert(qpv.assign.isDefined)
    val ps = new RecordingPreparedStatement
    qpv.assign.get(ps, 1)
    assert(called)
  }

  test("SimpleQueryParamValue uses default sqlEntry and equalityOperator") {
    val qpv = new SimpleQueryParamValue((_, _) => ())
    assert(qpv.sqlEntry == "?")
    assert(qpv.equalityOperator == "=")
  }

  test("NullParamValue.assign is None") {
    assert(NullParamValue.assign.isEmpty)
  }

  test("NullParamValue.sqlEntry is NULL") {
    assert(NullParamValue.sqlEntry == "NULL")
  }

  test("NullParamValue.equalityOperator is IS") {
    assert(NullParamValue.equalityOperator == "IS")
  }

}
