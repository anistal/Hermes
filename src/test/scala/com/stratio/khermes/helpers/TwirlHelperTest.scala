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

package com.stratio.khermes.helpers

import com.stratio.khermes.helpers.TwirlHelper.CompilationError
import com.stratio.khermes.utils.Khermes
import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import play.twirl.api.Txt

@RunWith(classOf[JUnitRunner])
class TwirlHelperTest extends FlatSpec
  with Matchers
  with BeforeAndAfter
  with LazyLogging {

  implicit val config = com.stratio.khermes.implicits.KhermesImplicits.config

  before {
    KhermesRunnerHelper.createPaths
  }

  "A TwirlHelper" should "compile a simple template without object injection" in {
    val template = "Hello world"
    val result = cleanContent(TwirlHelper.template[() => Txt](template, "templateTest").static().toString())
    result should be("Hello world")
  }

  it should "compile a template and inject a string" in {
    val template =
      """
        |@(name: String)
        |Hello @name
      """.stripMargin
    val result = cleanContent(TwirlHelper.template[(String) => Txt](template, "templateTest").static("Neo").toString())
    result should be("Hello Neo")
  }

  it should "compile a template and inject an khermes helper" in {
    val template =
      """
        |@(khermes: Khermes)
        |Hello @(khermes.Name.firstName)
      """.stripMargin

    val khermes = new Khermes("EN")

    val result = cleanContent(
      TwirlHelper.template[(Khermes) => Txt](template, "templateTest").static(khermes).toString())
    result should fullyMatch regex """Hello [a-zA-Z]+"""
  }

  it should "throw an error when the template is wrong" in {
    val template = "Hello @(error)"
    //scalastyle:off
    the[CompilationError] thrownBy (TwirlHelper.template[() => Txt]
      (template, "templateTest").static().toString()) should have('line (1), 'column (8))
    //scalastyle:on
  }

  /**
   * Cleans the content deleting return carriages an not necessary spaces.
   * @param content with the original content.
   * @return a sanitized content.
   */
  def cleanContent(content: String): String = content.replace("\n", "").replaceAll("  +", "")
}
