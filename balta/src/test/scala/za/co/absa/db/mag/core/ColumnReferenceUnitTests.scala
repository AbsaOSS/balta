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
    assert(simple.enteredName == "col1")
    assert(simple.sqlEntry == "col1")
    assert(simple.quoteLess == "col1")
  }

  test("Quoted regular name doesn't need quoting") {
    val quotedExact = ColumnReference("\"abc\"")
    assert(quotedExact.enteredName == "\"abc\"")
    assert(quotedExact.sqlEntry == "abc")
    assert(quotedExact.quoteLess == "abc")
  }

  test("Quoted string is used literally") {
    val quotedGeneral = ColumnReference("\"Col Name\"")
    assert(quotedGeneral.enteredName == """"Col Name"""")
    assert(quotedGeneral.sqlEntry == """"Col Name"""")
    assert(quotedGeneral.quoteLess == """Col Name""")
   }

  test("Contains a non-regular character - capital letter, it's lowercased") {
    val defaultQuoted = ColumnReference("someName")
    assert(defaultQuoted.enteredName == "someName")
    assert(defaultQuoted.sqlEntry == "somename")
    assert(defaultQuoted.quoteLess == "somename")
  }

  test("Contains a non-regular character - Space") {
    val defaultQuoted = ColumnReference("some name")
    assert(defaultQuoted.enteredName == "some name")
    assert(defaultQuoted.sqlEntry == "\"some name\"")
    assert(defaultQuoted.quoteLess == "some name")
  }

  test("Contains a non-regular character - non-ASCII letter") {
    val defaultQuoted = ColumnReference("lék")
    assert(defaultQuoted.enteredName == "lék")
    assert(defaultQuoted.sqlEntry == "\"lék\"")
    assert(defaultQuoted.quoteLess == "lék")
  }

  test("Contains a non-regular character - dash") {
    val defaultQuoted = ColumnReference("some-name")
    assert(defaultQuoted.enteredName == "some-name")
    assert(defaultQuoted.sqlEntry == "\"some-name\"")
    assert(defaultQuoted.quoteLess == "some-name")
  }

  test("Contains a non-regular character - quote") {
    val containsQuote = ColumnReference("a\"bc")
    assert(containsQuote.enteredName == "a\"bc")
    assert(containsQuote.sqlEntry == "\"a\"\"bc\"")
    assert(containsQuote.quoteLess == "a\"bc")
  }

  test("Starts with a number") {
    val defaultQuoted = ColumnReference("123")
    assert(defaultQuoted.enteredName == "123")
    assert(defaultQuoted.sqlEntry == "\"123\"")
    assert(defaultQuoted.quoteLess == "123")
  }

  test("Properly escaped name containing quotes") {
    val columnName = ColumnReference(""""Has ""Quotes""!"""")
    assert(columnName.enteredName == """"Has ""Quotes""!"""")
    assert(columnName.sqlEntry == """"Has ""Quotes""!"""")
    assert(columnName.quoteLess == "Has \"Quotes\"!")
  }

  test("Unescaped quote throws an exception") {
    assertThrows[IllegalArgumentException] {
      ColumnReference("\"ab\"c\"")
    }
  }


  test("ColumnReference object functionality") {
    // index
    val idx = ColumnReference(5)
    assert(idx.isInstanceOf[ColumnReference.ColumnIndex], "Expected ColumnIndex for apply(5)")
    assert(idx.asInstanceOf[ColumnReference.ColumnIndex].sqlEntry == "5")
  }

  test("Equality and hashCode based on sqlEntry") {
    val n1 = ColumnReference("ab")
    val n2 = ColumnReference("\"ab\"")
    assert(n1 == n2, "Expected equality based on sqlEntry")
    assert(n1.hashCode == n2.hashCode, "Expected equal hashCode based on sqlEntry")
  }

  test("Equality and hashCode based on sqlEntry for non-standard names") {
    val n1 = ColumnReference("a-b")
    val n2 = ColumnReference("\"a-b\"")
    assert(n1 == n2, "Expected equality based on sqlEntry")
    assert(n1.hashCode == n2.hashCode, "Expected equal hashCode based on sqlEntry")
  }

  test("Equality for same column name per standard, just differently entered") {
    val n1 = ColumnReference("ab_cd")
    val n2 = ColumnReference("AB_CD")
    val n3 = ColumnReference("\"ab_cd\"")
    assert(n1 == n2)
    assert(n1 == n3)
  }

  test("Inequality for different sqlEntry") {
    val n1 = ColumnReference("a-b")
    val n2 = ColumnReference("a-B")
    assert(n1 != n2, "Expected inequality for different sqlEntry")
  }

  test("Indexed column reference different from number named column") {
    val n1 = ColumnReference("1")
    val n2 = ColumnReference(1)
    assert(n1 != n2, "Expected inequality for different sqlEntry")
  }

  test("unapply returns enteredName") {
    val n1 = ColumnReference("ab")             // ColumnNameSimple with sqlEntry "ab"
    assert(ColumnName.unapply(n1).contains(n1.enteredName))
  }

  test("ColumnReference.quote works correctly") {
    assert(ColumnReference.quote("abc") == "\"abc\"")
    assert(ColumnReference.quote("a\"bc") == "\"a\"bc\"")
    assert(ColumnReference.quote("\"abc\"") == "\"\"abc\"\"")
  }

  test("ColumnReference.escapeQuote works correctly") {
    assert(ColumnReference.escapeQuote("abc") == "abc")
    assert(ColumnReference.escapeQuote("a\"bc") == "a\"\"bc")
    assert(ColumnReference.escapeQuote("\"abc\"") == "\"\"abc\"\"")
  }

  test("ColumnReference.hasUnescapedQuotes works correctly") {
    assert(ColumnReference.hasUnescapedQuotes("a\"bc"))
    assert(!ColumnReference.hasUnescapedQuotes("abc"))
    assert(ColumnReference.hasUnescapedQuotes("\"abc\""))
    assert(!ColumnReference.hasUnescapedQuotes("a\"\"bc\"\"de"))
    assert(ColumnReference.hasUnescapedQuotes("a\"\"bc\"\"\"de"))
    assert(!ColumnReference.hasUnescapedQuotes("a\"\"\"\"de"))
    assert(ColumnReference.hasUnescapedQuotes("a\"\"\"\"\"de"))
  }

}
