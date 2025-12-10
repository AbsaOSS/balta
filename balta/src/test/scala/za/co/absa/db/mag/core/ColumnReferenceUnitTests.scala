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

class ColumnReferenceUnitTests extends AnyFunSuiteLike{
  test("Simple regular name") {
    val simple = ColumnReference("col1")
    assert(simple.isInstanceOf[ColumnReference.ColumnNameSimple], "Expected ColumnNameSimple")
    assert(simple.enteredName == "col1")
    assert(simple.sqlEntry == "col1")
  }

  test("Quoted regular name doesn't need quoting") {
    val quotedExact = ColumnReference("\"ab\"")
    assert(quotedExact.isInstanceOf[ColumnReference.ColumnNameExact], "Expected ColumnNameExact for \"ab\"")
    assert(quotedExact.enteredName == "\"ab\"")
    assert(quotedExact.sqlEntry == "ab")
  }

  test("Quoted string is used literally") {
    val quotedGeneral = ColumnReference("\"Col Name\"")
    assert(quotedGeneral.isInstanceOf[ColumnReference.ColumnNameQuoted], "Expected ColumnNameQuoted for \"Col Name\"")
    assert(quotedGeneral.enteredName == "Col Name")
    assert(quotedGeneral.sqlEntry == "\"Col Name\"")
    assert(quotedGeneral.sqlEntry == """"Col Name"""")
   }

  test("Contains a non-regular character - capital letter") {
    val defaultQuoted = ColumnReference("someName")
    assert(defaultQuoted.enteredName == "someName")
    assert(defaultQuoted.sqlEntry == "\"someName\"")
  }

  test("Contains a non-regular character - Space") {
    val defaultQuoted = ColumnReference("some name")
    assert(defaultQuoted.enteredName == "some name")
    assert(defaultQuoted.sqlEntry == "\"some name\"")
  }

  test("Contains a non-regular character - non-ASCII letter") {
    val defaultQuoted = ColumnReference("lék")
    assert(defaultQuoted.enteredName == "lék")
    assert(defaultQuoted.sqlEntry == "\"lék\"")
  }

  test("Contains a non-regular character - dash") {
    val defaultQuoted = ColumnReference("some-name")
    assert(defaultQuoted.enteredName == "some-name")
    assert(defaultQuoted.sqlEntry == "\"some-name\"")
  }

  test("Contains a non-regular character - quote") {
    val containsQuote = ColumnReference("a\"b")
    assert(containsQuote.enteredName == "a\"b")
    assert(containsQuote.sqlEntry == "a\"\"b")
  }

  test("Starts with a number") {
    val defaultQuoted = ColumnReference("123")
    assert(defaultQuoted.enteredName == "123")
    assert(defaultQuoted.sqlEntry == "\"123\"")
  }


  test("ColumnReference object functionality") {
    // index
    val idx = ColumnReference(5)
    assert(idx.isInstanceOf[ColumnReference.ColumnIndex], "Expected ColumnIndex for apply(5)")
    assert(idx.asInstanceOf[ColumnReference.ColumnIndex].sqlEntry == "5")
  }

  test("Equality and hashCode based on sqlEntry") {
    val n1 = ColumnReference("ab")             // ColumnNameSimple with sqlEntry "ab"
    val n2 = ColumnReference("\"ab\"")         // ColumnNameExact with sqlEntry "ab"
    assert(n1 == n2, "Expected equality based on sqlEntry")
    assert(n1.hashCode == n2.hashCode, "Expected equal hashCode based on sqlEntry")
  }

  test("Inequality for different sqlEntry") {
    val n1 = ColumnReference("ab")
    val n2 = ColumnReference("aB")
    assert(n1 != n2, "Expected inequality for different sqlEntry")
  }

  test("Indexed column reference different from number named column") {
    val n1 = ColumnReference("1")
    val n2 = ColumnReference(1)
    assert(n1 != n2, "Expected inequality for different sqlEntry")
  }

  test("unapply returns enteredName") {
    val n1 = ColumnReference("ab")             // ColumnNameSimple with sqlEntry "ab"
    assert(ColumnReference.unapply(n1) == n1.enteredName)
    assert(ColumnName.unapply(n1) == n1.enteredName)
  }

}
