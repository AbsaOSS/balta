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

import java.sql.{ResultSetMetaData, Types}
import java.time.{OffsetDateTime, OffsetTime}

import org.scalatest.funsuite.AnyFunSuiteLike

import za.co.absa.db.balta.MockResultSets
import za.co.absa.db.balta.classes.QueryResultRow.FieldNames
import za.co.absa.db.balta.implicits.QueryResultRowImplicits.ProductTypeConvertor
import za.co.absa.db.mag.naming.LettersCase.AsIs
import za.co.absa.db.mag.naming.implementations.AsIsNaming

case class SimpleRecord(name: String, age: Int)
case class OptionalRecord(label: String, value: Option[Int])

case class MultiCtorRecord(x: Int, y: String) {
  def this(x: Int) = this(x, "default")
}

class QueryResultRowUnitTests extends AnyFunSuiteLike {

  private implicit val naming: AsIsNaming = new AsIsNaming(AsIs)

  private val sampleFields: Vector[Option[Object]] = Vector(
    Some("hello".asInstanceOf[Object]),
    Some(java.lang.Integer.valueOf(42).asInstanceOf[Object]),
    None,
    Some("".asInstanceOf[Object])
  )

  private val sampleColumnLabels: FieldNames = Map(
    "name" -> 1,
    "age" -> 2,
    "nullable" -> 3,
    "empty" -> 4
  )

  private def mkRow(fields: Vector[Option[Object]] = sampleFields,
                    labels: FieldNames = sampleColumnLabels,
                    rowNum: Int = 1): QueryResultRow = {
    new QueryResultRow(rowNum, fields, labels)
  }

  test("columnNumber returns correct index for existing column") {
    val row = mkRow()
    assert(row.columnNumber("name") == 1)
    assert(row.columnNumber("age") == 2)
  }

  test("apply(Int) returns correct value by 1-based column index") {
    val row = mkRow()
    assert(row(1).contains("hello"))
    assert(row(2).contains(java.lang.Integer.valueOf(42)))
  }

  test("apply(Int) returns None for null column") {
    val row = mkRow()
    assert(row(3).isEmpty)
  }

  test("apply(String) returns correct value by column label") {
    val row = mkRow()
    assert(row("name").contains("hello"))
    assert(row("age").contains(java.lang.Integer.valueOf(42)))
  }

  test("apply(String) returns None for null column") {
    val row = mkRow()
    assert(row("nullable").isEmpty)
  }

  test("columnNumber is case-insensitive") {
    val row = mkRow()
    assert(row.columnNumber("NAME") == 1)
    assert(row.columnNumber("Age") == 2)
  }

  test("columnNumber throws NoSuchElementException for missing column") {
    val row = mkRow()
    val ex = intercept[NoSuchElementException] {
      row.columnNumber("nonexistent")
    }
    assert(ex.getMessage.contains("nonexistent"))
  }

  test("getChar returns first character of a non-empty string") {
    val row = mkRow()
    assert(row.getChar(1).contains('h'))
  }

  test("getChar returns None for empty string") {
    val row = mkRow()
    assert(row.getChar(4).isEmpty)
  }

  test("getChar returns None for null column") {
    val row = mkRow()
    assert(row.getChar(3).isEmpty)
  }

  test("getAs with transformer applies the function") {
    val row = mkRow()
    val result = row.getAs[Int](2, { obj: Object => obj.asInstanceOf[java.lang.Integer].intValue() })
    assert(result.contains(42))
  }

  test("getAs with transformer returns None for null column") {
    val row = mkRow()
    val result = row.getAs[String](3, { obj: Object => obj.toString })
    assert(result.isEmpty)
  }

  test("getObject returns Some for non-null column") {
    val row = mkRow()
    assert(row.getObject(1).isDefined)
  }

  test("getObject returns None for null column") {
    val row = mkRow()
    assert(row.getObject(3).isEmpty)
  }

  test("fieldNamesFromMetadata builds correct map") {
    val metaData = new StubMetaData(
      List(("id", Types.INTEGER, "int4"), ("name", Types.VARCHAR, "varchar"))
    )
    val result = QueryResultRow.fieldNamesFromMetadata(metaData)
    assert(result == Map("id" -> 1, "name" -> 2))
  }

  test("fieldNamesFromMetadata normalizes column names for case-insensitive lookup") {
    val metaData = new StubMetaData(
      List(("ID_FIELD", Types.INTEGER, "int4"), ("Text_Field", Types.VARCHAR, "varchar"))
    )
    val result = QueryResultRow.fieldNamesFromMetadata(metaData)
    assert(result == Map("id_field" -> 1, "text_field" -> 2))

    val row = mkRow(labels = result)
    assert(row.columnNumber("ID_FIELD") == 1)
    assert(row.columnNumber("text_FIELD") == 2)
  }

  test("fieldNamesFromMetadata returns empty map for zero columns") {
    val metaData = new StubMetaData(Nil)
    val result = QueryResultRow.fieldNamesFromMetadata(metaData)
    assert(result.isEmpty)
  }

  test("createExtractors returns correct number of extractors") {
    val metaData = new StubMetaData(
      List(("a", Types.VARCHAR, "varchar"), ("b", Types.INTEGER, "int4"), ("c", Types.TIMESTAMP, "timestamp"))
    )
    val extractors = QueryResultRow.createExtractors(metaData)
    assert(extractors.size == 3)
  }

  test("createExtractors dispatches timestamptz to OffsetDateTime extractor") {
    val expected = OffsetDateTime.parse("2024-01-15T10:30:00+02:00")
    val metaData = new StubMetaData(List(("ts", Types.TIMESTAMP, "timestamptz")))
    val rs = new MockResultSets.MockResultSet(1) {
      override def getObject[T](columnIndex: Int, `type`: Class[T]): T = expected.asInstanceOf[T]
    }
    val extractors = QueryResultRow.createExtractors(metaData)
    assert(extractors.size == 1)
    assert(extractors.head(rs).contains(expected))
  }

  test("createExtractors dispatches timetz to OffsetTime extractor") {
    val expected = OffsetTime.parse("10:30:00+02:00")
    val metaData = new StubMetaData(List(("t", Types.TIME, "timetz")))
    val rs = new MockResultSets.MockResultSet(1) {
      override def getObject[T](columnIndex: Int, `type`: Class[T]): T = expected.asInstanceOf[T]
    }
    val extractors = QueryResultRow.createExtractors(metaData)
    assert(extractors.size == 1)
    assert(extractors.head(rs).contains(expected))
  }

  test("createExtractors dispatches ARRAY type") {
    val arr: Array[Object] = Array(java.lang.Integer.valueOf(1))
    val sqlArr = new StubSqlArray(arr)
    val metaData = new StubMetaData(List(("arr", Types.ARRAY, "_int4")))
    val rs = new MockResultSets.MockResultSet(1) {
      override def getArray(columnIndex: Int): java.sql.Array = sqlArr
    }
    val extractors = QueryResultRow.createExtractors(metaData)
    assert(extractors.size == 1)
    assert(extractors.head(rs).contains(sqlArr))
  }

  test("getArray returns Vector from sql.Array column") {
    val arr: Array[Object] = Array(
      java.lang.Integer.valueOf(1),
      java.lang.Integer.valueOf(2),
      java.lang.Integer.valueOf(3)
    )
    val sqlArray = new StubSqlArray(arr)
    val fields: Vector[Option[Object]] = Vector(Some(sqlArray.asInstanceOf[Object]))
    val labels: FieldNames = Map("nums" -> 1)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.getArray[Int](1)
    assert(result.contains(Vector(1, 2, 3)))
  }

  test("getArray returns None for null column") {
    val fields: Vector[Option[Object]] = Vector(None)
    val labels: FieldNames = Map("nums" -> 1)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.getArray[Int](1)
    assert(result.isEmpty)
  }

  test("getArray with transformer applies itemTransformerFnc") {
    val arr: Array[Object] = Array(
      java.lang.Integer.valueOf(10),
      java.lang.Integer.valueOf(20)
    )
    val sqlArray = new StubSqlArray(arr)
    val fields: Vector[Option[Object]] = Vector(Some(sqlArray.asInstanceOf[Object]))
    val labels: FieldNames = Map("nums" -> 1)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.getArray[String](1, { obj: Object => obj.toString })
    assert(result.contains(Vector("10", "20")))
  }

  test("getArray by column label delegates to int overload") {
    val arr: Array[Object] = Array(java.lang.Integer.valueOf(5))
    val sqlArray = new StubSqlArray(arr)
    val fields: Vector[Option[Object]] = Vector(Some(sqlArray.asInstanceOf[Object]))
    val labels: FieldNames = Map("data" -> 1)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.getArray[Int]("data")
    assert(result.contains(Vector(5)))
  }

  test("toProductType converts a row to a case class") {
    val fields: Vector[Option[Object]] = Vector(
      Some("Alice".asInstanceOf[Object]),
      Some(java.lang.Integer.valueOf(30).asInstanceOf[Object])
    )
    val labels: FieldNames = Map("name" -> 1, "age" -> 2)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.toProductType[SimpleRecord]
    assert(result == SimpleRecord("Alice", 30))
  }

  test("toProductType handles Option field with value") {
    val fields: Vector[Option[Object]] = Vector(
      Some("test".asInstanceOf[Object]),
      Some(java.lang.Integer.valueOf(42).asInstanceOf[Object])
    )
    val labels: FieldNames = Map("label" -> 1, "value" -> 2)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.toProductType[OptionalRecord]
    assert(result == OptionalRecord("test", Some(42)))
  }

  test("toProductType handles Option field with null") {
    val fields: Vector[Option[Object]] = Vector(
      Some("test".asInstanceOf[Object]),
      None
    )
    val labels: FieldNames = Map("label" -> 1, "value" -> 2)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.toProductType[OptionalRecord]
    assert(result == OptionalRecord("test", None))
  }

  test("toProductType throws NullPointerException for null non-Option field") {
    val fields: Vector[Option[Object]] = Vector(
      None,
      Some(java.lang.Integer.valueOf(1).asInstanceOf[Object])
    )
    val labels: FieldNames = Map("name" -> 1, "age" -> 2)
    val row = new QueryResultRow(1, fields, labels)
    val ex = intercept[NullPointerException] {
      row.toProductType[SimpleRecord]
    }
    assert(ex.getMessage.contains("name"))
  }

  test("toProductType works with case class having auxiliary constructor") {
    val fields: Vector[Option[Object]] = Vector(
      Some(java.lang.Integer.valueOf(99).asInstanceOf[Object]),
      Some("alt".asInstanceOf[Object])
    )
    val labels: FieldNames = Map("x" -> 1, "y" -> 2)
    val row = new QueryResultRow(1, fields, labels)
    val result = row.toProductType[MultiCtorRecord]
    assert(result == MultiCtorRecord(99, "alt"))
  }

  /**
   * Stub ResultSetMetaData for unit testing.
   */
  private class StubMetaData(columns: List[(String, Int, String)]) extends ResultSetMetaData {
    override def getColumnCount: Int = columns.size
    override def getColumnName(column: Int): String = columns(column - 1)._1
    override def getColumnType(column: Int): Int = columns(column - 1)._2
    override def getColumnTypeName(column: Int): String = columns(column - 1)._3
    override def getColumnLabel(column: Int): String = getColumnName(column)
    override def isAutoIncrement(column: Int): Boolean = false
    override def isCaseSensitive(column: Int): Boolean = false
    override def isSearchable(column: Int): Boolean = false
    override def isCurrency(column: Int): Boolean = false
    override def isNullable(column: Int): Int = ResultSetMetaData.columnNullable
    override def isSigned(column: Int): Boolean = false
    override def getColumnDisplaySize(column: Int): Int = 0
    override def getSchemaName(column: Int): String = ""
    override def getPrecision(column: Int): Int = 0
    override def getScale(column: Int): Int = 0
    override def getTableName(column: Int): String = ""
    override def getCatalogName(column: Int): String = ""
    override def isReadOnly(column: Int): Boolean = false
    override def isWritable(column: Int): Boolean = false
    override def isDefinitelyWritable(column: Int): Boolean = false
    override def getColumnClassName(column: Int): String = "java.lang.Object"
    override def unwrap[T](iface: Class[T]): T = throw new UnsupportedOperationException
    override def isWrapperFor(iface: Class[_]): Boolean = false
  }

  /**
   * Stub java.sql.Array for unit testing getArray methods.
   */
  private class StubSqlArray(data: Array[Object]) extends java.sql.Array {
    override def getBaseTypeName: String = "int4"
    override def getBaseType: Int = Types.INTEGER
    override def getArray: AnyRef = data
    override def getArray(map: java.util.Map[String, Class[_]]): AnyRef = data
    override def getArray(index: Long, count: Int): AnyRef = data
    override def getArray(index: Long, count: Int, map: java.util.Map[String, Class[_]]): AnyRef = data
    override def getResultSet: java.sql.ResultSet = throw new UnsupportedOperationException
    override def getResultSet(map: java.util.Map[String, Class[_]]): java.sql.ResultSet = throw new UnsupportedOperationException
    override def getResultSet(index: Long, count: Int): java.sql.ResultSet = throw new UnsupportedOperationException
    override def getResultSet(index: Long, count: Int, map: java.util.Map[String, Class[_]]): java.sql.ResultSet = throw new UnsupportedOperationException
    override def free(): Unit = ()
  }
}
