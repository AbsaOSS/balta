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

object MapImplicits {
  implicit class MapEnhancements[K, V](val map: Map[K, V]) extends AnyVal {
    /**
     * Gets the value associated with the key or throws the provided exception
     * @param key       - the key to get the value for
     * @param exception - the exception to throw in case the `option` is None
     * @tparam V1       - the type of the value
     * @return          - the value associated with key if it exists, otherwise throws the provided exception
     */
    def getOrThrow[V1 >: V](key: K, exception: => Throwable): V1 = {
      map.getOrElse(key, throw exception)
    }
  }

}
