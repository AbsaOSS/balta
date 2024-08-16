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

package za.co.absa.db.balta.implicits

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.classes.{DBTable, QueryResultRow}
import za.co.absa.db.balta.testing.classes.DBTestingConnection

import java.sql.ResultSetMetaData
import Postgres.PostgresRow
import za.co.absa.db.balta.classes.simple.SimpleJsonString

class PostgresRowIntegrationTests extends AnyFunSuiteLike  with DBTestingConnection{
  private val (tableRows: List[QueryResultRow], metadata: ResultSetMetaData) = DBTable("testing.pg_types").all("id") { q =>
    (q.toList, q.resultSetMetaData)
  }

  test("fieldNamesFromMetada") {
    val result = QueryResultRow.fieldNamesFromMetdata(metadata)

    val expecedResult = Seq(
      "id",
      "json_type",
      "jsonb_type",
      "array_of_json_type"
    )
      .zipWithIndex
      .map(x => (x._1, x._2 + 1))
      .toMap
    assert(result.size == 4)
    assert(result == expecedResult)
  }

  test("getSimpleJson") {
    //first row
    val expectedJson = SimpleJsonString("""{"a": 1, "b": "Hello"}""")
    val expectedJsonB = SimpleJsonString("""{"Hello": "World"}""")
    assert(tableRows.head.getSimpleJsonString(2).contains(expectedJson))
    assert(tableRows.head.getSimpleJsonString("json_type").contains(expectedJson))
    assert(tableRows.head.getSimpleJsonString(3).contains(expectedJsonB))
    assert(tableRows.head.getSimpleJsonString("jsonb_type").contains(expectedJsonB))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getSimpleJsonString(2).isEmpty)
    assert(secondRow.getSimpleJsonString("json_type").isEmpty)
    assert(secondRow.getSimpleJsonString(3).isEmpty)
    assert(secondRow.getSimpleJsonString("jsonb_type").isEmpty)
  }

  test("getArrayJson") {
    //first row
    val expected = Vector(
      SimpleJsonString("""{"a": 2, "body": "Hold the line!"}"""),
      SimpleJsonString("""{"a": 3, "body": ""}"""),
      SimpleJsonString("""{"a": 4}""")
    )
    assert(tableRows.head.getSJSArray(4).get == (expected))
    assert(tableRows.head.getSJSArray("array_of_json_type").contains(expected))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getSJSArray(4).isEmpty)
    assert(secondRow.getSJSArray("array_of_json_type").isEmpty)
  }
}
