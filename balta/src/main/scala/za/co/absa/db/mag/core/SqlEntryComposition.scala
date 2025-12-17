package za.co.absa.db.mag.core

import scala.language.implicitConversions

object SqlEntryComposition {

  private def columnsToSqlEntry(fields: Seq[ColumnReference]): Option[SqlEntry] = {
    if (fields.isEmpty) {
      None
    } else {
      val fieldEntries = fields.map(_.sqlEntry.entry)
      Some(SqlEntry(fieldEntries.mkString(", ")))
    }
  }

  sealed class SelectFragment private[SqlEntryComposition]() {
    private val select = SqlEntry("SELECT")

    def apply(firstField: ColumnReference, fields: ColumnReference*): SelectWithFieldsFragment = {
      val allFields = firstField +: fields
      new SelectWithFieldsFragment(select + columnsToSqlEntry(allFields))
    }

    def apply(sqlConstant: SqlEntryConstant): SelectWithFieldsFragment = new SelectWithFieldsFragment(select + sqlConstant.sqlConstant)
  }

  sealed class InsertFragment private[SqlEntryComposition]() {
    def INTO(intoEntry: SqlEntry): QueryInsertIntoFragment = {
      new QueryInsertIntoFragment(SqlEntry("INSERT INTO") + intoEntry)
    }
  }

  sealed class QueryInsertIntoFragment private[SqlEntryComposition](sqlEntry: SqlEntry) {
    def VALUES(firstValue: String, otherValues: String*): QueryInsert = {
      val valuesLine = (firstValue +: otherValues).mkString(", ")
      new QueryInsert(sqlEntry + SqlEntry(s"VALUES($valuesLine)"))
    }
  }

  sealed class DeleteFragment private[SqlEntryComposition]() {
    def FROM(fromEntry: SqlEntry): QueryDelete = new QueryDelete(SqlEntry("DELETE FROM") + fromEntry)
  }

  private object SelectFragment extends SelectFragment()
  private object InsertFragment extends InsertFragment()
  private object DeleteFragment extends DeleteFragment

  sealed class SelectWithFieldsFragment private[SqlEntryComposition](val sql: SqlEntry) {
    def FROM(fromEntry: SqlEntry): QuerySelect = new QuerySelect(sql + SqlEntry("FROM") + fromEntry)
  }

  sealed class OrderByFragment private[SqlEntryComposition](orderingEntry: Option[SqlEntry]) {
    val sqlEntry: Option[SqlEntry] = orderingEntry.prefix("ORDER BY")
  }

  trait OrderByMixIn {
    def sqlEntry: SqlEntry
    def ORDER(by: OrderByFragment): QueryComplete = new QueryComplete(sqlEntry + by.sqlEntry)
  }

  trait ReturningMixIn {
    def sqlEntry: SqlEntry
    def RETURNING(returning: SqlEntryConstant): QueryWithReturning = {
      new QueryWithReturning(sqlEntry + SqlEntry("RETURNING") + returning.sqlConstant)
    }
    def RETURNING(firstField: ColumnReference, otherFields: ColumnReference*): QueryWithReturning = {
      val allFields = firstField +: otherFields
      new QueryWithReturning(sqlEntry + SqlEntry("RETURNING") + columnsToSqlEntry(allFields))
    }
  }

  sealed protected class Query(val sqlEntry: SqlEntry)

  sealed class QuerySelect private[SqlEntryComposition](sqlEntry: SqlEntry)
    extends Query(sqlEntry) with OrderByMixIn {
    def WHERE(condition: SqlEntry): QuerySelectConditioned = WHERE(condition.toOption)
    def WHERE(condition: Option[SqlEntry]): QuerySelectConditioned = {
      new QuerySelectConditioned(sqlEntry + condition.prefix("WHERE"))
    }
//    def apply(paramsLine: String): QuerySelectWithParams = new QuerySelectWithParams(sqlEntry + SqlEntry(s"($paramsLine)"))
  }

  sealed class QuerySelectConditioned private[SqlEntryComposition](sqlEntry: SqlEntry)
    extends Query(sqlEntry) with OrderByMixIn {
  }

  sealed class QueryInsert private[SqlEntryComposition](sqlEntry: SqlEntry)
    extends Query(sqlEntry) with ReturningMixIn {
  }

  sealed class QueryDelete private[SqlEntryComposition](sqlEntry: SqlEntry) extends Query(sqlEntry) with ReturningMixIn {
    def WHERE(condition: SqlEntry): QueryDeleteConditioned = WHERE(condition.toOption)
    def WHERE(condition: Option[SqlEntry]): QueryDeleteConditioned = {
      new QueryDeleteConditioned(sqlEntry + condition.prefix("WHERE"))
    }
  }

  sealed class QueryDeleteConditioned private[SqlEntryComposition](sqlEntry: SqlEntry)
    extends Query(sqlEntry) with ReturningMixIn {
  }

  sealed class QueryComplete(sqlEntry: SqlEntry) extends Query(sqlEntry)

  sealed class QueryWithReturning(sqlEntry: SqlEntry) extends Query(sqlEntry)

  sealed class SqlEntryConstant private[mag](val sqlConstant: SqlEntry)

  val ALL: SqlEntryConstant = new SqlEntryConstant(SqlEntry("*"))
  val COUNT_ALL: SqlEntryConstant = new SqlEntryConstant(SqlEntry("count(1) AS cnt"))

  def SELECT: SelectFragment = SelectFragment
  def INSERT: InsertFragment = InsertFragment
  def DELETE: DeleteFragment = DeleteFragment
  def BY(by: SqlEntry): OrderByFragment = BY(by.toOption)
  def BY(by: Option[SqlEntry]): OrderByFragment = new OrderByFragment(by)
  def BY(columns: ColumnReference*): OrderByFragment= new OrderByFragment(columnsToSqlEntry(columns))

  implicit def QueryToSqlEntry(query: Query): SqlEntry = query.sqlEntry

}
