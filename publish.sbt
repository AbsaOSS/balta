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

ThisBuild / scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/AbsaOSS/atum-service/tree/master"),
    connection = "scm:git:git://github.com/AbsaOSS/atum-service.git",
    devConnection = "scm:git:ssh://github.com/AbsaOSS/atum-service.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "benedeki",
    name  = "David Benedeki",
    email = "david.benedeki@absa.africa",
    url   = url("https://github.com/benedeki")
  ),
  Developer(
    id    = "miroslavpojer",
    name  = "Miroslav Pojer",
    email = "miroslav.pojer@absa.africa",
    url   = url("https://github.com/miroslavpojer")
  ),
  Developer(
    id    = "lsulak",
    name  = "Ladislav Sulak",
    email = "ladislav.sulak@absa.africa",
    url   = url("https://github.com/lsulak")
  ),
  Developer(
    id = "salamonpavel",
    name = "Pavel Salamon",
    email = "pavel.salamon@absa.africa",
    url = url("https://github.com/salamonpavel")
  )
)

ThisBuild / organization := "za.co.absa.db"
sonatypeProfileName := "za.co.absa"

ThisBuild / organizationName := "ABSA Group Limited"
ThisBuild / organizationHomepage := Some(url("https://www.absa.africa"))

ThisBuild / description := "Scala library to write Postgres DB code tests with"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")
ThisBuild / homepage := Some(url("https://github.com/AbsaOSS/atum-service"))
