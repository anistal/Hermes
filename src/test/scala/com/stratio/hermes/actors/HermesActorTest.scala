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

package com.stratio.hermes.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.stratio.hermes.constants.HermesConstants
import com.stratio.hermes.implicits.HermesImplicits
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Generic class used to test Hermes actors. All tests should be extend this class.
 */
abstract class HermesActorTest extends TestKit(ActorSystem("ActorTest",
  ConfigFactory.parseString(HermesActorTest.DefaultConfig)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {


//  lazy implicit val config: Config = ConfigFactory.parseString(HermesActorTest.DefaultConfig)
  lazy implicit val config: Config = HermesImplicits.config
  lazy implicit val executionContext = HermesImplicits.executionContext

}

/**
 * Companion object used to set constants related with configurations to test actors.
 */
object HermesActorTest {

  val DefaultConfig = """
                        |akka {
                        |  cluster.auto-join = off
                        |  actor.provider = akka.cluster.ClusterActorRefProvider
                        |  akka.testconductor.barrier-timeout = 4000
                        |
                        |  loglevel = "INFO"
                        |  remote {
                        |    log-remote-lifecycle-events = off
                        |    netty.tcp {
                        |      hostname = "localhost"
                        |      port = 0
                        |    }
                        |  }
                        |}
                        |
                        |kafka {
                        |  metadata.broker.list = "localhost:9092"
                        |  bootstrap.servers = "localhost:9092"
                        |}
                      """.stripMargin
}
