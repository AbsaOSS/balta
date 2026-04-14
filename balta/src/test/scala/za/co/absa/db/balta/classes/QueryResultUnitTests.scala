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
import za.co.absa.db.balta.MockResultSets

import java.sql.SQLException

class QueryResultUnitTests extends AnyFunSuiteLike with MockResultSets {

  test("hasNext returns false for an empty result set") {
    val qr = new QueryResult(emptyMockResultSet)
    assert(!qr.hasNext)
  }

  test("hasNext returns true when at least one row is available") {
    val qr = new QueryResult(singleRowMockResultSet)
    assert(qr.hasNext)
  }

  test("hasNext returns true after repeated calls without consuming the row") {
    val qr = new QueryResult(singleRowMockResultSet)
    assert(qr.hasNext)
    assert(qr.hasNext)
  }

  test("hasNext returns false after the only row has been consumed") {
    val qr = new QueryResult(singleRowMockResultSet)
    qr.next()
    assert(!qr.hasNext)
  }

  test("hasNext swallows SQLException and returns false") {
    val throwingResultSet = new MockResultSets.MockResultSet(0) {
      private var called = false
      override def next(): Boolean = {
        if (!called) {
          called = true
          throw new SQLException("simulated failure")
        }
        false
      }
    }
    val qr = new QueryResult(throwingResultSet)
    assert(!qr.hasNext)
  }

  test("next returns the first row when result set is non-empty") {
    val qr = new QueryResult(singleRowMockResultSet)
    val row = qr.next()
    assert(row.rowNumber == 1)
  }

  test("next advances through all rows in order") {
    val qr = new QueryResult(twoRowMockResultSet)
    val first  = qr.next()
    val second = qr.next()
    assert(first.rowNumber  == 1)
    assert(second.rowNumber == 2)
  }

  test("next throws NoSuchElementException when result set is exhausted") {
    val qr = new QueryResult(emptyMockResultSet)
    assertThrows[NoSuchElementException](qr.next())
  }

  test("next throws NoSuchElementException after consuming all rows") {
    val qr = new QueryResult(singleRowMockResultSet)
    qr.next()
    assertThrows[NoSuchElementException](qr.next())
  }

  test("columnCount reflects the value reported by ResultSetMetaData") {
    val qr = new QueryResult(mockResultSetWithCols(3))
    assert(qr.columnCount == 3)
  }

}
