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

package za.co.absa.db.mag.naming

/**
 *  `LettersCase` is a sealed trait that represents different cases of letters.
 *  It provides a method to convert a string to the specific case.
 */
sealed trait LettersCase {

  /**
   *  Converts a string to the specific case.
   *  @param s - The original string.
   *  @return The string converted to the specific case.
   */
  def convert(s: String): String
}

object LettersCase {

  /**
   *  `AsIs` is a [[LettersCase]] that leaves strings as they are.
   */
  case object AsIs extends LettersCase {
    override def convert(s: String): String = s
  }

  /**
   *  `LowerCase` is a [[LettersCase]] that converts strings to lower case.
   */
  case object LowerCase extends LettersCase {
    override def convert(s: String): String = s.toLowerCase
  }

  /**
   *  `UpperCase` is a [[LettersCase]] that converts strings to upper case.
   */
  case object UpperCase extends LettersCase {
    override def convert(s: String): String = s.toUpperCase
  }
}

