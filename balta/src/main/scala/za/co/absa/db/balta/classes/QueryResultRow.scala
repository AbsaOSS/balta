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

import QueryResultRow._
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PGobject

import java.sql
import java.sql.{Date, ResultSet, ResultSetMetaData, SQLException, Time, Types}
import java.time.{Instant, LocalDateTime, LocalTime, OffsetDateTime, OffsetTime}
import java.util.UUID
import scala.util.Try

/**
 * This is a row of a query result. It allows to safely extract values from the row by column name.
 *
 * @param rowNumber   - the number of the row in the result set
 * @param fields      - the values of the row
 * @param columnNames - the names of the columns
 */
class QueryResultRow private[classes](val rowNumber: Int,
                                      private val fields: Vector[Option[Object]],
                                      private val columnNames: FieldNames) {

  def columnCount: Int = fields.length
  def columnNumber(columnLabel: String): Int = columnNames(columnLabel.toLowerCase)

  def apply(column: Int): Option[Object] = fields(column - 1)
  def apply(columnLabel: String): Option[Object] = apply(columnNumber(columnLabel))

  def getAs[T](column: Int, transformer: TransformerFnc[T]): Option[T] = apply(column).map(transformer)
  def getAs[T](column: Int): Option[T] = apply(column)map(_.asInstanceOf[T])

  def getAs[T](columnLabel: String, transformer: TransformerFnc[T]): Option[T] = getAs(columnNumber(columnLabel), transformer)
  def getAs[T](columnLabel: String): Option[T] = apply(columnNumber(columnLabel)).map(_.asInstanceOf[T])

  def getBoolean(column: Int): Option[Boolean] = getAs(column: Int, item => item.asInstanceOf[Boolean])
  def getBoolean(columnLabel: String): Option[Boolean] = getBoolean(columnNumber(columnLabel))

  def getChar(column: Int): Option[Char] = {
    getString(column) match {
      case Some(value) =>
        if (value.isEmpty) None else Some(value.charAt(0))
      case None =>
        None
    }
  }
  def getChar(columnLabel: String): Option[Char] = getChar(columnNumber(columnLabel))


  def getString(column: Int): Option[String] = getAs(column: Int, _.asInstanceOf[String])
  def getString(columnLabel: String): Option[String] = getString(columnNumber(columnLabel))

  def getInt(column: Int): Option[Int] = getAs(column: Int, _.asInstanceOf[Int])
  def getInt(columnLabel: String): Option[Int] = getInt(columnNumber(columnLabel))

  def getLong(column: Int): Option[Long] = getAs(column: Int, _.asInstanceOf[Long])
  def getLong(columnLabel: String): Option[Long] = getLong(columnNumber(columnLabel))

  def getDouble(column: Int): Option[Double] = getAs(column: Int, _.asInstanceOf[Double])
  def getDouble(columnLabel: String): Option[Double] = getDouble(columnNumber(columnLabel))

  def getFloat(column: Int): Option[Float] = getAs(column: Int, _.asInstanceOf[Float])
  def getFloat(columnLabel: String): Option[Float] = getFloat(columnNumber(columnLabel))

  def getBigDecimal(column: Int): Option[BigDecimal] = getAs(column: Int, _.asInstanceOf[java.math.BigDecimal]).map(scala.math.BigDecimal(_))
  def getBigDecimal(columnLabel: String): Option[BigDecimal] = getBigDecimal(columnNumber(columnLabel))

  def getTime(column: Int): Option[Time] = getAs(column: Int, _.asInstanceOf[Time])
  def getTime(columnLabel: String): Option[Time] = getTime(columnNumber(columnLabel))

  def getDate(column: Int): Option[Date] = getAs(column: Int, _.asInstanceOf[Date])
  def getDate(columnLabel: String): Option[Date] = getDate(columnNumber(columnLabel))

  def getLocalDateTime(column: Int): Option[LocalDateTime] = getAs(column: Int, _.asInstanceOf[LocalDateTime])
  def getLocalDateTime(columnLabel: String): Option[LocalDateTime] = getLocalDateTime(columnNumber(columnLabel))

  def getOffsetDateTime(column: Int): Option[OffsetDateTime] = getAs(column: Int, _.asInstanceOf[OffsetDateTime])
  def getOffsetDateTime(columnLabel: String): Option[OffsetDateTime] = getOffsetDateTime(columnNumber(columnLabel))

  def getInstant(column: Int): Option[Instant] = getOffsetDateTime(column).map(_.toInstant)
  def getInstant(columnLabel: String): Option[Instant] = getOffsetDateTime(columnLabel).map(_.toInstant)

  def getUUID(column: Int): Option[UUID] = getAs(column: Int, _.asInstanceOf[UUID])
  def getUUID(columnLabel: String): Option[UUID] = getUUID(columnNumber(columnLabel))

  def getArray[T](column: Int): Option[Vector[T]] = {
    def transformerFnc(obj: Object): Vector[T] = {
      obj.asInstanceOf[sql.Array].getArray().asInstanceOf[Array[T]].toVector
    }
    getAs(column: Int, transformerFnc _) //TODO
  }

  def getArray[T](columnLabel: String): Option[Vector[T]] = getArray[T](columnNumber(columnLabel))

  def getArray[T](column: Int, itemTransformerFnc: TransformerFnc[T]): Option[Vector[T]] = {
    def transformerFnc(obj: Object): Vector[T] = {
      obj
        .asInstanceOf[sql.Array]
        .getArray()
        .asInstanceOf[Array[Object]]
        .toVector
        .map(itemTransformerFnc)
    }

    getAs(column: Int, transformerFnc _)
  }

}

object QueryResultRow {

  type FieldNames = Map[String, Int]
  type TransformerFnc[T] = Object => T
  type Extractors = Vector[ResultSet => Option[Object]]

  def apply(resultSet: ResultSet)(implicit fieldNames: FieldNames, extractors: Extractors): QueryResultRow = {
    val fields = extractors.map(_(resultSet))
    new QueryResultRow(resultSet.getRow, fields, fieldNames)
  }

  def fieldNamesFromMetdata(metaData: ResultSetMetaData): FieldNames = {
    Range.inclusive(1, metaData.getColumnCount).map(i => metaData.getColumnName(i) -> i).toMap
  }

  def createExtractors(metaData: ResultSetMetaData): Extractors = {
    def generalExtractor(resultSet: ResultSet, column: Int): Option[Object] = Option(resultSet.getObject(column))

    def timeTzExtractor(resultSet: ResultSet, column: Int): Option[Object] = Option(resultSet.getObject(column, classOf[OffsetTime]))

    def timestampExtractor(resultSet: ResultSet, column: Int): Option[Object] = Option(resultSet.getObject(column, classOf[LocalDateTime]))

    def timestampTzExtractor(resultSet: ResultSet, column: Int): Option[Object] = Option(resultSet.getObject(column, classOf[OffsetDateTime]))

    def arrayExtractor(resultSet: ResultSet, column: Int): Option[Object] = {
      val array: sql.Array = resultSet.getArray(column)
      Option(array)
    }

    def columnTypeName(column: Int): String = metaData.getColumnTypeName(column).toLowerCase()

    Range.inclusive(1, metaData.getColumnCount).map { column =>
      metaData.getColumnType(column) match {
        case Types.TIME if columnTypeName(column) == "timetz" =>  timeTzExtractor(_, column)
        case Types.TIMESTAMP if columnTypeName(column) == "timestamptz" => timestampTzExtractor(_, column)
        case Types.TIMESTAMP => timestampExtractor(_, column)
        case Types.ARRAY => arrayExtractor(_, column)
        case _ => generalExtractor(_, column)
      }
    }.toVector
  }

}
