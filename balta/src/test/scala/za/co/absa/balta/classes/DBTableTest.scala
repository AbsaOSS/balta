package za.co.absa.balta.classes

import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import za.co.absa.balta.classes.setter.Params

import java.sql.{Connection, PreparedStatement, ResultSet}

class DBTableTest extends AnyFlatSpec with Matchers {

  // Note: this method covers code coverage for DBTable.insert as Happy day test scenario
  "insert" should "generate correct SQL and handle results properly" in {
    val mockConn = mock(classOf[Connection])
    val mockPreparedStatement = mock(classOf[PreparedStatement])
    val mockResultSet = mock(classOf[ResultSet])
    val dbConnection = new DBConnection(mockConn)

    when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement)
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true)

    val tableName = "test_table"
    val values = Params.add("column1", "value1").add("column2", 100)
    val dbTable = DBTable(tableName)

    val result = dbTable.insert(values)(dbConnection)

    // Verify SQL query
    val expectedSQL = "INSERT INTO test_table (column1,column2) VALUES(?,?) RETURNING *;"
    verify(mockConn).prepareStatement(expectedSQL)

    // Verify interaction with PreparedStatement
    verify(mockPreparedStatement).setString(1, "value1")
    verify(mockPreparedStatement).setInt(2, 100)

    // Verify result handling
    result shouldBe a [QueryResultRow]
  }

  // TODO - Add more "insert" method's tests should cover quality aspects of the code from QA perspective.

  // TODO - Add more test to cover other class methods.

}
