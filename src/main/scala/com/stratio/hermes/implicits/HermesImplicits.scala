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

package com.stratio.hermes.implicits

import java.net.NetworkInterface

import akka.actor.ActorSystem
import com.stratio.hermes.constants.HermesConstants
import com.typesafe.config.{ConfigResolveOptions, Config, ConfigFactory, ConfigValueFactory}
import scala.collection.JavaConversions._

/**
 * General implicits used in the application.
 */
object HermesImplicits {

  lazy implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val config: Config = ConfigFactory
    .load(getClass.getClassLoader,
      ConfigResolveOptions.defaults.setAllowUnresolved(true))
    .withValue("clustering.ip", ConfigValueFactory.fromAnyRef(getHostIP()))
    .resolve

  lazy implicit val system: ActorSystem = ActorSystem(HermesConstants.ConstantAkkaClusterName, config)

  /**
   * Gets the IP of the current host .
   * @return if the ip is running in Docker it will search in eth0 interface, if not it returns 127.0.0.1
   */
  def getHostIP(): String =
    NetworkInterface.getNetworkInterfaces()
      .find(_.getName.equals("eth0"))
      .flatMap(_.getInetAddresses
          .find(_.isSiteLocalAddress)
          .map(_.getHostAddress))
      .getOrElse("127.0.0.1")
}
