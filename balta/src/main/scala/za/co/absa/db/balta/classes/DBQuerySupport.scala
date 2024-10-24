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

package za.co.absa.db.balta.classes

import za.co.absa.db.balta.classes.setter.SetterFnc

/**
 * This is a based trait providing the ability to run an SQL query and verify the result via a provided function.
 */
trait DBQuerySupport {

  protected def runQuery[R](sql: String, setters: List[SetterFnc])
                 (verify: QueryResult => R /* Assertion */)
                 (implicit connection: DBConnection): R = {
    val preparedStatement = connection.connection.prepareStatement(sql)

    setters.foldLeft(1) { case (parameterIndex, fnc) =>
      fnc(preparedStatement, parameterIndex)
      if (fnc.setsToNull) parameterIndex //null values is entered as constant, not as parameter
      else parameterIndex + 1
    }

    val result = preparedStatement.executeQuery()
    try {
      verify(new QueryResult(result))
    } finally {
      result.close()
    }
  }
}
