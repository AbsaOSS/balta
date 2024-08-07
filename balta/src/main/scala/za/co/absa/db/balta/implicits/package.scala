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

import za.co.absa.db.balta.classes.DBConnection

import java.sql.Connection
import scala.language.implicitConversions

package object implicits {

  /**
   * This implicit conversion allows to use a DBConnection at any place where as a JDBC Connection is required.
   */
  implicit def dbConnectionToJdbcConnection(in: DBConnection): Connection = in.connection

}
