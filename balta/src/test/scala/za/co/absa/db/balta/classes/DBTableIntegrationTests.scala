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
import za.co.absa.db.balta.classes.inner.Params
import za.co.absa.db.balta.testing.classes.DBTestingConnection
import za.co.absa.db.balta.implicits.OptionImplicits.OptionEnhancements
import DBTableIntegrationTests.QueryResultRowAssertion

import java.time.OffsetDateTime
import za.co.absa.db.balta.typeclasses.QueryParamType.NULL

class DBTableIntegrationTests extends AnyFunSuiteLike with DBTestingConnection{
  private val table = DBTable("testing.table_lifecycle")

  test("Table lifecycle using parameters") {
    table.delete()
    assert(table.count() == 0)
    val insertResult = table.insert(Params
      .add("id_field", 1)
      .add("text_field", "textA")
      .add("boolean_field", true)
    )
    insertResult.assertTo(1, "textA", booleanField = true, now())
    table.insert(Params
      .add("id_field", 2)
      .add("text_field", "textB")
      .add("boolean_field", false)
    )
    table.insert(Params
      .add("id_field", 3)
      .add("text_field", "textB")
      .add("boolean_field", true)
    )
    table.insert(Params
      .add("id_field", 4)
      .add("text_field", "textA")
      .add("boolean_field", false)
    )
    val insertedNulls = table.insert(Params
      .add("id_field", 5)
      .add("text_field", NULL)
      .add("boolean_field", NULL)
    )
    insertedNulls.assertHasJustId(5)

    assert(table.count() == 5)

    val returnedValue = table.where(Params.add("text_field", "textA"), "id_field") { queryResult =>
      queryResult.next().assertTo(1, "textA", booleanField = true)
      queryResult.next().assertTo(4, "textA", booleanField = false)
      assert(queryResult.noMore)
      "Hello world"
    }
    assert(returnedValue == "Hello world")

    table.where(Params.add("text_field", "textB").add("boolean_field", false)) { queryResult =>
      queryResult.next().assertTo(2, "textB", booleanField = false)
      assert(queryResult.noMore)
    }

    val deletedRows = table.deleteWithCheck(Params.add("text_field", "textB")) { queryResult =>
      queryResult
        .toList
        .sortBy(_.getLong("id_field").get)
    }
    assert(deletedRows.length == 2)
    deletedRows.head.assertTo(2, "textB", booleanField = false)
    deletedRows(1).assertTo(3, "textB", booleanField = true)

    assert(table.countOnCondition(Params.add("boolean_field", false)) == 1)

    table.all("id_field") { queryResult =>
      queryResult.next().assertTo(1, "textA", booleanField = true)
      queryResult.next().assertTo(4, "textA", booleanField = false)
      queryResult.next().assertHasJustId(5)
      assert(queryResult.noMore)
    }

    table.delete(Params.add("text_field", NULL))
    assert(table.count() == 2)
    table.delete()
    assert(table.count() == 0)
  }


  test("Table lifecycle using where condition") {
    table.delete()
    assert(table.count() == 0)
    val insertResult = table.insert(Params
      .add("id_field", 1)
      .add("text_field", "textA")
      .add("boolean_field", true)
    )
    insertResult.assertTo(1, "textA", booleanField = true, now())
    table.insert(Params
      .add("id_field", 2)
      .add("text_field", "textB")
      .add("boolean_field", false)
    )
    table.insert(Params
      .add("id_field", 3)
      .add("text_field", "textB")
      .add("boolean_field", true)
    )
    table.insert(Params
      .add("id_field", 4)
      .add("text_field", "textA")
      .add("boolean_field", false)
    )
    val insertedNulls = table.insert(Params
      .add("id_field", 5)
      .add("text_field", NULL)
      .add("boolean_field", NULL)
    )
    insertedNulls.assertHasJustId(5)

    assert(table.count() == 5)

    val returnedValue = table.where("text_field != 'textA'", "id_field") { queryResult =>
      queryResult.next().assertTo(2, "textB", booleanField = false)
      queryResult.next().assertTo(3, "textB", booleanField = true)
      assert(queryResult.noMore)
      "Hello world"
    }
    assert(returnedValue == "Hello world")

    table.where("text_field = 'textA' AND boolean_field") { queryResult =>
      queryResult.next().assertTo(1, "textA", booleanField = true)
      assert(queryResult.noMore)
    }

    val deletedRows = table.deleteWithCheck("text_field = 'textA'") { queryResult =>
      queryResult
        .toList
        .sortBy(_.getLong("id_field").get)
    }
    assert(deletedRows.length == 2)
    deletedRows.head.assertTo(1, "textA", booleanField = true)
    deletedRows(1).assertTo(4, "textA", booleanField = false)

    assert(table.countOnCondition("boolean_field") == 1)

    val remainingRows = table.all() { queryResult =>
      queryResult.toList.sortBy(_.getLong("id_field").get)
    }
    assert(remainingRows.length == 3)
    remainingRows.head.assertTo(2, "textB", booleanField = false)
    remainingRows(1).assertTo(3, "textB", booleanField = true)
    remainingRows(2).assertHasJustId(5)

    table.delete("text_field IS NOT NULL")
    assert(table.count() == 1)
    table.deleteWithCheck{ queryResult =>
      queryResult.next().assertHasJustId(5)
      queryResult.noMore
    }
    assert(table.count() == 0)
  }

  test("Table with strange column names") {
    val strangeTable = DBTable("testing.strange_columns")
    assert(strangeTable.count() == 1)

    strangeTable.insert(Params
      .add("id_strange_columns", 2)
      .add("col1", "a1")
      .add("\"Col1\"", "bb1")
      .add("col 1", "ccc1")
      .add("col-1", "ddddd1")
      .add("colá", "eeeee1")
      .add("1col", "ffffff1")
    )

    strangeTable.insert(Params
      .add("id_strange_columns", 3)
      .add("Col1", "a2")
      .add("\"Col1\"", "bb2")
      .add("\"col 1\"", "ccc2")
      .add("\"col-1\"", "ddddd2")
      .add("\"colá\"", "eeeee2")
      .add("\"1col\"", "ffffff2")
    )

    strangeTable.where(Params.add("\"Col1\"", "bb")) { queryResult =>
      val row = queryResult.next()
      assert(row.getLong("id_strange_columns") @= 1L)
      assert(row.getString("col1") @= "a")
      assert(row.getString("\"Col1\"") @= "bb")
      assert(row.getString("col 1") @= "ccc")
      assert(row.getString("col-1") @= "ddddd")
      assert(row.getString("colá") @= "eeeee")
      assert(row.getString("1col") @= "ffffff")
      assert(queryResult.noMore)
    }
    strangeTable.where(Params.add("\"Col1\"", "bb1")) { queryResult =>
      val row = queryResult.next()
      assert(row.getLong("id_strange_columns") @= 2L)
      assert(row.getString("col1") @= "a1")
      assert(row.getString("\"Col1\"") @= "bb1")
      assert(row.getString("col 1") @= "ccc1")
      assert(row.getString("col-1") @= "ddddd1")
      assert(row.getString("colá") @= "eeeee1")
      assert(row.getString("1col") @= "ffffff1")
      assert(queryResult.noMore)
    }
    strangeTable.where(Params.add("col 1", "ccc2")) { queryResult =>
      val row = queryResult.next()
      assert(row.getLong("id_strange_columns") @= 3L)
      assert(row.getString("col1") @= "a2")
      assert(row.getString("\"Col1\"") @= "bb2")
      assert(row.getString("col 1") @= "ccc2")
      assert(row.getString("col-1") @= "ddddd2")
      assert(row.getString("colá") @= "eeeee2")
      assert(row.getString("1col") @= "ffffff2")
      assert(queryResult.noMore)
    }

    strangeTable.delete()
    assert(strangeTable.count() == 0)
  }
}

object DBTableIntegrationTests {
  implicit class QueryResultRowAssertion(val row: QueryResultRow) extends AnyVal {
    private def error(field: String, expected: Any): String = {
      s"`$field` field does not contain [$expected] but [${row(field).orNull}]"
    }

    def assertTo(idField: Long, textField: String, booleanField: Boolean, createdAt: OffsetDateTime): Unit = {
      assertTo(idField, textField, booleanField)
      assert(row("created_at") @= createdAt, error("created_at", createdAt))
    }

    def assertTo(idField: Long, textField: String, booleanField: Boolean): Unit = {
      assert(row("id_field") @=  idField, error("id_field", idField))
      assert(row("text_field") @= textField, error("text_field", textField))
      assert(row("boolean_field") @= booleanField, error("boolean_field", booleanField))
    }

    def assertHasJustId(idField: Long): Unit = {
      assert(row("id_field") @= idField, error("id_field", idField))
      assert(row("text_field").isEmpty, error("text_field", "NULL"))
      assert(row("boolean_field").isEmpty, error("boolean_field", "NULL"))
    }
  }
}
