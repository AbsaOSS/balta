package za.co.absa.balta.classes.setter

import org.mockito.Mockito.verify
import org.postgresql.util.PGobject
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import za.co.absa.balta.classes.JsonBString

import java.sql.{Date, PreparedStatement, Time, Types}
import java.time.{Instant, LocalDate, LocalTime, OffsetDateTime, ZoneOffset}
import java.util.UUID

class SetterFncTest extends AnyFlatSpec with Matchers with MockitoSugar {

  "SetterFnc" should "handle Boolean correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(true)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setBoolean(1, true)
  }

  it should "handle Int correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(123)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setInt(1, 123)
  }

  it should "handle Long correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(123L)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setLong(1, 123L)
  }

  it should "handle Double correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(123.456)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setDouble(1, 123.456)
  }

  it should "handle Float correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(123.456f)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setFloat(1, 123.456f)
  }

  it should "handle BigDecimal correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(BigDecimal(123.456))
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setBigDecimal(1, BigDecimal(123.456).bigDecimal)
  }

  it should "handle Char correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc('a')
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setString(1, "a")
  }

  it should "handle String correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc("abc")
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setString(1, "abc")
  }

  it should "handle Instant correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val setter = SetterFnc.createSetterFnc(Instant.parse("2021-09-01T12:34:56.789Z"))
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setObject(1, OffsetDateTime.ofInstant(Instant.parse("2021-09-01T12:34:56.789Z"), ZoneOffset.UTC))
  }

  it should "handle OffsetDateTime correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val offsetDateTime = OffsetDateTime.parse("2021-09-01T12:34:56.789+01:00")
    val setter = SetterFnc.createSetterFnc(offsetDateTime)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setObject(1, offsetDateTime)
  }

  it should "handle LocalTime correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val localTime = LocalTime.parse("12:34:56")
    val setter = SetterFnc.createSetterFnc(localTime)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setTime(1, Time.valueOf(localTime))
  }

  it should "handle LocalDate correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val localDate = LocalDate.parse("2021-09-01")
    val setter = SetterFnc.createSetterFnc(localDate)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setDate(1, Date.valueOf(localDate))
  }

  it should "handle UUID correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val uuid = UUID.randomUUID()
    val setter = SetterFnc.createSetterFnc(uuid)
    setter(mockPrepStmt, 1)
    verify(mockPrepStmt).setObject(1, uuid)
  }

  it should "handle JsonBString correctly" in {
    val mockPrepStmt = mock[PreparedStatement]
    val jsonBString = JsonBString("""{"key": "value"}""")
    val setter = SetterFnc.createSetterFnc(jsonBString)
    setter(mockPrepStmt, 1)

    val jsonObject = new PGobject()
    jsonObject.setType("jsonb")
    jsonObject.setValue(jsonBString.value)

    verify(mockPrepStmt).setObject(1, jsonObject)
  }

  it should "handle CustomDBType correctly" in {
    // TODO - more type related tests needed here
    val mockPrepStmt = mock[PreparedStatement]
    val customValue = "customValue"
    val customType = "customType"
    val customDBType = CustomDBType(customValue, customType)
    val setter = SetterFnc.createSetterFnc(customDBType)
    setter(mockPrepStmt, 1)

    verify(mockPrepStmt).setString(1, customValue)
    setter.sqlEntry shouldBe s"?::$customType"
  }
  
  "nullSetterFnc" should "set a null value correctly on PreparedStatement" in {
    val mockPrepStmt = mock[PreparedStatement]
    val nullSetter = SetterFnc.nullSetterFnc

    // Apply the nullSetterFnc to the mocked PreparedStatement
    nullSetter(mockPrepStmt, 1)

    // Verify that setNull was called with the correct position and SQL type
    verify(mockPrepStmt).setNull(1, Types.NULL)
  }
}
