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

import Dependencies._
import com.github.sbt.jacoco.report.JacocoReportSettings

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.18"
lazy val scala213 = "2.13.11"

lazy val supportedScalaVersions: Seq[String] = Seq(scala211, scala212 , scala213)

name := "balta"

ThisBuild / scalaVersion := scala212

ThisBuild / versionScheme := Some("early-semver")

lazy val commonJacocoReportSettings: JacocoReportSettings = JacocoReportSettings(
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

lazy val commonJacocoExcludes: Seq[String] = Seq()

lazy val balta = (project in file("balta"))
  .settings(
    name := "balta",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= libDependencies,
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"balta - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )
