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

import java.util.concurrent.atomic.AtomicInteger
import java.util.{Date, Properties}

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import com.stratio.hermes.kafka.KafkaProducer
import com.stratio.hermes.utils.Hermes
import com.typesafe.config.Config
import com.stratio.hermes.actors.WorkerSupervisorActor._

/**
 * Actor that produces messages to kafka. It runs n threads depending of the number of processors of the node.
 * @param config with the cluster configuration.
 */
class WorkerSupervisorActor()(implicit config: Config)
  extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  lazy val propsProducer = new Properties
  propsProducer.put("metadata.broker.list", config.getString("kafka.metadata.broker.list"))
  propsProducer.put("bootstrap.servers", config.getString("kafka.bootstrap.servers"))
  propsProducer.put("key.serializer" , "org.apache.kafka.common.serialization.StringSerializer")
  propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  propsProducer.put("request.requieres.acks", "1")

  override def receive: Receive = {
    case Start =>
      val count = new AtomicInteger(0)
      val initialTime = new Date().getTime
      (1 to Runtime.getRuntime.availableProcessors).foreach(startThread(_, count, initialTime))
      sender ! StartOK
  }

  protected def startThread(threadIndex: Int, count: AtomicInteger, time: Long): Unit = {
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        val producer = KafkaProducer.getInstance(propsProducer)
        val hermes = Hermes()
        produce(producer, hermes, count, time, 1)
      }

      def produce(producer: org.apache.kafka.clients.producer.KafkaProducer[AnyRef, AnyRef],
                  hermes: Hermes,
                  count: AtomicInteger,
                  time: Long,
                  index: Int): Unit = {
        KafkaProducer.send(producer, "testTopic", s"""{"name": "${hermes.Name.fullName}"}""")
        produce(producer, hermes, count, time, index + 1)
      }
    })

    thread.setName(s"thread-$threadIndex")
    thread.start
  }
}

object WorkerSupervisorActor {
  case object Start
  case object StartOK
}