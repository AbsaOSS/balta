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

package za.co.absa.db.balta.implicits

object OptionImplicits {
  implicit class OptionEnhancements[T](val option: Option[T]) extends AnyVal {
    /**
     * Gets the `option` value or throws the provided exception
     *
     * @param exception the exception to throw in case the `option` is None
     * @return
     */
    def getOrThrow(exception: => Throwable): T = {
      option.getOrElse(throw exception)
    }

    /**
     * The function is an alias for `contains` method, but shorter and suitable for inflix usage
     *
     * @param value the value to check if present in the `option`
     * @return      true if the `option` is defined and contains the provided value, false otherwise
     */
    def @=(value: T): Boolean = option.contains(value)
  }

}
