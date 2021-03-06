/*
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.khermes.utils.generators

import com.stratio.khermes.constants.KhermesConstants
import com.stratio.khermes.exceptions.KhermesException
import com.stratio.khermes.helpers.ParserHelper._
import com.stratio.khermes.helpers.{RandomHelper, ResourcesHelper}
import com.stratio.khermes.implicits.KhermesSerializer
import com.stratio.khermes.utils.KhermesUnit
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

case class EmailGenerator(locale: String) extends KhermesUnit
  with KhermesSerializer
  with LazyLogging {

  override def unitName: String = "email"

  lazy val emailModel = locale match {
    case KhermesConstants.DefaultLocale =>
      val resources = ResourcesHelper.getResources(unitName)
        .map(parse[Seq[String]](unitName, _))
      val maybeErrors = parseErrors[Seq[String]](resources)
      if (maybeErrors.nonEmpty) logger.warn(s"$maybeErrors")
      resources
    case localeValue =>
      val resource = Seq(parse[Seq[String]](unitName, s"$localeValue.json"))
      val maybeErrors = parseErrors[Seq[String]](resource)
      if (maybeErrors.nonEmpty) logger.warn(s"$maybeErrors")
      resource
  }

  def domains(emailModel: Seq[Either[String, Seq[String]]]): Seq[String] =
    emailModel
      .filter(_.isRight)
      .flatMap(_.right.get)

  /**
   * Returns an email address using a fullname and a random domain
   * @param fullname Name and surname
   * @return A valid email address, as string, concatenating the first letter from the name and the whole surname,
   *         and finally a random domain
   */
  def address(fullname: String): String = {
    val domain = RandomHelper.randomElementFromAList[String](domains(emailModel)).getOrElse(
      throw new KhermesException(s"Error loading locate /locales/$unitName/$locale.json"))
    s"${getInitial(fullname)}${getSurname(fullname)}@$domain"
  }

  private def getInitial(fullname: String) = {
    Try(getName(fullname).charAt(0)).getOrElse(throw new KhermesException(s"Error parsing a no valid name"))
  }

  def getName(fullName: String): String =
    Try(fullName.trim.split(" ")(0)).getOrElse(
      throw new KhermesException(s"Error extracting the name value")).toLowerCase

  def getSurname(fullName: String): String =
    Try(fullName.trim.split(" ")(1)).getOrElse(
      throw new KhermesException(s"Error extracting the surname value")).toLowerCase
}

