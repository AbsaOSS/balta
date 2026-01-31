package za.co.absa.db.balta.postgres.typeclasses
// jacoco-touch: simulate change

import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import za.co.absa.db.balta.typeclasses.QueryParamValue.AssignFunc
import za.co.absa.db.balta.typeclasses.QueryParamValue

object PostgresQueryParamValue {
  class PostgresObjectQueryParamValue(value: String, pgType: String) extends QueryParamValue {
    private val pgObject = new PGobject()
    pgObject.setType(pgType)
    pgObject.setValue(value)

    private def assignFunc(prep: PreparedStatement, position: Int): Unit = {
      prep.setObject(position, pgObject)
    }
    override val assign: Option[AssignFunc] = Some(assignFunc)
  }


}
