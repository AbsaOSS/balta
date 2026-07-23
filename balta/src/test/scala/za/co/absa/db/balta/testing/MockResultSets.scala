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

import java.io.{InputStream, Reader}
import java.math.BigDecimal
import java.net.URL
import java.sql._

/**
 * Provides factory methods for minimal JDBC ResultSet stubs.
 * Mix into any unit test suite that needs to construct a [[za.co.absa.db.balta.classes.QueryResult]]
 * without a real database connection.
 *
 * Each factory method returns a fresh instance so tests do not share mutable state.
 */
trait MockResultSets {

  /** Returns a ResultSet that immediately reports no more rows. */
  def emptyMockResultSet: MockResultSets.MockResultSet = new MockResultSets.MockResultSet(0)

  /** Returns a ResultSet that emits exactly one row (rowNumber = 1). */
  def singleRowMockResultSet: MockResultSets.MockResultSet = new MockResultSets.MockResultSet(0) {
    private var consumed = false
    override def next(): Boolean = if (!consumed) { consumed = true; row = 1; true } else false
  }

  /** Returns a ResultSet that emits exactly two rows (rowNumbers 1 and 2). */
  def twoRowMockResultSet: MockResultSets.MockResultSet = new MockResultSets.MockResultSet(0) {
    private var count = 0
    override def next(): Boolean = if (count < 2) { count += 1; row = count; true } else false
  }

  /** Returns an empty ResultSet with the given number of columns. */
  def mockResultSetWithCols(cols: Int): MockResultSets.MockResultSet = new MockResultSets.MockResultSet(cols)
}

object MockResultSets {

  private class MockMetaData(cols: Int) extends ResultSetMetaData {
    override def getColumnCount: Int                        = cols
    override def getColumnName(column: Int): String        = s"col$column"
    override def getColumnType(column: Int): Int           = Types.VARCHAR
    override def getColumnTypeName(column: Int): String    = "varchar"
    override def isNullable(column: Int): Int              = ResultSetMetaData.columnNullable
    override def getColumnLabel(column: Int): String       = getColumnName(column)
    override def isAutoIncrement(column: Int): Boolean     = false
    override def isCaseSensitive(column: Int): Boolean     = false
    override def isSearchable(column: Int): Boolean        = false
    override def isCurrency(column: Int): Boolean          = false
    override def isSigned(column: Int): Boolean            = false
    override def getColumnDisplaySize(column: Int): Int    = 0
    override def getSchemaName(column: Int): String        = ""
    override def getPrecision(column: Int): Int            = 0
    override def getScale(column: Int): Int                = 0
    override def getTableName(column: Int): String         = ""
    override def getCatalogName(column: Int): String       = ""
    override def isReadOnly(column: Int): Boolean          = false
    override def isWritable(column: Int): Boolean          = false
    override def isDefinitelyWritable(column: Int): Boolean = false
    override def getColumnClassName(column: Int): String   = "java.lang.String"
    override def unwrap[T](iface: Class[T]): T             = throw new UnsupportedOperationException
    override def isWrapperFor(iface: Class[_]): Boolean    = false
  }

  /**
   * Minimal ResultSet stub whose `next()` always returns false.
   * Subclasses override `next()` to control row availability.
   * `getObject(Int)` returns null so that the general extractor wraps each field as None.
   */
  class MockResultSet(cols: Int) extends ResultSet {
    protected var row: Int = 0
    override def getMetaData: ResultSetMetaData                           = new MockMetaData(cols)
    override def getRow: Int                                              = row
    override def next(): Boolean                                          = false
    override def getObject(columnIndex: Int): AnyRef                     = null
    override def getObject[T](columnIndex: Int, `type`: Class[T]): T     = null.asInstanceOf[T]
    override def getArray(columnIndex: Int): java.sql.Array              = null
    override def getArray(columnLabel: String): java.sql.Array           = null
    override def close(): Unit                                            = ()
    override def wasNull(): Boolean                                       = false
    override def getString(columnIndex: Int): String                     = null
    override def getBoolean(columnIndex: Int): Boolean                   = false
    override def getByte(columnIndex: Int): Byte                         = 0
    override def getShort(columnIndex: Int): Short                       = 0
    override def getInt(columnIndex: Int): Int                           = 0
    override def getLong(columnIndex: Int): Long                         = 0L
    override def getFloat(columnIndex: Int): Float                       = 0f
    override def getDouble(columnIndex: Int): Double                     = 0d
    override def getBigDecimal(columnIndex: Int, scale: Int): BigDecimal = null
    override def getBytes(columnIndex: Int): scala.Array[Byte]           = null
    override def getDate(columnIndex: Int): Date                         = null
    override def getTime(columnIndex: Int): Time                         = null
    override def getTimestamp(columnIndex: Int): Timestamp               = null
    override def getAsciiStream(columnIndex: Int): InputStream           = null
    override def getUnicodeStream(columnIndex: Int): InputStream         = null
    override def getBinaryStream(columnIndex: Int): InputStream          = null
    override def getString(columnLabel: String): String                  = null
    override def getBoolean(columnLabel: String): Boolean                = false
    override def getByte(columnLabel: String): Byte                      = 0
    override def getShort(columnLabel: String): Short                    = 0
    override def getInt(columnLabel: String): Int                        = 0
    override def getLong(columnLabel: String): Long                      = 0L
    override def getFloat(columnLabel: String): Float                    = 0f
    override def getDouble(columnLabel: String): Double                  = 0d
    override def getBigDecimal(columnLabel: String, scale: Int): BigDecimal = null
    override def getBytes(columnLabel: String): scala.Array[Byte]        = null
    override def getDate(columnLabel: String): Date                      = null
    override def getTime(columnLabel: String): Time                      = null
    override def getTimestamp(columnLabel: String): Timestamp            = null
    override def getAsciiStream(columnLabel: String): InputStream        = null
    override def getUnicodeStream(columnLabel: String): InputStream      = null
    override def getBinaryStream(columnLabel: String): InputStream       = null
    override def getWarnings: SQLWarning                                  = null
    override def clearWarnings(): Unit                                    = ()
    override def getCursorName: String                                    = null
    override def getObject(columnLabel: String): AnyRef                  = null
    override def findColumn(columnLabel: String): Int                    = 0
    override def getCharacterStream(columnIndex: Int): Reader            = null
    override def getCharacterStream(columnLabel: String): Reader         = null
    override def getBigDecimal(columnIndex: Int): BigDecimal             = null
    override def getBigDecimal(columnLabel: String): BigDecimal          = null
    override def isBeforeFirst: Boolean                                   = false
    override def isAfterLast: Boolean                                     = false
    override def isFirst: Boolean                                         = false
    override def isLast: Boolean                                          = false
    override def beforeFirst(): Unit                                      = ()
    override def afterLast(): Unit                                        = ()
    override def first(): Boolean                                         = false
    override def last(): Boolean                                          = false
    override def absolute(row: Int): Boolean                             = false
    override def relative(rows: Int): Boolean                            = false
    override def previous(): Boolean                                     = false
    override def setFetchDirection(direction: Int): Unit                 = ()
    override def getFetchDirection: Int                                  = 0
    override def setFetchSize(rows: Int): Unit                           = ()
    override def getFetchSize: Int                                       = 0
    override def getType: Int                                            = ResultSet.TYPE_FORWARD_ONLY
    override def getConcurrency: Int                                     = ResultSet.CONCUR_READ_ONLY
    override def rowUpdated(): Boolean                                   = false
    override def rowInserted(): Boolean                                  = false
    override def rowDeleted(): Boolean                                   = false
    override def updateNull(columnIndex: Int): Unit                      = ()
    override def updateBoolean(columnIndex: Int, x: Boolean): Unit      = ()
    override def updateByte(columnIndex: Int, x: Byte): Unit            = ()
    override def updateShort(columnIndex: Int, x: Short): Unit          = ()
    override def updateInt(columnIndex: Int, x: Int): Unit              = ()
    override def updateLong(columnIndex: Int, x: Long): Unit            = ()
    override def updateFloat(columnIndex: Int, x: Float): Unit          = ()
    override def updateDouble(columnIndex: Int, x: Double): Unit        = ()
    override def updateBigDecimal(columnIndex: Int, x: BigDecimal): Unit = ()
    override def updateString(columnIndex: Int, x: String): Unit        = ()
    override def updateBytes(columnIndex: Int, x: scala.Array[Byte]): Unit = ()
    override def updateDate(columnIndex: Int, x: Date): Unit            = ()
    override def updateTime(columnIndex: Int, x: Time): Unit            = ()
    override def updateTimestamp(columnIndex: Int, x: Timestamp): Unit  = ()
    override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Int): Unit    = ()
    override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Int): Unit   = ()
    override def updateCharacterStream(columnIndex: Int, x: Reader, length: Int): Unit     = ()
    override def updateObject(columnIndex: Int, x: AnyRef, scaleOrLength: Int): Unit       = ()
    override def updateObject(columnIndex: Int, x: AnyRef): Unit        = ()
    override def updateNull(columnLabel: String): Unit                   = ()
    override def updateBoolean(columnLabel: String, x: Boolean): Unit   = ()
    override def updateByte(columnLabel: String, x: Byte): Unit         = ()
    override def updateShort(columnLabel: String, x: Short): Unit       = ()
    override def updateInt(columnLabel: String, x: Int): Unit           = ()
    override def updateLong(columnLabel: String, x: Long): Unit         = ()
    override def updateFloat(columnLabel: String, x: Float): Unit       = ()
    override def updateDouble(columnLabel: String, x: Double): Unit     = ()
    override def updateBigDecimal(columnLabel: String, x: BigDecimal): Unit = ()
    override def updateString(columnLabel: String, x: String): Unit     = ()
    override def updateBytes(columnLabel: String, x: scala.Array[Byte]): Unit = ()
    override def updateDate(columnLabel: String, x: Date): Unit         = ()
    override def updateTime(columnLabel: String, x: Time): Unit         = ()
    override def updateTimestamp(columnLabel: String, x: Timestamp): Unit = ()
    override def updateAsciiStream(columnLabel: String, x: InputStream, length: Int): Unit   = ()
    override def updateBinaryStream(columnLabel: String, x: InputStream, length: Int): Unit  = ()
    override def updateCharacterStream(columnLabel: String, x: Reader, length: Int): Unit    = ()
    override def updateObject(columnLabel: String, x: AnyRef, scaleOrLength: Int): Unit      = ()
    override def updateObject(columnLabel: String, x: AnyRef): Unit     = ()
    override def insertRow(): Unit                                       = ()
    override def updateRow(): Unit                                       = ()
    override def deleteRow(): Unit                                       = ()
    override def refreshRow(): Unit                                      = ()
    override def cancelRowUpdates(): Unit                                = ()
    override def moveToInsertRow(): Unit                                 = ()
    override def moveToCurrentRow(): Unit                                = ()
    override def getStatement: Statement                                 = null
    override def getObject(columnIndex: Int, map: java.util.Map[String, Class[_]]): AnyRef = null
    override def getRef(columnIndex: Int): Ref                          = null
    override def getBlob(columnIndex: Int): Blob                        = null
    override def getClob(columnIndex: Int): Clob                        = null
    override def getObject(columnLabel: String, map: java.util.Map[String, Class[_]]): AnyRef = null
    override def getRef(columnLabel: String): Ref                       = null
    override def getBlob(columnLabel: String): Blob                     = null
    override def getClob(columnLabel: String): Clob                     = null
    override def getDate(columnIndex: Int, cal: java.util.Calendar): Date   = null
    override def getDate(columnLabel: String, cal: java.util.Calendar): Date = null
    override def getTime(columnIndex: Int, cal: java.util.Calendar): Time   = null
    override def getTime(columnLabel: String, cal: java.util.Calendar): Time = null
    override def getTimestamp(columnIndex: Int, cal: java.util.Calendar): Timestamp   = null
    override def getTimestamp(columnLabel: String, cal: java.util.Calendar): Timestamp = null
    override def getURL(columnIndex: Int): URL                          = null
    override def getURL(columnLabel: String): URL                       = null
    override def updateRef(columnIndex: Int, x: Ref): Unit              = ()
    override def updateRef(columnLabel: String, x: Ref): Unit           = ()
    override def updateBlob(columnIndex: Int, x: Blob): Unit            = ()
    override def updateBlob(columnLabel: String, x: Blob): Unit         = ()
    override def updateClob(columnIndex: Int, x: Clob): Unit            = ()
    override def updateClob(columnLabel: String, x: Clob): Unit         = ()
    override def updateArray(columnIndex: Int, x: java.sql.Array): Unit = ()
    override def updateArray(columnLabel: String, x: java.sql.Array): Unit = ()
    override def getRowId(columnIndex: Int): RowId                      = null
    override def getRowId(columnLabel: String): RowId                   = null
    override def updateRowId(columnIndex: Int, x: RowId): Unit          = ()
    override def updateRowId(columnLabel: String, x: RowId): Unit       = ()
    override def getHoldability: Int                                     = 0
    override def isClosed: Boolean                                       = false
    override def updateNString(columnIndex: Int, nString: String): Unit  = ()
    override def updateNString(columnLabel: String, nString: String): Unit = ()
    override def updateNClob(columnIndex: Int, nClob: NClob): Unit       = ()
    override def updateNClob(columnLabel: String, nClob: NClob): Unit    = ()
    override def getNClob(columnIndex: Int): NClob                       = null
    override def getNClob(columnLabel: String): NClob                    = null
    override def getSQLXML(columnIndex: Int): SQLXML                     = null
    override def getSQLXML(columnLabel: String): SQLXML                  = null
    override def updateSQLXML(columnIndex: Int, xmlObject: SQLXML): Unit  = ()
    override def updateSQLXML(columnLabel: String, xmlObject: SQLXML): Unit = ()
    override def getNString(columnIndex: Int): String                    = null
    override def getNString(columnLabel: String): String                 = null
    override def getNCharacterStream(columnIndex: Int): Reader           = null
    override def getNCharacterStream(columnLabel: String): Reader        = null
    override def updateNCharacterStream(columnIndex: Int, x: Reader, length: Long): Unit    = ()
    override def updateNCharacterStream(columnLabel: String, x: Reader, length: Long): Unit = ()
    override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Long): Unit    = ()
    override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Long): Unit   = ()
    override def updateCharacterStream(columnIndex: Int, x: Reader, length: Long): Unit     = ()
    override def updateAsciiStream(columnLabel: String, x: InputStream, length: Long): Unit  = ()
    override def updateBinaryStream(columnLabel: String, x: InputStream, length: Long): Unit = ()
    override def updateCharacterStream(columnLabel: String, x: Reader, length: Long): Unit   = ()
    override def updateBlob(columnIndex: Int, inputStream: InputStream, length: Long): Unit  = ()
    override def updateBlob(columnLabel: String, inputStream: InputStream, length: Long): Unit = ()
    override def updateClob(columnIndex: Int, reader: Reader, length: Long): Unit    = ()
    override def updateClob(columnLabel: String, reader: Reader, length: Long): Unit = ()
    override def updateNClob(columnIndex: Int, reader: Reader, length: Long): Unit    = ()
    override def updateNClob(columnLabel: String, reader: Reader, length: Long): Unit = ()
    override def updateNCharacterStream(columnIndex: Int, x: Reader): Unit    = ()
    override def updateNCharacterStream(columnLabel: String, x: Reader): Unit = ()
    override def updateAsciiStream(columnIndex: Int, x: InputStream): Unit    = ()
    override def updateBinaryStream(columnIndex: Int, x: InputStream): Unit   = ()
    override def updateCharacterStream(columnIndex: Int, x: Reader): Unit     = ()
    override def updateAsciiStream(columnLabel: String, x: InputStream): Unit  = ()
    override def updateBinaryStream(columnLabel: String, x: InputStream): Unit = ()
    override def updateCharacterStream(columnLabel: String, x: Reader): Unit   = ()
    override def updateBlob(columnIndex: Int, inputStream: InputStream): Unit  = ()
    override def updateBlob(columnLabel: String, inputStream: InputStream): Unit = ()
    override def updateClob(columnIndex: Int, reader: Reader): Unit    = ()
    override def updateClob(columnLabel: String, reader: Reader): Unit = ()
    override def updateNClob(columnIndex: Int, reader: Reader): Unit    = ()
    override def updateNClob(columnLabel: String, reader: Reader): Unit = ()
    override def getObject[T](columnLabel: String, `type`: Class[T]): T = null.asInstanceOf[T]
    override def unwrap[T](iface: Class[T]): T = throw new UnsupportedOperationException
    override def isWrapperFor(iface: Class[_]): Boolean = false
  }
}
