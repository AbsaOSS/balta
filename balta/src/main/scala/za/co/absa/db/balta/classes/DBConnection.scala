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

import scala.language.implicitConversions
import za.co.absa.db.balta.classes.inner.ConnectionInfo

import java.sql.{Connection, DriverManager}

class DBConnection(val connection: Connection) extends AnyVal

object DBConnection {
  def apply(connectionInfo: =>ConnectionInfo): DBConnection = {
    val connection = DriverManager.getConnection(connectionInfo.dbUrl, connectionInfo.username, connectionInfo.password)
    connection.setAutoCommit(false)
    new DBConnection(connection)
  }

  def apply(dbUrl: String, username: String, password: String, persistData: Boolean = false): DBConnection =
    apply(ConnectionInfo(dbUrl, username, password, persistData)
)

  /**
   * This implicit conversion allows to use a DBConnection at any place where as a JDBC Connection is required.
   */
  implicit def dbConnectionToJdbcConnection(in: DBConnection): Connection = in.connection

}

