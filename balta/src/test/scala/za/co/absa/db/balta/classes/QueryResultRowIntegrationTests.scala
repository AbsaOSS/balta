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
import za.co.absa.db.balta.testing.classes.DBTestingConnection

import java.sql.{Date, ResultSetMetaData, Time}
import java.text.SimpleDateFormat
import java.time.{Instant, LocalDateTime, OffsetDateTime}
import java.util.UUID

class QueryResultRowIntegrationTests extends AnyFunSuiteLike with DBTestingConnection{
  private val (tableRows: List[QueryResultRow], metadata: ResultSetMetaData) = DBTable("testing.base_types").all("long_type") { q =>
    (q.toList, q.resultSetMetaData)
  }

  test("fieldNamesFromMetada") {
    val result = QueryResultRow.fieldNamesFromMetadata(metadata)

    val expecedResult = Seq(
      "long_type",
      "boolean_type",
      "char_type",
      "string_type",
      "int_type",
      "double_type",
      "float_type",
      "bigdecimal_type",
      "date_type",
      "time_type",
      "timestamp_type",
      "timestamptz_type",
      "uuid_type",
      "array_int_type")
      .zipWithIndex
      .map(x => (x._1, x._2 + 1))
      .toMap
    assert(result.size == 14)
    assert(result == expecedResult)
  }

  test("getLong") {
    //first row
    assert(tableRows.head.getLong(1).contains(1))
    assert(tableRows.head.getLong("long_type").contains(1))
    assert(tableRows.head.getAs[Long](1).contains(1))
    assert(tableRows.head.getAs[Long]("long_type").contains(1))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getLong(1).isEmpty)
    assert(secondRow.getLong("long_type").isEmpty)
  }

  test("getBoolean") {
    //first row
    assert(tableRows.head.getBoolean(2).contains(true))
    assert(tableRows.head.getBoolean("boolean_type").contains(true))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getBoolean(2).isEmpty)
    assert(secondRow.getBoolean("boolean_type").isEmpty)
  }

  test("getChar") {
    //first row
    assert(tableRows.head.getChar(3).contains('a'))
    assert(tableRows.head.getChar("char_type").contains('a'))
    assert(tableRows.head.getChar(4).contains('h'))
    assert(tableRows.head.getChar("string_type").contains('h'))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getChar(3).isEmpty)
    assert(secondRow.getChar("char_type").isEmpty)
  }

  test("getString") {
    //first row
    assert(tableRows.head.getString(4).contains("hello world"))
    assert(tableRows.head.getString("string_type").contains("hello world"))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getString(2).isEmpty)
    assert(secondRow.getString("string_type").isEmpty)
  }

  test("getInt") {
    //first row
    assert(tableRows.head.getInt(5).contains(257))
    assert(tableRows.head.getInt("int_type").contains(257))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getInt(5).isEmpty)
    assert(secondRow.getInt("int_type").isEmpty)
  }

  test("getDouble") {
    //first row
    assert(tableRows.head.getDouble(6).contains(3.14))
    assert(tableRows.head.getDouble("double_type").contains(3.14))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getDouble(6).isEmpty)
    assert(secondRow.getDouble("double_type").isEmpty)
  }

  test("getFloat") {
    //first row
    assert(tableRows.head.getFloat(7).contains(2.71F))
    assert(tableRows.head.getFloat("float_type").contains(2.71F))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getFloat(7).isEmpty)
    assert(secondRow.getFloat("float_type").isEmpty)
  }

  test("getBigDecimal") {
    //first row
    assert(tableRows.head.getBigDecimal(8).contains(BigDecimal("123456789.0123456789")))
    assert(tableRows.head.getBigDecimal("bigdecimal_type").contains(BigDecimal("123456789.0123456789")))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getBigDecimal(8).isEmpty)
    assert(secondRow.getBigDecimal("bigdecimal_type").isEmpty)
  }

  test("getDate") {
    //first row
    val df = new SimpleDateFormat("yyyy-MM-dd");
    val expected = new Date(df.parse("2022-08-09").getTime)
    assert(tableRows.head.getDate(9).contains(expected))
    assert(tableRows.head.getDate("date_type").contains(expected))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getDate(9).isEmpty)
    assert(secondRow.getDate("date_type").isEmpty)
  }

  test("getTime") {
    //first row
    val df = new SimpleDateFormat("hh:mm:ss")
    val expected = new Time(df.parse("10:12:15").getTime)
    assert(tableRows.head.getTime(10).contains(expected))
    assert(tableRows.head.getTime("time_type").contains(expected))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getTime(10).isEmpty)
    assert(secondRow.getTime("time_type").isEmpty)
  }


  test("getLocalDateTime") {
    //first row
    val timestampString = "2020-06-02T01:00:00"
    val expectedLocalDateTime = LocalDateTime.parse(s"$timestampString")

    assert(tableRows.head.getLocalDateTime(11).contains(expectedLocalDateTime))
    assert(tableRows.head.getLocalDateTime("timestamp_type").contains(expectedLocalDateTime))

    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getLocalDateTime(11).isEmpty)
    assert(secondRow.getLocalDateTime("timestamp_type").isEmpty)
  }

  test("getOffsetDateTime and getInstant") {
    //first row
    val expectedOffsetDateTime = OffsetDateTime.parse("2021-04-03T11:00:00+01:00")
    val expectedInstant = Instant.parse("2021-04-03T10:00:00.00Z")

    val resultOfColumn = tableRows.head.getOffsetDateTime(12).get
    val resultOfColumnLabel = tableRows.head.getOffsetDateTime("timestamptz_type").get
    assert(resultOfColumn.isEqual(expectedOffsetDateTime))
    assert(resultOfColumnLabel.isEqual(expectedOffsetDateTime))
    assert(tableRows.head.getInstant(12).contains(expectedInstant))
    assert(tableRows.head.getInstant("timestamptz_type").contains(expectedInstant))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getOffsetDateTime(12).isEmpty)
    assert(secondRow.getOffsetDateTime("timestamptz_type").isEmpty)
    assert(secondRow.getInstant(12).isEmpty)
    assert(secondRow.getInstant("timestamptz_type").isEmpty)
  }

  test("getUUID") {
    //first row
    val expected = UUID.fromString("090416f8-7da0-4598-844b-63659334e5b6")
    assert(tableRows.head.getUUID(13).contains(expected))
    assert(tableRows.head.getUUID("uuid_type").contains(expected))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getUUID(13).isEmpty)
    assert(secondRow.getUUID("uuid_type").isEmpty)
  }

  test("getVector[Int]") {
    //first row
    val expected = Vector(1, 2, 3)
    assert(tableRows.head.getArray[Int](14).contains(expected))
    assert(tableRows.head.getArray[Int]("array_int_type").contains(expected))
    //second row
    val secondRow = tableRows.tail.head
    assert(secondRow.getArray[Int](14).isEmpty)
    assert(secondRow.getArray[Int]("array_int_type").isEmpty)
  }

}
