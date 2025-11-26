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

import java.sql.{Date, PreparedStatement, Time}
import java.util.UUID
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.Instant
import java.time.ZoneOffset
import za.co.absa.db.balta.typeclasses.QueryParamValue.{SimpleQueryParamValue, ObjectQueryParamValue}

trait QueryParamType[T] {
  def toQueryParamValue(value: T): QueryParamValue
}

object QueryParamType {

  implicit object SQLParamBoolean extends QueryParamType[Boolean] {
    override def toQueryParamValue(value: Boolean): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setBoolean(position, value)})
  }

  implicit object SQLParamInt extends QueryParamType[Int] {
    override def toQueryParamValue(value: Int): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setInt(position, value)})
  }

  implicit object SQLParamLong extends QueryParamType[Long] {
    override def toQueryParamValue(value: Long): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setLong(position, value)})
  }

  implicit object SQLParamString extends QueryParamType[String] {
    override def toQueryParamValue(value: String): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setString(position, value)})
  }

  implicit object SQLParamDouble extends QueryParamType[Double] {
    override def toQueryParamValue(value: Double): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setDouble(position, value)})
  }

  implicit object SQLParamFloat extends QueryParamType[Float] {
    override def toQueryParamValue(value: Float): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setFloat(position, value)})
  }

  implicit object SQLParamBigDecimal extends QueryParamType[BigDecimal] {
    override def toQueryParamValue(value: BigDecimal): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setBigDecimal(position, value.bigDecimal)})
  }

  implicit object SQLParamChar extends QueryParamType[Char] {
    override def toQueryParamValue(value: Char): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setString(position, value.toString)})
  }

  implicit object SQLParamInstant extends QueryParamType[Instant] {
    override def toQueryParamValue(value: Instant): QueryParamValue = new ObjectQueryParamValue(OffsetDateTime.ofInstant(value, ZoneOffset.UTC))
  }

  implicit object SQLParamOffsetDateTime extends QueryParamType[OffsetDateTime] {
    override def toQueryParamValue(value: OffsetDateTime): QueryParamValue = new ObjectQueryParamValue(value)
  }

  implicit object SQLParamLocalTime extends QueryParamType[LocalTime] {
    override def toQueryParamValue(value: LocalTime): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setTime(position, Time.valueOf(value))})
  }

  implicit object SQLParamLocalDate extends QueryParamType[LocalDate] {
    override def toQueryParamValue(value: LocalDate): QueryParamValue = new SimpleQueryParamValue((prep: PreparedStatement, position: Int) => {prep.setDate(position, Date.valueOf(value))})
  }

  implicit object SQLParamUUID extends QueryParamType[UUID] {
    override def toQueryParamValue(value: UUID): QueryParamValue = new ObjectQueryParamValue(value)
  }

  object NULL

  implicit object SQLParamNull extends QueryParamType[NULL.type] {
    override def toQueryParamValue(value: NULL.type): QueryParamValue = QueryParamValue.NullParamValue
  }

}
