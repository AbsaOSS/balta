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

package za.co.absa.db.balta.validator

import org.scalatest.funsuite.AnyFunSuite

class ValidatorUnitTests extends AnyFunSuite {

  private val validator = new Validator

//  test("isPositive returns true for a positive integer") {
//    assert(validator.isPositive(1))
//  }

//  test("isPositive returns false for zero") {
//    assert(!validator.isPositive(0))
//  }

//  test("isPositive returns false for a negative integer") {
//    assert(!validator.isPositive(-5))
//  }

  test("isNonEmpty returns true for a non-empty string") {
    assert(validator.isNonEmpty("hello"))
  }

  test("isNonEmpty returns false for an empty string") {
    assert(!validator.isNonEmpty(""))
  }

  test("isNonEmpty returns false for a null string") {
    assert(!validator.isNonEmpty(null))
  }

//  test("isInRange returns true when value is within bounds") {
//    assert(validator.isInRange(5, 1, 10))
//  }

//  test("isInRange returns true when value equals the lower bound") {
//    assert(validator.isInRange(1, 1, 10))
//  }

//  test("isInRange returns true when value equals the upper bound") {
//    assert(validator.isInRange(10, 1, 10))
//  }

//  test("isInRange returns false when value is below the lower bound") {
//    assert(!validator.isInRange(0, 1, 10))
//  }

//  test("isInRange returns false when value is above the upper bound") {
//    assert(!validator.isInRange(11, 1, 10))
//  }

  test("classify returns 'negative' for a negative integer") {
    assert(validator.classify(-3) == "negative")
  }

  test("classify returns 'zero' for zero") {
    assert(validator.classify(0) == "zero")
  }

  test("classify returns 'positive' for a positive integer") {
    assert(validator.classify(7) == "positive")
  }

//  test("firstNonEmpty returns the primary value when it is non-empty") {
//    assert(validator.firstNonEmpty("first", "second") == Some("first"))
//  }

//  test("firstNonEmpty returns the secondary value when primary is empty") {
//    assert(validator.firstNonEmpty("", "second") == Some("second"))
//  }

//  test("firstNonEmpty returns None when both values are empty") {
//    assert(validator.firstNonEmpty("", "") == None)
//  }

//  test("firstNonEmpty returns None when both values are null") {
//    assert(validator.firstNonEmpty(null, null) == None)
//  }
}
