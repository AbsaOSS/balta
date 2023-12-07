package za.co.absa.balta

import org.mockito.Mockito.{doNothing, mock, verify}
import za.co.absa.balta.classes.{ConnectionInfo, DBConnection}

import java.sql.Connection

class RollbackDBTestSuiteTest extends DBTestSuite {

  override protected lazy val connectionInfo: ConnectionInfo = ConnectionInfo(
    dbUrl = "url",
    username = "user",
    password = "pass",
    persistData = false
  )

  // Override the dbConnection to use a mock connection
  override protected lazy val dbConnection: DBConnection = {
    val mockConnection = mock(classOf[Connection])
    doNothing().when(mockConnection).setAutoCommit(false)
    doNothing().when(mockConnection).commit()
    doNothing().when(mockConnection).rollback()
    new DBConnection(mockConnection)
  }

  test("Transaction handling based on persistData flag - rollback") {

    test("another dummy test") {}

    verify(dbConnection.connection).rollback()
  }

  // Add tests for:
  //   - the table, function, now, add, and addNull methods to ensure they behave as expected.
  //   - test reading Connection Info from Config file
}
