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

import za.co.absa.db.mag.naming.LettersCase.LowerCase
import za.co.absa.db.mag.naming.{LettersCase, NamingConvention}

/**
 *  `SnakeCaseNaming` provides a naming convention that converts camel case strings to snake case.
 *  It implements the [[NamingConvention]] trait.
 *
 *  @param lettersCase - The case of the letters in the string.
 */
class SnakeCaseNaming(lettersCase: LettersCase) extends NamingConvention {

  private def camelCaseToSnakeCase(s: String): String = {
    s.replaceAll("([A-Z])", "_$1")
  }

  private def stripIfFirstChar(s: String, ch: Char): String = {
    if (s == "") {
      s
    } else if (s(0) == ch) {
      s.substring(1)
    } else {
      s
    }
  }

  /**
   *  Converts the original string to snake case and the specified letter case.
   *  @param original - The original string.
   *  @return The original string converted to snake case and the specified letter case.
   */
  override def stringPerConvention(original: String): String = {
    lettersCase.convert(stripIfFirstChar(camelCaseToSnakeCase(original), '_'))
  }
}

/**
 *  `SnakeCaseNaming.Implicits` provides an implicit [[NamingConvention]] instance that converts camel case strings to snake case.
 */
object SnakeCaseNaming {
  object Implicits {

    /**
     *  An implicit [[NamingConvention]] instance that converts camel case strings to snake case.
     */
    implicit val namingConvention: NamingConvention = new SnakeCaseNaming(LowerCase)
  }
}
