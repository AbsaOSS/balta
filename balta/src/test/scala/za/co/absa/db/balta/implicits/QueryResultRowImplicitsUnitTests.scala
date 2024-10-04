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

import scala.reflect.runtime.universe.typeOf

class QueryResultRowImplicitsUnitTests extends AnyFunSuiteLike {

  test("isOptionType should return true for Option type") {
    assert(QueryResultRowImplicits.isOptionType(typeOf[Option[Int]]))
  }

  test("isOptionType should return false for non-Option simple type") {
    assert(!QueryResultRowImplicits.isOptionType(typeOf[Int]))
  }

  test("isOptionType should return false for non-Option complex type") {
    assert(!QueryResultRowImplicits.isOptionType(typeOf[this.type]))
  }

  test("isOptionType should return false for non-Option container type") {
    assert(!QueryResultRowImplicits.isOptionType(typeOf[List[String]]))
  }
}
