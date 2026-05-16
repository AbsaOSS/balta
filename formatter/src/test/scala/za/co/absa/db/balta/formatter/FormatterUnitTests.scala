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

package za.co.absa.db.balta.formatter

import org.scalatest.funsuite.AnyFunSuite

class FormatterUnitTests extends AnyFunSuite {

  private val fmt = new Formatter

  test("capitalize uppercases the first character of a lowercase word") {
    assert(fmt.capitalize("hello") == "Hello")
  }

  test("capitalize leaves an already-capitalized string unchanged") {
    assert(fmt.capitalize("Hello") == "Hello")
  }

  test("capitalize returns an empty string unchanged") {
    assert(fmt.capitalize("") == "")
  }

  test("capitalize returns null unchanged") {
    assert(fmt.capitalize(null) == null)
  }

  test("capitalize handles a single-character string") {
    assert(fmt.capitalize("a") == "A")
  }

//  test("truncate returns the string unchanged when shorter than maxLen") {
//    assert(fmt.truncate("hi", 10) == "hi")
//  }

//  test("truncate returns the string unchanged when equal to maxLen") {
//    assert(fmt.truncate("hello", 5) == "hello")
//  }

//  test("truncate appends ellipsis when the string exceeds maxLen") {
//    assert(fmt.truncate("hello world", 5) == "hello...")
//  }

  test("wrap surrounds a string with the given prefix and suffix") {
    assert(fmt.wrap("world", "Hello, ", "!") == "Hello, world!")
  }

  test("wrap with empty prefix and suffix returns the original string") {
    assert(fmt.wrap("world", "", "") == "world")
  }

//  test("repeat returns the string concatenated the given number of times") {
//    assert(fmt.repeat("ab", 3) == "ababab")
//  }

//  test("repeat returns an empty string when times is zero") {
//    assert(fmt.repeat("ab", 0) == "")
//  }

//  test("repeat returns an empty string when times is negative") {
//    assert(fmt.repeat("ab", -1) == "")
//  }

//  test("repeat with times equal to one returns the original string") {
//    assert(fmt.repeat("ab", 1) == "ab")
//  }
}
