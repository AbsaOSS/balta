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

package za.co.absa.db.balta.calculator

import org.scalatest.funsuite.AnyFunSuite

class CalculatorUnitTests extends AnyFunSuite {

  private val calc = new Calculator

  test("add returns the sum of two positive integers") {
    assert(calc.add(3, 4) == 7)
  }

  test("add returns the sum when one operand is negative") {
    assert(calc.add(-2, 5) == 3)
  }

//  test("subtract returns the difference of two integers") {
//    assert(calc.subtract(10, 4) == 6)
//  }

//  test("subtract returns negative result when b is greater than a") {
//    assert(calc.subtract(2, 9) == -7)
//  }

//  test("multiply returns the product of two integers") {
//    assert(calc.multiply(3, 7) == 21)
//  }

//  test("multiply returns zero when one operand is zero") {
//    assert(calc.multiply(0, 99) == 0)
//  }

  test("divide returns the integer quotient") {
    assert(calc.divide(10, 2) == 5)
  }

  test("divide throws ArithmeticException when divisor is zero") {
    intercept[ArithmeticException] {
      calc.divide(5, 0)
    }
  }

//  test("abs returns the same value for a positive integer") {
//    assert(calc.abs(7) == 7)
//  }

//  test("abs returns the negated value for a negative integer") {
//    assert(calc.abs(-7) == 7)
//  }

//  test("abs returns zero for zero input") {
//    assert(calc.abs(0) == 0)
//  }

  test("max returns the first value when it is greater") {
    assert(calc.max(9, 3) == 9)
  }

  test("max returns the second value when it is greater") {
    assert(calc.max(3, 9) == 9)
  }

  test("max returns the common value when both are equal") {
    assert(calc.max(5, 5) == 5)
  }
}
