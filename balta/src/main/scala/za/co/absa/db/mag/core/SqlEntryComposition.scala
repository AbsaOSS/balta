package za.co.absa.db.mag.core

import scala.language.implicitConversions
import za.co.absa.db.mag.core.SqlEntry._

object SqlEntryComposition {

  sealed class SelectFragment private[SqlEntryComposition]() {
    def apply(firstField: ColumnReference, fields: ColumnReference*): SelectWithFieldsFragment = {
      val allFields = firstField +: fields
      new SelectWithFieldsFragment(select + allFields.map(_.sqlEntry).mkSqlEntry(", "))
    }

    def apply(sqlConstant: SqlEntryConstant): SelectWithFieldsFragment = new SelectWithFieldsFragment(select + sqlConstant.sqlConstant)
  }

  sealed class InsertFragment private[SqlEntryComposition]() {
    def INTO(intoEntry: SqlEntry): QueryInsertIntoFragment = {
      new QueryInsertIntoFragment(insertInto + intoEntry)
    }
  }

  sealed class QueryInsertIntoFragment private[SqlEntryComposition](sqlEntry: SqlEntry) {
    def VALUES(firstValue: SqlEntry, otherValues: SqlEntry*): QueryInsert = {
      VALUES(firstValue +: otherValues)
    }
    def VALUES(values: Seq[SqlEntry]): QueryInsert = {
      val valuesEntry = values.mkSqlEntry("VALUES(", ", ", ")")
      new QueryInsert(sqlEntry + valuesEntry)
    }
  }

  sealed class DeleteFragment private[SqlEntryComposition]() {
    def FROM(fromEntry: SqlEntry): QueryDelete = new QueryDelete(deleteFrom + fromEntry)
  }

  private object SelectFragment extends SelectFragment()
  private object InsertFragment extends InsertFragment()
  private object DeleteFragment extends DeleteFragment

  sealed class SelectWithFieldsFragment private[SqlEntryComposition](val sql: SqlEntry) {
    def FROM(fromEntry: SqlEntry): QuerySelect = new QuerySelect(sql + from + fromEntry)
  }

  sealed class OrderByFragment private[SqlEntryComposition](orderingEntry: Option[SqlEntry]) {
    val sqlEntry: Option[SqlEntry] = orderingEntry.prefix(orderBy)
  }

  sealed trait OrderByMixIn {
    def sqlEntry: SqlEntry
    def ORDER(by: OrderByFragment): QueryComplete = new QueryComplete(sqlEntry + by.sqlEntry)
  }

  sealed trait ReturningMixIn {
    def sqlEntry: SqlEntry
    def RETURNING(returningFields: SqlEntryConstant): QueryWithReturning = {
      new QueryWithReturning(sqlEntry + returning + returningFields.sqlConstant)
    }
    def RETURNING(firstField: ColumnReference, otherFields: ColumnReference*): QueryWithReturning = {
      val allFields = firstField +: otherFields
      new QueryWithReturning(sqlEntry + returning + columnsToSqlEntry(allFields))
    }
  }

  sealed class Query(val sqlEntry: SqlEntry)

  sealed class QuerySelect private[SqlEntryComposition](sqlEntry: SqlEntry)
    extends Query(sqlEntry) with OrderByMixIn {
    def WHERE(condition: SqlEntry): QuerySelectConditioned = WHERE(condition.toOption)
    def WHERE(condition: Option[SqlEntry]): QuerySelectConditioned = {
      new QuerySelectConditioned(sqlEntry + condition.prefix(where))
    }
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
      new QueryDeleteConditioned(sqlEntry + condition.prefix(where))
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
  def BY(columns: ColumnReference*): OrderByFragment = new OrderByFragment(columnsToSqlEntry(columns))

  implicit def QueryToSqlEntry(query: Query): SqlEntry = query.sqlEntry
  implicit def StringToSqlEntry(string: String): SqlEntry = SqlEntry(string)

  private val select = SqlEntry("SELECT")
  private val insertInto = SqlEntry("INSERT INTO")
  private val deleteFrom = SqlEntry("DELETE FROM")
  private val from = SqlEntry("FROM")
  private val where = SqlEntry("WHERE")
  private val orderBy = SqlEntry("ORDER BY")
  private val returning = SqlEntry("RETURNING")

  private def columnsToSqlEntry(fields: Seq[ColumnReference]): Option[SqlEntry] = {
    if (fields.isEmpty) {
      None
    } else {
      val fieldEntries = fields.map(_.sqlEntry.entry)
      Some(SqlEntry(fieldEntries.mkString(", ")))
    }
  }

}
