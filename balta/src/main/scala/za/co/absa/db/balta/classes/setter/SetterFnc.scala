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

package za.co.absa.db.balta.classes.setter

import java.sql.{Date, PreparedStatement, Time, Timestamp, Types => SqlTypes}
import java.util.UUID
import org.postgresql.util.PGobject
import za.co.absa.db.balta.classes.simple.JsonBString

import java.time.{Instant, LocalDate, LocalTime, OffsetDateTime, ZoneId, ZoneOffset}

/**
 * This is a trait representing a function that sets a value in a prepared statement.
 */
abstract class SetterFnc extends ((PreparedStatement, Int) => Unit) {
  def sqlEntry: String = "?"
}

object SetterFnc {

  /**
   * This method creates a `SetterFnc` for a given value.
   *
   * @param value - the value to be set
   * @tparam T    - the type of the value
   * @return      - a function that sets the given value in a prepared statement
   */
  def createSetterFnc[T: AllowedParamTypes](value: T): SetterFnc = {
    value match {
      case b: Boolean                  => simple((prep: PreparedStatement, position: Int) => {prep.setBoolean(position, b)})
      case i: Int                      => simple((prep: PreparedStatement, position: Int) => {prep.setInt(position, i)})
      case l: Long                     => simple((prep: PreparedStatement, position: Int) => {prep.setLong(position, l)})
      case d: Double                   => simple((prep: PreparedStatement, position: Int) => {prep.setDouble(position, d)})
      case f: Float                    => simple((prep: PreparedStatement, position: Int) => {prep.setFloat(position, f)})
      case bd: BigDecimal              => simple((prep: PreparedStatement, position: Int) => {prep.setBigDecimal(position, bd.bigDecimal)})
      case ch: Char                    => simple((prep: PreparedStatement, position: Int) => {prep.setString(position, ch.toString)})
      case s: String                   => simple((prep: PreparedStatement, position: Int) => {prep.setString(position, s)})
      case u: UUID                     => new UuidSetterFnc(u)
      case js: JsonBString             => new JsonBSetterFnc(js)
      case i: Instant                  => simple((prep: PreparedStatement, position: Int) => {prep.setObject(position, OffsetDateTime.ofInstant(i, ZoneOffset.UTC))})
      case ts: OffsetDateTime          => simple((prep: PreparedStatement, position: Int) => {prep.setObject(position, ts)})
      case lt: LocalTime               => simple((prep: PreparedStatement, position: Int) => {prep.setTime(position, Time.valueOf(lt))})
      case ld: LocalDate               => simple((prep: PreparedStatement, position: Int) => {prep.setDate(position, Date.valueOf(ld))})
      case CustomDBType(value, dbType) => new CustomDBTypeSetterFnc(value, dbType)
    }
  }

  val nullSetterFnc: SetterFnc = simple((prep: PreparedStatement, position: Int) => prep.setNull(position, SqlTypes.NULL))

  private [this] def simple(body: (PreparedStatement, Int) => Unit): SetterFnc = {
    new SetterFnc {
      override def apply(prep: PreparedStatement, position: Int): Unit = body(prep, position)
    }
  }

  private class UuidSetterFnc(value: UUID) extends SetterFnc {
    def apply(prep: PreparedStatement, position: Int): Unit = {
      prep.setObject(position, value)
    }
  }

  private class JsonBSetterFnc(value: JsonBString) extends SetterFnc {
    private val jsonObject = new PGobject()
    jsonObject.setType("jsonb")
    jsonObject.setValue(value.value)

    def apply(prep: PreparedStatement, position: Int): Unit = {
      prep.setObject(position, jsonObject)
    }
  }

  private class CustomDBTypeSetterFnc(value: String, dbType: String) extends SetterFnc {
    def apply(prep: PreparedStatement, position: Int): Unit = {
      prep.setString(position, value)
    }

    override def sqlEntry: String = s"?::$dbType"
  }
}
