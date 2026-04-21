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

import java.time.{Instant, LocalDate, LocalTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.testing.RecordingPreparedStatement

class QueryParamTypeUnitTests extends AnyFunSuiteLike {

  test("QueryParamBoolean sets boolean on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamBoolean.toQueryParamValue(true)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setBoolean", 1, true))
  }

  test("QueryParamInt sets int on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamInt.toQueryParamValue(42)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setInt", 1, 42))
  }

  test("QueryParamLong sets long on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamLong.toQueryParamValue(123L)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setLong", 1, 123L))
  }

  test("QueryParamString sets string on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamString.toQueryParamValue("hello")
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setString", 1, "hello"))
  }

  test("QueryParamDouble sets double on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamDouble.toQueryParamValue(3.14)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setDouble", 1, 3.14))
  }

  test("QueryParamFloat sets float on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamFloat.toQueryParamValue(2.5f)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setFloat", 1, 2.5f))
  }

  test("QueryParamBigDecimal sets BigDecimal on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val bd = BigDecimal("99.99")
    val qpv = QueryParamType.QueryParamBigDecimal.toQueryParamValue(bd)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setBigDecimal", 1, bd.bigDecimal))
  }

  test("QueryParamChar sets string representation on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val qpv = QueryParamType.QueryParamChar.toQueryParamValue('A')
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setString", 1, "A"))
  }

  test("QueryParamInstant sets OffsetDateTime via setObject") {
    val ps = new RecordingPreparedStatement
    val instant = Instant.parse("2024-01-15T10:30:00Z")
    val qpv = QueryParamType.QueryParamInstant.toQueryParamValue(instant)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setObject", 1, OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)))
  }

  test("QueryParamOffsetDateTime sets object on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val odt = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC)
    val qpv = QueryParamType.QueryParamOffsetDateTime.toQueryParamValue(odt)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setObject", 1, odt))
  }

  test("QueryParamLocalTime sets Time on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val lt = LocalTime.of(14, 30, 0)
    val qpv = QueryParamType.QueryParamLocalTime.toQueryParamValue(lt)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setTime", 1, java.sql.Time.valueOf(lt)))
  }

  test("QueryParamLocalDate sets Date on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val ld = LocalDate.of(2024, 6, 15)
    val qpv = QueryParamType.QueryParamLocalDate.toQueryParamValue(ld)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setDate", 1, java.sql.Date.valueOf(ld)))
  }

  test("QueryParamUUID sets object on PreparedStatement") {
    val ps = new RecordingPreparedStatement
    val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    val qpv = QueryParamType.QueryParamUUID.toQueryParamValue(uuid)
    qpv.assign.get(ps, 1)
    assert(ps.lastCall == ("setObject", 1, uuid))
  }

  test("QueryParamNull returns NullParamValue with no assign function") {
    val qpv = QueryParamType.QueryParamNull.toQueryParamValue(QueryParamType.NULL)
    assert(qpv.assign.isEmpty)
    assert(qpv.sqlEntry == "NULL")
    assert(qpv.equalityOperator == "IS")
  }

}
