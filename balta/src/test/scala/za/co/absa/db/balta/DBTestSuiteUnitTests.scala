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
import za.co.absa.db.balta.classes.inner.ConnectionInfo

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

  private class ConcreteSuite(override val persistDataOverride: Option[Boolean])
    extends DBTestSuite(persistDataOverride) {
    def exposedConnectionInfo: ConnectionInfo = connectionInfo
  }
}

