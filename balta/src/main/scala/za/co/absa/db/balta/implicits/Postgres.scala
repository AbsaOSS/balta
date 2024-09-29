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

import org.postgresql.util.PGobject
import za.co.absa.db.balta.classes.QueryResultRow
import za.co.absa.db.balta.classes.simple.{JsonBString, SimpleJsonString}

object Postgres {
  implicit class PostgresRow(val row: QueryResultRow) extends AnyVal {
    private def jsonBStringTransformer(obj: Object): JsonBString = JsonBString(obj.asInstanceOf[PGobject].toString)
    private def simpleJsonStringTransformer(obj: Object): SimpleJsonString = SimpleJsonString(obj.asInstanceOf[PGobject].toString)

    @deprecated("Use getSimpleJsonString instead", "0.2.0")
    def getJsonB(column: Int): Option[JsonBString] = row.getAs[JsonBString](column: Int, jsonBStringTransformer _)
    @deprecated("Use getSimpleJsonString instead", "0.2.0")
    def getJsonB(columnLabel: String): Option[JsonBString] = getJsonB(row.columnNumber(columnLabel))

    def getSimpleJsonString(column: Int): Option[SimpleJsonString] = row.getAs[SimpleJsonString](column: Int, simpleJsonStringTransformer _)
    def getSimpleJsonString(columnLabel: String): Option[SimpleJsonString] = getSimpleJsonString(row.columnNumber(columnLabel))

    def getSJSArray(column: Int): Option[Vector[SimpleJsonString]] =
      row.getArray(column: Int, item => SimpleJsonString(item.asInstanceOf[String]))
    def getSJSArray(columnLabel: String): Option[Vector[SimpleJsonString]] = getSJSArray(row.columnNumber(columnLabel))

  }
}
