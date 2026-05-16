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

/**
 * String formatter used to produce measurable JaCoCo coverage data.
 */
class Formatter {

  /**
   * Capitalizes the first character of a string, leaving the rest unchanged.
   * Returns the input unchanged when it is null or empty.
   * @param s input string
   * @return string with the first character uppercased
   */
  def capitalize(s: String): String = {
    if (s == null || s.isEmpty) s
    else s.head.toUpper + s.tail
  }

  /**
   * Truncates a string to at most maxLen characters, appending "..." when truncation occurs.
   * @param s      input string
   * @param maxLen maximum allowed length before truncation
   * @return the original string or its prefix followed by "..."
   */
  def truncate(s: String, maxLen: Int): String = {
    if (s.length <= maxLen) s
    else s.take(maxLen) + "..."
  }

  /**
   * Wraps a string with a prefix and suffix.
   * @param s      input string
   * @param prefix string prepended to s
   * @param suffix string appended to s
   * @return prefix + s + suffix
   */
  def wrap(s: String, prefix: String, suffix: String): String = s"$prefix$s$suffix"

  /**
   * Repeats a string a given number of times.
   * Returns an empty string when times is zero or negative.
   * @param s     string to repeat
   * @param times number of repetitions
   * @return concatenation of s repeated times
   */
  def repeat(s: String, times: Int): String = {
    if (times <= 0) ""
    else s * times
  }
}
