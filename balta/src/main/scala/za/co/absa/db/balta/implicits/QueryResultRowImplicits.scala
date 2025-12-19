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

package za.co.absa.db.balta.implicits

import za.co.absa.db.balta.classes.QueryResultRow
import za.co.absa.db.balta.implicits.OptionImplicits.OptionEnhancements
import za.co.absa.db.mag.naming.NamingConvention

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

object QueryResultRowImplicits {

  def isOptionType(typeToCheck: Type): Boolean = {
    typeToCheck <:< typeOf[Option[_]]
  }

  /**
   * This class provides an implicit conversion from QueryResultRow to a case class
   * This logic placed in an implicit class to prevent polluting the QueryResultRow class with too much unrelated logic
   * @param row The QueryResultRow to convert
   */
  implicit class ProductTypeConvertor(val row: QueryResultRow) extends AnyVal {

    /**
     * Converts a QueryResultRow to a case class
     * @param namingConvention  - The naming convention to use when converting field names to column names
     * @tparam T                - The case class to convert to
     * @return                  - The case class instance filled with data from the QueryResultRow
     */
    def toProductType[T <: Product : TypeTag](implicit namingConvention: NamingConvention): T = {
      val tpe = typeOf[T]
      val defaultConstructor = getConstructor(tpe)
      val constructorMirror = getConstructorMirror(tpe, defaultConstructor)
      val params = readParamsFromRow(defaultConstructor)
      constructorMirror(params: _*).asInstanceOf[T]
    }

    private def getConstructor(tpe: Type): MethodSymbol = {
      val constructorSymbol = tpe.decl(termNames.CONSTRUCTOR)
      val defaultConstructor =
        if (constructorSymbol.isMethod) constructorSymbol.asMethod
        else {
          val ctors = constructorSymbol.asTerm.alternatives
          ctors.map(_.asMethod).find(_.isPrimaryConstructor).get
        }
      defaultConstructor
    }

    private def getConstructorMirror(tpe: Type, constructor: MethodSymbol): MethodMirror = {
      val classSymbol = tpe.typeSymbol.asClass
      val classMirror = currentMirror.reflectClass(classSymbol)
      val constructorMirror = classMirror.reflectConstructor(constructor)
      constructorMirror
    }

    private def readParamsFromRow(constructor: MethodSymbol)(implicit namingConvention: NamingConvention): List[Any] = {
      constructor.paramLists.flatten.map { param =>
        val name = param.name.decodedName.toString
        val paramType = param.typeSignature
        val columnName = namingConvention.stringPerConvention(name)
        getParamValue(columnName, paramType)
      }

    }

    private def getParamValue[T: TypeTag](columnName: String, expectedType: Type): Any = {
      //TODO TypeTag is not used, consider removing it https://github.com/AbsaOSS/balta/issues/64
      val value = row(columnName)
      if (isOptionType(expectedType)) {
        value
      } else {
        value.getOrThrow(new NullPointerException(s"Column '$columnName' is null"))
      }
    }
  }
}
