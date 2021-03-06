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

import com.stratio.khermes.constants.KhermesConstants
import com.stratio.khermes.helpers.KhermesConfig._
import com.typesafe.config.ConfigFactory
import java.time.Duration


import scala.util.Try

/**
 * Class used to load and parse configuration that will be used by the supervisor. Remember that all information
 * encapsulated should be serializable because will be part as an akka's message.
 * @param khermesConfigContent with configuration about khermes generator.
 * @param kafkaConfigContent with kafka's configuration.
 * @param template to generate.
 * @param avroSchema in the case that you are using avro serialization.
 */
case class KhermesConfig(khermesConfigContent: String,
                         kafkaConfigContent: String,
                         template: String,
                         avroSchema: Option[String] = None) {

  val khermesConfig = ConfigFactory.parseString(khermesConfigContent)
  val kafkaConfig = ConfigFactory.parseString(kafkaConfigContent)

  assertCorrectConfig()

  /**
   * Tries to parse the configuration and checks that the KhermesConfig object has all required fields.
   */
  protected[this] def assertCorrectConfig(): Unit = {
    def buildErrors(mandatoryFields: Seq[String]): Seq[String] =
      for {
        mandatoryField <- mandatoryFields
        if Try(khermesConfig.getAnyRef(mandatoryField)).isFailure && Try(kafkaConfig.getAnyRef(mandatoryField)).isFailure
      } yield(s"$mandatoryField not found in the config.")

    val errors = buildErrors(MandatoryFields) ++ (if(configType == ConfigType.Avro) buildErrors(AvroMandatoryFields) else Seq.empty)
    assert(errors.isEmpty, errors.mkString("\n"))
  }

  def configType(): ConfigType.Value =
    if(kafkaConfig.getString("kafka.key.serializer") == KhermesConstants.KafkaAvroSerializer) {
      ConfigType.Avro
    } else {
      ConfigType.Json
    }

  def topic: String = khermesConfig.getString("khermes.topic")

  def templateName: String = khermesConfig.getString("khermes.template-name")

  def templateContent: String = template

  def khermesI18n: String = khermesConfig.getString("khermes.i18n")

  def timeoutNumberOfEventsOption: Option[Int] = Try(khermesConfig.getInt("khermes.timeout-rules.number-of-events")).toOption

  def timeoutNumberOfEventsDurationOption: Option[Duration] = Try(khermesConfig.getDuration("khermes.timeout-rules.duration")).toOption

  def stopNumberOfEventsOption: Option[Int] = Try(khermesConfig.getInt("khermes.stop-rules.number-of-events")).toOption

}

object KhermesConfig {

  val MandatoryFields = Seq(
    "khermes.topic",
    "khermes.template-name",
    "khermes.i18n",
    "kafka.key.serializer"
  )

  val AvroMandatoryFields = Seq(
    "kafka.schema.registry.url"
  )

  object ConfigType extends Enumeration {
    val Avro, Json = Value
  }
}
