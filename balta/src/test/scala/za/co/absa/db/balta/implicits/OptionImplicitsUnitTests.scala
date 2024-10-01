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

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.implicits.OptionImplicits.OptionEnhancements


class OptionImplicitsUnitTests extends AnyFunSuiteLike {
  test("getOrThrow returns the value if it is defined") {
    val opt = Some(true)
    assert(opt.getOrThrow(new Exception("Foo")))
  }

  test("getOrThrow throws an exception if the value is not defined") {
    val opt = None
    assertThrows[Exception](opt.getOrThrow(new Exception("Foo")))
  }

  test("@= returns true if the value is defined and equals the provided value") {
    val opt = Some(42)
    assert(opt @= 42)
  }

  test("@= returns false if the value is defined but does not equal the provided value") {
    val opt = Some(42)
    assert(!(opt @= 43))
  }

  test("@= returns false if the value is not defined") {
    val opt = None
    assert(!(opt @= 42))
  }

}
