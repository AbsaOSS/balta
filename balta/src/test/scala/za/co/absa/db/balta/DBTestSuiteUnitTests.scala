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

package za.co.absa.db.balta

import org.scalatest.funsuite.AnyFunSuiteLike
import za.co.absa.db.balta.classes.DBFunction.DBFunctionWithPositionedParamsOnly
import za.co.absa.db.balta.classes.DBTable
import za.co.absa.db.balta.classes.inner.ConnectionInfo
import za.co.absa.db.balta.classes.inner.Params.{NamedParams, OrderedParams}
import za.co.absa.db.balta.typeclasses.QueryParamType

class DBTestSuiteUnitTests extends AnyFunSuiteLike {

  test("connectionInfoFromResourceConfig reads local resource file correctly") {
    val connectionInfo = DBTestSuite.connectionInfoFromResourceConfig("/database.properties")
    assert(connectionInfo.dbUrl == "jdbc:postgresql://localhost:5432/mag_db")
    assert(connectionInfo.username == "mag_owner")
    assert(connectionInfo.password == "changeme")
    assert(!connectionInfo.persistData)
  }

  test("connectionInfoFromResourceConfig reads persistData true when configured") {
    val connectionInfo = DBTestSuite.connectionInfoFromResourceConfig("/database-persist.properties")
    assert(connectionInfo.persistData)
  }

  test("connectionInfo with persistDataOverride=Some(true) overrides config value") {
    val suite = new ConcreteSuite(Some(true))
    val info = suite.exposedConnectionInfo
    assert(info.persistData)
  }

  test("connectionInfo with persistDataOverride=Some(false) overrides config value") {
    val suite = new ConcreteSuite(Some(false))
    val info = suite.exposedConnectionInfo
    assert(!info.persistData)
  }

  test("connectionInfo with persistDataOverride=None uses config value") {
    val suite = new ConcreteSuite(None)
    val info = suite.exposedConnectionInfo
    assert(!info.persistData)
  }

  test("table helper creates DBTable with provided name") {
    val suite = new ConcreteSuite(None)
    val table = suite.exposedTable("my_table")
    assert(table == DBTable("my_table"))
  }

  test("function helper creates DBFunction with provided name") {
    val suite = new ConcreteSuite(None)
    val fn = suite.exposedFunction("my_fn")
    assert(fn.isInstanceOf[DBFunctionWithPositionedParamsOnly])
    assert(fn.functionName == "my_fn")
  }

  test("named add helper delegates to Params.add") {
    val suite = new ConcreteSuite(None)
    val params: NamedParams = suite.exposedAdd("id", 7)
    assert(params.size == 1)
    assert(params("id").sqlEntry == "?")
  }

  test("named addNull helper delegates to Params.addNull") {
    val suite = new ConcreteSuite(None)
    val params: NamedParams = suite.exposedAddNull("missing")
    assert(params.size == 1)
    assert(params("missing").sqlEntry == "NULL")
    assert(params("missing").equalityOperator == "IS")
  }

  test("ordered add helper delegates to Params.add") {
    val suite = new ConcreteSuite(None)
    val params: OrderedParams = suite.exposedAdd(1)
    assert(params.size == 1)
    assert(params.values.head.sqlEntry == "?")
  }

  test("ordered addNull helper delegates to Params.addNull") {
    val suite = new ConcreteSuite(None)
    val params: OrderedParams = suite.exposedAddNull[QueryParamType.NULL.type]()
    assert(params.size == 1)
    assert(params.values.head.sqlEntry == "NULL")
    assert(params.values.head.equalityOperator == "IS")
  }

  private class ConcreteSuite(override val persistDataOverride: Option[Boolean])
    extends DBTestSuite(persistDataOverride) {
    def exposedConnectionInfo: ConnectionInfo = connectionInfo
    def exposedTable(tableName: String): DBTable = table(tableName)
    def exposedFunction(functionName: String): DBFunctionWithPositionedParamsOnly = function(functionName)
    def exposedAdd[T: QueryParamType](name: String, value: T): NamedParams = add(name, value)
    def exposedAddNull(name: String): NamedParams = addNull(name)
    def exposedAdd[T: QueryParamType](value: T): OrderedParams = add(value)
    def exposedAddNull[T: QueryParamType](): OrderedParams = addNull[T]()
  }
}

