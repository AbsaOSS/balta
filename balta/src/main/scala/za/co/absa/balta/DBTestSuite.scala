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

package za.co.absa.balta

import org.scalactic.source
import org.scalatest.Tag
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.balta.classes.DBFunction.DBFunctionWithPositionedParamsOnly
import za.co.absa.balta.classes.setter.{AllowedParamTypes, Params}
import za.co.absa.balta.classes.setter.Params.{NamedParams, OrderedParams}
import za.co.absa.balta.classes.{ConnectionInfo, DBConnection, DBFunction, DBTable, QueryResult}

import java.sql.DriverManager
import java.time.OffsetDateTime
import java.util.Properties

/**
 * This is a base class for all DB tests. It inherits from AnyFunSuite and provides the following:
 * * automatic creation and provision of a DB connection
 * * an enhanced test function that automatically rolls back the transaction after the test is finished
 * * easy access to DB tables and functions
 * * the now() function that returns the current transaction time in the DB
 */
abstract class DBTestSuite extends AnyFunSuite {

  /* the DB connection is ``lazy`, so it actually can be created only when needed and therefore the credentials
  overridden in the successor */
  protected lazy implicit val dbConnection: DBConnection = {
    createConnection(
      connectionInfo.dbUrl,
      connectionInfo.username,
      connectionInfo.password
    )
  }

  /**
   * This is the connection info for the DB. It can be overridden in the derived classes to provide specific credentials
   */
  protected lazy val connectionInfo: ConnectionInfo = readConnectionInfoFromConfig

  /**
   * This is an enhanced test function that automatically rolls back the transaction after the test is finished
   *
   * @param testName – the name of the test
   * @param testTags – the optional list of tags for this test
   * @param testFun – the test function
   */
  override protected def test(testName: String, testTags: Tag*)
                      (testFun: => Any /* Assertion */)
                      (implicit pos: source.Position): Unit = {
    val dbTestFun = {
      try {
        testFun
      }
      finally {
        if (connectionInfo.persistData) {
          dbConnection.connection.commit()
        } else {
          dbConnection.connection.rollback()
        }
      }
    }
    super.test(testName, testTags: _*)(dbTestFun)
  }

  /**
   * This is a helper function that allows to easily access a DB table
   * @param tableName - the name of the table
   * @return          - the DBTable object
   */
  protected def table(tableName: String): DBTable = {
    DBTable(tableName)
  }

  /**
   * This is a helper function that allows to easily access a DB function
   * @param functionName - the name of the function
   * @return             - the DBFunction object
   */
  protected def function(functionName: String): DBFunctionWithPositionedParamsOnly = {
    DBFunction(functionName)
  }

  /**
   * This is a helper function that allows to easily get the DB current time
   * @param connection  - the DB connection
   * @return            - the current transaction time
   */
  protected def now()(implicit connection: DBConnection): OffsetDateTime = {
    val preparedStatement = connection.connection.prepareStatement("SELECT now() AS now")
    val prep = preparedStatement.executeQuery()
    val result = new QueryResult(prep).next().getOffsetDateTime("now").get
    prep.close()
    result
  }

  /**
   * This is a helper function that allows to easily create parameter for table and function queries
   *
   * @param paramName - the name of the parameter
   * @param value     - the value of the parameter
   * @tparam T        - the type of the parameter value
   * @return          - a list parameters to be used in an SQL prepared statement
   */
  protected def add[T: AllowedParamTypes](paramName: String, value: T): NamedParams = {
    Params.add(paramName, value)
  }

  /**
   * This is a helper function that allows to easily create parameter of value NULL for table and function queries
   *
   * @param paramName - the name of the parameter
   * @return          - a list parameters to be used in an SQL prepared statement
   */
  protected def addNull(paramName: String): NamedParams = {
    Params.addNull(paramName)
  }

  /**
   * This is a helper function that allows to easily create a positioned parameter for table and function queries
   *
   * @param value - the value of the parameter
   * @tparam T    - the type of the parameter value
   * @return      - a list parameters to be used in an SQL prepared statement
   */
  protected def add[T: AllowedParamTypes](value: T): OrderedParams = {
    Params.add(value)
  }

  /**
   * This is a helper function that allows to easily create a positioned parameter of value NULL for table and function queries
   *
   * @tparam T - the type of the parameter value
   * @return   - a list parameters to be used in an SQL prepared statement
   */
  protected def addNull[T: AllowedParamTypes](): OrderedParams = {
    Params.addNull()
  }

  // private functions
  private def createConnection(url: String, username: String, password: String): DBConnection = {
    val conn = DriverManager.getConnection(url, username, password)
    conn.setAutoCommit(false)
    new DBConnection(conn)
  }

  private def readConnectionInfoFromConfig = {
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
