package za.co.absa.balta.implicits

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import za.co.absa.balta.classes.DBConnection

import java.sql.Connection

class ImplicitConversionTest extends AnyFlatSpec with Matchers with MockitoSugar {

  "DBConnection" should "implicitly convert to java.sql.Connection" in {
    val mockConnection = mock[Connection]
    val dbConnection = new DBConnection(mockConnection)

    // Test implicit conversion
    val connection: Connection = dbConnection
    connection shouldBe a [Connection]
  }

  it should "be usable wherever java.sql.Connection is expected" in {
    val mockConnection = mock[Connection]
    val dbConnection = new DBConnection(mockConnection)

    def requiresSqlConnection(conn: Connection): Boolean = true

    // DBConnection should be implicitly converted and accepted by the method
    requiresSqlConnection(dbConnection) shouldBe true
  }

  it should "handle null DBConnection appropriately" in {
    val dbConnection = new DBConnection(null)

    noException should be thrownBy {
      val connection: Connection = dbConnection
      connection shouldBe null
    }
  }
}
