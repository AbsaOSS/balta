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
// jacoco-touch: simulate change

import za.co.absa.db.mag.naming.LettersCase.AsIs
import za.co.absa.db.mag.naming.{LettersCase, NamingConvention}

/**
 *  `AsIsNaming` provides a naming convention that leaves strings as they are.
 *  It implements the [[NamingConvention]] trait.
 *  @param lettersCase - The case of the letters in the string.
 */
class AsIsNaming(lettersCase: LettersCase) extends NamingConvention {

  /**
   *  Returns the original string converted to the specified letter case.
   *  @param original - The original string.
   *  @return The original string converted to the specified letter case.
   */
  override def stringPerConvention(original: String): String = {
    lettersCase.convert(original)
  }
}

/**
 *  `AsIsNaming.Implicits` provides an implicit [[NamingConvention]] instance that leaves strings as they are.
 */
object AsIsNaming {
  object Implicits {

    /**
     *  An implicit [[NamingConvention]] instance that leaves strings as they are.
     */
    implicit val namingConvention: NamingConvention = new AsIsNaming(AsIs)
  }
}
