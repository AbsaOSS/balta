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

import za.co.absa.db.mag.exceptions.NamingException
import za.co.absa.db.mag.naming.NamingConvention

/**
 *  `ExplicitNamingRequired` is a `NamingConvention` that throws a `NamingException` for any string.
 *  This is used when explicit naming is required and no other naming convention should be applied.
 */
class ExplicitNamingRequired extends NamingConvention {

  /**
   *  Throws a `NamingConvention` with a message indicating that explicit naming is required.
   *  @param original - The original string.
   *  @return Nothing, as a `NamingException` is always thrown.
   */
  override def stringPerConvention(original: String): String = {
    val message = s"No convention for '$original', explicit naming required."
    throw NamingException(message)
  }
}

/**
 *  `ExplicitNamingRequired.Implicits` provides an implicit `NamingConvention` instance that
 *  throws a `NamingException` for any string.
 */
object ExplicitNamingRequired {
  object Implicits {

    /**
     *  An implicit `NamingConvention` instance that throws a `NamingException` for any string.
     */
    implicit val namingConvention: NamingConvention = new ExplicitNamingRequired()
  }
}
