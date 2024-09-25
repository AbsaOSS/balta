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

package za.co.absa.db.mag.naming.implementations

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.mag.exceptions.NamingException
import za.co.absa.db.mag.naming.LettersCase.{LowerCase, UpperCase}

class MapBasedNamingUnitTests extends AnyFunSuiteLike {
  private val map = Map(
    "Hello" -> "World!",
    "Foo" -> "Bar"
  )

  private val mapNamingConventionAsIs = MapBasedNaming(map)
  private val mapNamingConventionLowerUpper = MapBasedNaming(map, LowerCase, UpperCase)
  private val mapNamingConventionUpperLower = MapBasedNaming(map, UpperCase, LowerCase)

  test("MapBasedNaming with AsIs LetterCase should return the found string") {

    val input = "Hello"
    val expectedOutput = "World!"
    val output = mapNamingConventionAsIs.stringPerConvention(input)

    assert(output == expectedOutput)
  }

  test("MapBasedNaming with altered case should return the found string in defined output case") {
    val input1 = "Hello"
    val expectedOutput1 = "WORLD!"
    val output1 = mapNamingConventionLowerUpper.stringPerConvention(input1)

    assert(output1 == expectedOutput1)

    val input2 = "Foo"
    val expectedOutput2 = "bar"
    val output2 = mapNamingConventionUpperLower.stringPerConvention(input2)

    assert(output2 == expectedOutput2)
  }

  test("MapBaseNaming fails when key is not found") {
    val input = "NotInMap"
    assertThrows[NamingException] {
      mapNamingConventionAsIs.stringPerConvention(input)
    }
  }

  test("With default LetterCase MapBasedNaming cares about case") {
    val input = "hello"
    assertThrows[NamingException] {
      mapNamingConventionAsIs.stringPerConvention(input)
    }
  }
}
