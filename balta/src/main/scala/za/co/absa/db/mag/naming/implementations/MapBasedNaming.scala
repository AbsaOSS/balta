/*
 * Copyright 2022 ABSA Group Limited
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

package za.co.absa.db.mag.naming.implementations

import za.co.absa.db.balta.implicits.MapImplicits.MapEnhancements
import za.co.absa.db.mag.exceptions.NamingException
import za.co.absa.db.mag.naming.LettersCase.AsIs
import za.co.absa.db.mag.naming.{LettersCase, NamingConvention}

/**
 *  `MapBasedNaming` requires an explicit map of name conversions provided in a form of a `Map[String, String]`.
 *  If the requested name is not found in the map, a `NamingException` is thrown.
 */
class MapBasedNaming private(names: Map[String, String], lettersCase: LettersCase) extends NamingConvention {

  /**
   *  Throws a `NamingConvention` if the original is not present between the keys of the Map.
   *  @param original - The original string.
   *  @return         - The string from the map linked to the original string.
   */
  override def stringPerConvention(original: String): String = {
    names.getOrThrow(lettersCase.convert(original), NamingException(s"No convention for '$original' has been defined."))
  }
}

object MapBasedNaming {
  /**
   * Creates a new `MapBasedNaming` instance with the specified names and letter cases.
   * @param names             - The map of names.
   * @param keysLettersCase   - The case of the keys in the map. Input values are converted to this case upon querying.
   * @param valueLettersCase  - The case of the values in the map.
   * @return                  - The string from the map linked to the original string.
   */
  def apply(names: Map[String, String], keysLettersCase: LettersCase = AsIs, valueLettersCase: LettersCase = AsIs): NamingConvention = {
    val actualNames =  names.map { case (k, v) => (keysLettersCase.convert(k), valueLettersCase.convert(v)) }
    new MapBasedNaming(actualNames, keysLettersCase)
  }
}
