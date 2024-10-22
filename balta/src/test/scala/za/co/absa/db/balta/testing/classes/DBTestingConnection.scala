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

package za.co.absa.db.balta.testing.classes

import za.co.absa.db.balta.classes.{DBConnection, QueryResult}
import za.co.absa.db.balta.classes.simple.ConnectionInfo

import java.time.OffsetDateTime
import java.util.Properties

trait DBTestingConnection {
  lazy implicit val connection: DBConnection = DBConnection(connectionInfo)

  protected lazy val connectionInfo: ConnectionInfo = readConnectionInfoFromConfig

  protected def now(): OffsetDateTime = {
    val preparedStatement = connection.connection.prepareStatement("SELECT now() AS now")
    val prep = preparedStatement.executeQuery()
    val result = new QueryResult(prep).next().getOffsetDateTime("now").get
    prep.close()
    result
  }

  private def readConnectionInfoFromConfig: ConnectionInfo = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("/database.properties"))

    ConnectionInfo(
      dbUrl = properties.getProperty("test.jdbc.url"),
      username = properties.getProperty("test.jdbc.username"),
      password = properties.getProperty("test.jdbc.password"),
      persistData = properties.getProperty("test.persist.db", "false").toBoolean
    )
  }
}
