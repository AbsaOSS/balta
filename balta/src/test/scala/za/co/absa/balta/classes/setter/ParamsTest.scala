package za.co.absa.balta.classes.setter

import org.mockito.Mockito.{mock, verify}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import za.co.absa.balta.classes.setter.AllowedParamTypes._

import java.sql.PreparedStatement

class ParamsTest extends AnyFlatSpec with Matchers {

  "NamedParams" should "add and retrieve parameters correctly" in {
    val params = Params.add("param1", "value1").add("param2", 100)
    val mockPrepStmt = mock(classOf[PreparedStatement])

    params("param1").apply(mockPrepStmt, 1)
    params("param2").apply(mockPrepStmt, 2)

    params.size shouldBe 2
    params.keys shouldBe Some(List("param1", "param2"))
    verify(mockPrepStmt).setString(1, "value1")
    verify(mockPrepStmt).setInt(2, 100)
  }

  it should "handle null values correctly" in {
    val params = Params.addNull("param1")

    params.size shouldBe 1
    params("param1") shouldBe SetterFnc.nullSetterFnc
  }

  it should "correctly return pairs of parameters and their setters" in {
    val params = Params.add("param1", "value1").add("param2", 100)
    val mockPrepStmt = mock(classOf[PreparedStatement])

    params("param1").apply(mockPrepStmt, 1)
    params("param2").apply(mockPrepStmt, 2)

    val pairs = params.pairs

    pairs should contain allOf(("param1", params("param1")), ("param2", params("param2")))
    verify(mockPrepStmt).setString(1, "value1")
    verify(mockPrepStmt).setInt(2, 100)
  }

  "OrderedParams" should "add and retrieve parameters correctly" in {
    val params = Params.add("value1").add(100)
    val mockPrepStmt = mock(classOf[PreparedStatement])

    params("0").apply(mockPrepStmt, 1)
    params("1").apply(mockPrepStmt, 2)

    params.size shouldBe 2
    params.keys shouldBe None
    verify(mockPrepStmt).setString(1, "value1")
    verify(mockPrepStmt).setInt(2, 100)
  }

  ignore should "handle null values correctly" in {
    // TODO - support needed to get this running or better understanding
//    val params = Params.addNull()
//    params.size shouldBe 1
//    params("0") shouldBe SetterFnc.nullSetterFnc
  }
}
