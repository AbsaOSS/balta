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

/**
 * Simple arithmetic calculator used to produce measurable JaCoCo coverage data.
 */
class Calculator {

  /**
   * Returns the sum of two integers.
   * @param a first operand
   * @param b second operand
   * @return a + b
   */
  def add(a: Int, b: Int): Int = a + b

  /**
   * Returns the difference of two integers.
   * @param a first operand
   * @param b second operand
   * @return a - b
   */
  def subtract(a: Int, b: Int): Int = a - b

  /**
   * Returns the product of two integers.
   * @param a first operand
   * @param b second operand
   * @return a * b
   */
  def multiply(a: Int, b: Int): Int = a * b

  /**
   * Returns the integer quotient of a divided by b.
   * @param a dividend
   * @param b divisor; must not be zero
   * @return a / b
   * @throws ArithmeticException if b is zero
   */
  def divide(a: Int, b: Int): Int = {
    if (b == 0) throw new ArithmeticException("Division by zero")
    a / b
  }

  /**
   * Returns the absolute value of an integer.
   * @param a input value
   * @return non-negative value of a
   */
  def abs(a: Int): Int = if (a < 0) -a else a

  /**
   * Returns the larger of two integers.
   * @param a first value
   * @param b second value
   * @return the maximum of a and b
   */
  def max(a: Int, b: Int): Int = if (a >= b) a else b
}
