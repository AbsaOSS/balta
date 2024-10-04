/*
 * Copyright 2022 ABSA Group Limited
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

package za.co.absa.db.mag.naming

/**
 *  `NamingConvention` is a base trait that defines the interface for different naming conventions.
 *  It provides methods to convert a class name according to given naming convention.
 */
trait NamingConvention {

  /**
   *  Converts the class name according to the specific naming convention.
   *  @param c - The class.
   *  @return The class name converted to string according to the specific naming convention.
   */
  def fromClassNamePerConvention(c: Class[_]): String = {
    val className = c.getSimpleName
    val cleanClassName = className.lastIndexOf('$') match {
      case -1 => className
      case x  => className.substring(0, x)
    }
    stringPerConvention(cleanClassName)
  }

  /**
   *  Converts the class name according to the specific naming convention.
   *  @param instance - The instance of the class.
   *  @return The class name converted to string according to the specific naming convention.
   */
  def fromClassNamePerConvention(instance: AnyRef): String = {
    fromClassNamePerConvention(instance.getClass)
  }

  /**
   *  Converts the original string according to the specific naming convention.
   *  @param original - The original string.
   *  @return The original string converted according to the specific naming convention.
   */
  def stringPerConvention(original: String): String
}
