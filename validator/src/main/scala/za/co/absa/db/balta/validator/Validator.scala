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

/**
 * Value and string validator used to produce measurable JaCoCo coverage data.
 */
class Validator {

  /**
   * Returns true when the integer is strictly positive.
   * @param value the integer to check
   * @return true if value > 0
   */
  def isPositive(value: Int): Boolean = value > 0

  /**
   * Returns true when the string is non-null and non-empty.
   * @param value the string to check
   * @return true if value is not null and not empty
   */
  def isNonEmpty(value: String): Boolean = value != null && value.nonEmpty

  /**
   * Returns true when value falls within the inclusive range [min, max].
   * @param value the integer to check
   * @param min   lower bound (inclusive)
   * @param max   upper bound (inclusive)
   * @return true if min <= value <= max
   */
  def isInRange(value: Int, min: Int, max: Int): Boolean = value >= min && value <= max

  /**
   * Classifies an integer as "negative", "zero", or "positive".
   * @param value the integer to classify
   * @return one of "negative", "zero", or "positive"
   */
  def classify(value: Int): String = value match {
    case v if v < 0 => "negative"
    case 0          => "zero"
    case _          => "positive"
  }

  /**
   * Returns the first non-null, non-empty string from the pair, or None if both are absent.
   * @param primary   preferred value
   * @param secondary fallback value
   * @return Some(first non-empty value) or None
   */
  def firstNonEmpty(primary: String, secondary: String): Option[String] = {
    if (isNonEmpty(primary)) Some(primary)
    else if (isNonEmpty(secondary)) Some(secondary)
    else None
  }
}
