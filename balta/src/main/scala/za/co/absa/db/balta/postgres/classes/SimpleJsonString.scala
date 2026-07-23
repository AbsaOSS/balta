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

package za.co.absa.db.balta.postgres.classes

import scala.language.implicitConversions
import za.co.absa.db.balta.typeclasses.QueryParamType
import za.co.absa.db.balta.typeclasses.QueryParamValue
import za.co.absa.db.balta.postgres.typeclasses.PostgresQueryParamValue.PostgresObjectQueryParamValue

/**
 * A simple class to signal a JSON string is expected. No validation is performed. No extended comparison functionality
 * is provided.
 * @param value A JSON string.
 */
case class SimpleJsonString(value: String) extends AnyVal

object SimpleJsonString {
  implicit def fromString(value: String): SimpleJsonString = SimpleJsonString(value)

  implicit object ParamTypeSimpleJsonString extends QueryParamType[SimpleJsonString] {
    override def toQueryParamValue(value: SimpleJsonString): QueryParamValue = new PostgresObjectQueryParamValue(value.value, "json")
  }
}


