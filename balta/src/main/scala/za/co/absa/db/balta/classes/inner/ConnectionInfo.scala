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

package za.co.absa.db.balta.classes.inner

/**
 * This is a function that sets a parameter of a prepared statement.
 *
 * @param dbUrl       - the JDBC URL of the database
 * @param username    - the username to use when connecting to the database
 * @param password    - the password to use when connecting to the database
 * @param persistData - whether to persist the data to the database (usually false for tests, set to true for
 *                    debugging purposes)
 */
case class ConnectionInfo(
                           dbUrl: String,
                           username: String,
                           password: String,
                           persistData: Boolean
                         )
