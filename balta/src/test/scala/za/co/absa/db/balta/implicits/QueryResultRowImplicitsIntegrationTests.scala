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

import java.time.OffsetDateTime
import java.util.UUID
import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.classes.DBFunction
import za.co.absa.db.balta.implicits.QueryResultRowImplicits.ProductTypeConvertor
import za.co.absa.db.balta.testing.classes.DBTestingConnection
import za.co.absa.db.mag.naming.NamingConvention
import za.co.absa.db.mag.naming.implementations.MapBasedNaming


class QueryResultRowImplicitsIntegrationTests extends AnyFunSuiteLike with DBTestingConnection{
  private val function = DBFunction("testing.simple_function")
  private val timestamp = OffsetDateTime.parse("2023-01-01T00:00:00Z")
  private val uuid = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")

  test("Product type with Option types is created with actual values") {
    import za.co.absa.db.mag.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

    val result = function.setParam(true).execute{queryResult =>
      val row = queryResult.next()
      row.toProductType[ResultTypeOptional]
    }
    val expected = ResultTypeOptional(
      intData = Some(42),
      textData = Some("Hello World!"),
      timestampData = Some(timestamp),
      uuidData = Some(uuid)
    )
    assert(result == expected)
  }

  test("Product type with Option types is created with no values") {
    import za.co.absa.db.mag.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

    val result = function.setParam(false).execute{queryResult =>
      val row = queryResult.next()
      row.toProductType[ResultTypeOptional]
    }
    val expected = ResultTypeOptional(
      None, None, None, None
    )
    assert(result == expected)
  }


  test("Product type with no-Option types is create with values") {
    import za.co.absa.db.mag.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

    val result = function.setParam(true).execute{queryResult =>
      val row = queryResult.next()
      row.toProductType[ResultType]
    }
    val expected = ResultType(
      intData = 42,
      textData = "Hello World!",
      timestampData = timestamp,
      uuidData = uuid
    )
    assert(result == expected)
  }

  test("Product type with no-Option types throws NullPointerException exception when no value encountered") {
    import za.co.absa.db.mag.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

    function.setParam(false).execute{queryResult =>
      val row = queryResult.next()
      assertThrows[NullPointerException](row.toProductType[ResultType])
    }
  }

  test("Product type with wrong names throws NoSuchElementException exception") {
    import za.co.absa.db.mag.naming.implementations.SnakeCaseNaming.Implicits.namingConvention

    function.setParam(false).execute{queryResult =>
      val row = queryResult.next()
      assertThrows[NoSuchElementException](row.toProductType[CaseClassOfWrongFields])
    }
  }

  test("Product type of tuple is created if correct naming exists") {
    implicit val naming: NamingConvention =  MapBasedNaming(Map(
      "_1" -> "int_data",
      "_2" -> "text_data",
      "_3" -> "timestamp_data",
      "_4" -> "uuid_data"
    ))
    val result = function.setParam(true).execute{queryResult =>
      val row = queryResult.next()
      row.toProductType[(Int, String, OffsetDateTime, UUID)]
    }
    val expected = (
      42,
      "Hello World!",
      timestamp,
      uuid
    )
    assert(result == expected)
  }
}

// these classes has to be top level
case class ResultTypeOptional(
                              intData: Option[Int],
                              textData: Option[String],
                              timestampData: Option[OffsetDateTime],
                              uuidData: Option[UUID]
                             )

case class ResultType(
                      intData: Int,
                      textData: String,
                      timestampData: OffsetDateTime,
                      uuidData: UUID
                     )

case class CaseClassOfWrongFields(
                                  notInTheRow: String
                                 )
