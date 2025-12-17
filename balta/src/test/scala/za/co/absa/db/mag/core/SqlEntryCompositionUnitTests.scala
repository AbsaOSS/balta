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

package za.co.absa.db.mag.core

import org.scalatest.funsuite.AnyFunSuiteLike

import scala.language.postfixOps
import za.co.absa.db.mag.core.SqlEntryComposition._

class SqlEntryCompositionUnitTests extends AnyFunSuiteLike {
  private val field1 = ColumnName("field1")
  private val field2 = ColumnName("field2")
  private val tableName = SqlEntry("my_table")
  private val condition = SqlEntry("field1 = 100")
  private val functionName = SqlEntry("my_function")
  private val param = "42"

  test("Composition of `SELECT ... FROM`") {
    val query  = SELECT(field1, field2) FROM tableName
    val expectedSql = "SELECT field1, field2 FROM my_table"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `SELECT ... FROM ... WHERE ...`") {
    val query  = SELECT(ALL) FROM tableName WHERE condition
    val expectedSql = "SELECT * FROM my_table WHERE field1 = 100"
    assert(query.sqlEntry.entry == expectedSql)
  }
  test("Composition of `SELECT ... FROM ... ORDER BY`") {
    val query  = SELECT(field1) FROM tableName ORDER BY(field1, field2)
    val expectedSql = "SELECT field1 FROM my_table ORDER BY field1, field2"
    assert(query.sqlEntry.entry == expectedSql)
  }
  test("Composition of `SELECT ... FROM ... WHERE ... ORDER BY`") {
    val query  = SELECT(COUNT_ALL) FROM tableName WHERE condition ORDER BY(field1)
    val expectedSql = "SELECT count(1) AS cnt FROM my_table WHERE field1 = 100 ORDER BY field1"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `SELECT ... FROM ... WHERE ... ORDER BY` using function call") {
    val query  = SELECT(ALL) FROM functionName(param) WHERE condition ORDER BY(field1)
    val expectedSql = "SELECT * FROM my_function(42) WHERE field1 = 100 ORDER BY field1"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `SELECT ... FROM ...` using function call without providing parameters") {
    val query  = SELECT(ALL) FROM functionName()
    val expectedSql = "SELECT * FROM my_function()"
    assert(query.sqlEntry.entry == expectedSql)
  }


  test("Composition of `INSERT INTO ...`") {
    val query  = INSERT INTO tableName VALUES("100", "'Sample Text'")
    val expectedSql = "INSERT INTO my_table VALUES(100, 'Sample Text')"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `INSERT INTO ... VALUES RETURNING ...` not providing any field names") {
    val query  = INSERT INTO tableName VALUES(param) RETURNING(ALL)
    val expectedSql = "INSERT INTO my_table VALUES(42) RETURNING *"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `INSERT INTO ...(...) VALUES ... RETURNING ...` using field names") {
    val fields = SqlEntry(field1, field2)
    val query  = INSERT INTO tableName(fields.entry) VALUES("100, 'Sample Text'") RETURNING(ALL)
    val expectedSql = "INSERT INTO my_table(field1, field2) VALUES(100, 'Sample Text') RETURNING *"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `DELETE ... FROM ...`") {
    val query  = DELETE FROM tableName
    val expectedSql = "DELETE FROM my_table"
    assert(query.sqlEntry.entry == expectedSql)
  }

  test("Composition of `DELETE ... FROM ... RETURNING ...`") {
    val query  = DELETE FROM tableName RETURNING(field1)
    val expectedSql = "DELETE FROM my_table RETURNING field1"
    assert(query.sqlEntry.entry == expectedSql)
  }

}
