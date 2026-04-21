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

package za.co.absa.db.balta.testing

import java.io.{InputStream, Reader}
import java.net.URL
import java.sql._
import java.util.Calendar

/**
 * Minimal PreparedStatement stub that records the last setter call.
 * Used by unit tests that verify QueryParamType / QueryParamValue binding without a real DB.
 */
class RecordingPreparedStatement extends PreparedStatement {
  var lastCall: (String, Any, Any) = _

  override def setBoolean(parameterIndex: Int, x: Boolean): Unit      = { lastCall = ("setBoolean", parameterIndex, x) }
  override def setInt(parameterIndex: Int, x: Int): Unit              = { lastCall = ("setInt", parameterIndex, x) }
  override def setLong(parameterIndex: Int, x: Long): Unit            = { lastCall = ("setLong", parameterIndex, x) }
  override def setString(parameterIndex: Int, x: String): Unit        = { lastCall = ("setString", parameterIndex, x) }
  override def setDouble(parameterIndex: Int, x: Double): Unit        = { lastCall = ("setDouble", parameterIndex, x) }
  override def setFloat(parameterIndex: Int, x: Float): Unit          = { lastCall = ("setFloat", parameterIndex, x) }
  override def setBigDecimal(parameterIndex: Int, x: java.math.BigDecimal): Unit = { lastCall = ("setBigDecimal", parameterIndex, x) }
  override def setDate(parameterIndex: Int, x: Date): Unit            = { lastCall = ("setDate", parameterIndex, x) }
  override def setTime(parameterIndex: Int, x: Time): Unit            = { lastCall = ("setTime", parameterIndex, x) }
  override def setObject(parameterIndex: Int, x: AnyRef): Unit        = { lastCall = ("setObject", parameterIndex, x) }

  // Remaining PreparedStatement methods — no-op stubs
  override def executeQuery(): ResultSet = null
  override def executeUpdate(): Int = 0
  override def setNull(parameterIndex: Int, sqlType: Int): Unit = ()
  override def setByte(parameterIndex: Int, x: Byte): Unit = ()
  override def setShort(parameterIndex: Int, x: Short): Unit = ()
  override def setBytes(parameterIndex: Int, x: scala.Array[Byte]): Unit = ()
  override def setTimestamp(parameterIndex: Int, x: Timestamp): Unit = ()
  override def setAsciiStream(parameterIndex: Int, x: InputStream, length: Int): Unit = ()
  override def setUnicodeStream(parameterIndex: Int, x: InputStream, length: Int): Unit = ()
  override def setBinaryStream(parameterIndex: Int, x: InputStream, length: Int): Unit = ()
  override def clearParameters(): Unit = ()
  override def setObject(parameterIndex: Int, x: AnyRef, targetSqlType: Int): Unit = ()
  override def execute(): Boolean = false
  override def addBatch(): Unit = ()
  override def setCharacterStream(parameterIndex: Int, reader: Reader, length: Int): Unit = ()
  override def setRef(parameterIndex: Int, x: Ref): Unit = ()
  override def setBlob(parameterIndex: Int, x: Blob): Unit = ()
  override def setClob(parameterIndex: Int, x: Clob): Unit = ()
  override def setArray(parameterIndex: Int, x: java.sql.Array): Unit = ()
  override def getMetaData: ResultSetMetaData = null
  override def setDate(parameterIndex: Int, x: Date, cal: Calendar): Unit = ()
  override def setTime(parameterIndex: Int, x: Time, cal: Calendar): Unit = ()
  override def setTimestamp(parameterIndex: Int, x: Timestamp, cal: Calendar): Unit = ()
  override def setNull(parameterIndex: Int, sqlType: Int, typeName: String): Unit = ()
  override def setURL(parameterIndex: Int, x: URL): Unit = ()
  override def getParameterMetaData: ParameterMetaData = null
  override def setRowId(parameterIndex: Int, x: RowId): Unit = ()
  override def setNString(parameterIndex: Int, value: String): Unit = ()
  override def setNCharacterStream(parameterIndex: Int, value: Reader, length: Long): Unit = ()
  override def setNClob(parameterIndex: Int, value: NClob): Unit = ()
  override def setClob(parameterIndex: Int, reader: Reader, length: Long): Unit = ()
  override def setBlob(parameterIndex: Int, inputStream: InputStream, length: Long): Unit = ()
  override def setNClob(parameterIndex: Int, reader: Reader, length: Long): Unit = ()
  override def setSQLXML(parameterIndex: Int, xmlObject: SQLXML): Unit = ()
  override def setObject(parameterIndex: Int, x: AnyRef, targetSqlType: Int, scaleOrLength: Int): Unit = ()
  override def setAsciiStream(parameterIndex: Int, x: InputStream, length: Long): Unit = ()
  override def setBinaryStream(parameterIndex: Int, x: InputStream, length: Long): Unit = ()
  override def setCharacterStream(parameterIndex: Int, reader: Reader, length: Long): Unit = ()
  override def setAsciiStream(parameterIndex: Int, x: InputStream): Unit = ()
  override def setBinaryStream(parameterIndex: Int, x: InputStream): Unit = ()
  override def setCharacterStream(parameterIndex: Int, reader: Reader): Unit = ()
  override def setNCharacterStream(parameterIndex: Int, value: Reader): Unit = ()
  override def setClob(parameterIndex: Int, reader: Reader): Unit = ()
  override def setBlob(parameterIndex: Int, inputStream: InputStream): Unit = ()
  override def setNClob(parameterIndex: Int, reader: Reader): Unit = ()

  // Statement methods
  override def executeQuery(sql: String): ResultSet = null
  override def executeUpdate(sql: String): Int = 0
  override def close(): Unit = ()
  override def getMaxFieldSize: Int = 0
  override def setMaxFieldSize(max: Int): Unit = ()
  override def getMaxRows: Int = 0
  override def setMaxRows(max: Int): Unit = ()
  override def setEscapeProcessing(enable: Boolean): Unit = ()
  override def getQueryTimeout: Int = 0
  override def setQueryTimeout(seconds: Int): Unit = ()
  override def cancel(): Unit = ()
  override def getWarnings: SQLWarning = null
  override def clearWarnings(): Unit = ()
  override def setCursorName(name: String): Unit = ()
  override def execute(sql: String): Boolean = false
  override def getResultSet: ResultSet = null
  override def getUpdateCount: Int = 0
  override def getMoreResults: Boolean = false
  override def setFetchDirection(direction: Int): Unit = ()
  override def getFetchDirection: Int = 0
  override def setFetchSize(rows: Int): Unit = ()
  override def getFetchSize: Int = 0
  override def getResultSetConcurrency: Int = 0
  override def getResultSetType: Int = 0
  override def addBatch(sql: String): Unit = ()
  override def clearBatch(): Unit = ()
  override def executeBatch(): scala.Array[Int] = scala.Array.empty
  override def getConnection: Connection = null
  override def getMoreResults(current: Int): Boolean = false
  override def getGeneratedKeys: ResultSet = null
  override def executeUpdate(sql: String, autoGeneratedKeys: Int): Int = 0
  override def executeUpdate(sql: String, columnIndexes: scala.Array[Int]): Int = 0
  override def executeUpdate(sql: String, columnNames: scala.Array[String]): Int = 0
  override def execute(sql: String, autoGeneratedKeys: Int): Boolean = false
  override def execute(sql: String, columnIndexes: scala.Array[Int]): Boolean = false
  override def execute(sql: String, columnNames: scala.Array[String]): Boolean = false
  override def getResultSetHoldability: Int = 0
  override def isClosed: Boolean = false
  override def setPoolable(poolable: Boolean): Unit = ()
  override def isPoolable: Boolean = false
  override def closeOnCompletion(): Unit = ()
  override def isCloseOnCompletion: Boolean = false
  override def unwrap[T](iface: Class[T]): T = throw new UnsupportedOperationException
  override def isWrapperFor(iface: Class[_]): Boolean = false
}
