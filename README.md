# Stratio KHermes.
[![Coverage Status](https://coveralls.io/repos/github/Stratio/Hermes/badge.svg?branch=master)](https://coveralls.io/github/Stratio/Hermes?branch=master)

## Overview.
When you have a complex system architecture with a huge variety of services, the first question that you always make to yourself is:  "What happen when I start to generate tons of events and what is the behaviour of the system when it starts to process them?". For this reasons we are devoloping a high configurable and scalable tool called KHermes that could answer this "a priori simple" question.

> "KHermes is a distributed fake data generator used in distributed environments that produces a high volume of events in a scalable way".

It has the next features:
  - Configurable templates through Play Twirl. Make your own template and send it to one or more nodes.
  - Random event generation though KHermes' helper: based in Faker, you could generate generic names, dates, numbers, etc.
  - Scalable generation through an Akka Cluster. Run up all nodes that you need to generate data.
  - A simple but powerful shell to take the control of your cluster: you can start, stop node generation in seconds.

## Architecture.
The main idea behind KHermes is to run nodes that produces messages. This nodes should be increased or decreased depending of the needs of the user. For this reason we thought that could be a good idea use an Akka cluster. An architecture could be summarized in these points:
- Each node of Akka's cluster could receive messages to perform operations such as start, stop, etc. data generation. To start a node it needs three basic things:
    * A KHermes' configuration. This configuration will set, for example, where the templates will compile, i18n of the data, etc.
    * A Kafka's configuration. This configuration will set Kafka's parameters. You can see the official Kafka's documentation to get more specific information.
    * A Twirl's template. A template that will define how to generat a CSV, JSON or every structure that you need.
    * All configurations could be reused thanks to we persist all of them in Zookeeper. For this reason it is mandatory to have a running instance of zookeeper in our system.
    <TODO Figure of the architecture here>

## Installation and Execution.
Right now the only way  to execute KHermes is to generate a jar file. To make it, you should execute:
```sh
$ mvn clean package
```
This command will generate a fat jar with all dependencies in target/hermes-<version>.jar. To run it, you should execute:
```sh
$ java -jar target/hermes-<version>.jar [-Dparameters.to.overwrite]
```

## Getting started.
The first thing that you should do is to specify a configuration. KHermes' configuration is done thanks to Typesafe config.
You can see all options that you can configure in the next section:
```javascript
hermes {
  templates-path = "/opt/hermes/templates"
  client = false
}
akka {
  loglevel = "error"
  actor {
    provider = "akka.cluster.ClusterActorRefProvider"
    debug {
      receive = on
      lifecycle = on
    }
  }
  remote {
    log-remote-lifecycle-events = off
    netty.tcp {
      hostname = localhost
      port = 2553
    }
  }
  cluster {
    roles = [backend]
    seed-nodes = [${?VALUE}]
    auto-down-unreachable-after = 10s
  }
}
```
As you can see, you could set configurations for Hermes or Akka's cluster. We can not see how an Akka's cluster is configured because there are a lot of information in its official documentation.
For KHermes, you can set the next parameters:
- templates-path: when you send a template to one node, it sends a Twirl's template. The template is translated to a Scala's native code that should be compiled when it runs the first time. For this reason you need to set and temporal path where all .scala and .class files are.
- client: if you need to start a node and also you need a shell you could put the value of this parameter to true. When you run you could see a KHermes' shell, something like:
```
╦ ╦┌─┐┬─┐┌┬┐┌─┐┌─┐
╠═╣├┤ ├┬┘│││├┤ └─┐
╩ ╩└─┘┴└─┴ ┴└─┘└─┘ Powered by Stratio (www.stratio.com)

> System Name   : hermes
> Start time    : Fri Mar 10 12:31:52 CET 2017
> Number of CPUs: 8
> Total memory  : 251658240
> Free memory   : 225155304
    
hermes>
```
If you execut help in your command line you can see the list of available commands in our shell:
```
hermes> help
Hermes commands:
  set hermes             Sets your Hermes configuration
  set kafka              Sets your Kafka configuration
  set template           Sets your template
  set avro               Sets your Avro configuration
  show config            Show all set configurations
  ls                     Lists the nodes with their current status
  start <node-id>        Starts event generation in node with id <node-id>
  stop <node-id>         Stops event generation in node with id <node-id>
  clear                  Cleans the screen.
  help                   Shows this help.
  exit                   Exit of Hermes Cli.
```
Steps to run a policy:
* Step 1) Save a KHermes configuration that will be persisted in Zookeeper. This is needed because if not is saved, the next time that the user executes KHermes it will lost this configuration:
  ```
  hermes> set hermes
  Press Control + D to finish
  hermes {
     templates-path = "/tmp/hermes/templates"
     topic = "test"
     template-name = "testTemplate"
     i18n = "ES"
     timeout-rules {
       number-of-events: 1000
       duration: 2 seconds
     }
     stop-rules {
       number-of-events: 5000
     }
  }
  ```
  As you can see you should to configure the next variables:
    - templates-path: in every node that you send this configuration, it will need to generate and compile a template.
    - topic: it indicates a Kafka's topic where messages will be produced.
    - template-name: it indicates a prefix for the generated .scala and .class files. It is possible that in the future this variable dissapears.
    - i18n: internationalization of KHermes' helper. It generates, for example names in Spanish. Righ now only there are available ES and EN.
    - timeout-rules: it is optional. When it is set it generates 1000 events and wait 2 seconds to generate the next 1000 events.
    - stop-rules: it is optional. When it is set it generates 5000 events and the node stops to generate. Also the node will be free to generate.
    
* Step 2) Save a Kafka's configuration that also will be persisted in Zookeeper.
  ```
  hermes> set kafka
  Press Control + D to finish
  kafka {
     bootstrap.servers = "localhost:9092"
     acks = "-1"
     key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
     value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
  }
  ```
  
* Step 3) Save a Twirl template that also will be persisted in Zookeeper.
  ```
  hermes> set template
  Press Control + D to finish
  @import com.stratio.hermes.utils.Hermes
  @(hermes: Hermes)
  {
    "name" : "@(Hermes.Name.firstName)"
  }
  ```
* Step four) Once you have saved these configuration in ZK, you could start a generation in the nodes that you need:
  ```
  hermes> ls
  Node Id                                Status
  ------------------------------------   ------
  845441ec-cb0d-4363-b494-a39d56a82727 | false
  hermes> start 845441ec-cb0d-4363-b494-a39d56a82727
  hermes> ls
  Node Id                                Status
  ------------------------------------   ------
  845441ec-cb0d-4363-b494-a39d56a82727 | true
  ```
  At this moment the node with id 845441ec-cb0d-4363-b494-a39d56a82727 is producing messages to Kafka following the saved template. You can check it using Kafka's console consumer.

## Random Helper.
Supporting us in [Faker](https://github.com/stympy/faker) we are developing a random generator. At this moment we have the next features:
* Name generation:
  fullname() → Paul Brown
  middleName() → George Michael
  firstName() → Steven
  lastName() → Robinson

* Number generation:
  number(2) → 23
  number(2,Positive) → 23
  decimal(2) → 23.45
  decimal(2,Negative) → -45.89
  decimal(2,4) → 45.7568
  decimal(3,2,Positive) → 354.89
  numberInRange(1,9) → 2
  decimalInRange(1,9) → 2.6034840849740117

* Geolocation generation:
  geolocation() → (40.493556, -3.566764, Madrid)
  geolocationWithoutCity() → (28.452717, -13.863761)
  city() → Tenerife
  country() → ES

* Timestamp generation:
  dateTime("1970-1-12" ,"2017-1-1") → 2005-03-01T20:34:30.000+01:00
  time() → 15:30:00.000+01:00
* Music generation:
  playedSong() → ("Despacito (Featuring Daddy Yankee)", "Luis Fonsi, Daddy Yankee", "Despacito (Featuring Daddy Yankee)","Latin")

## Docker.
* Seed + Node
  ```sh
  docker run -dit --name SEED_NAME -e PARAMS="-Dhermes.client=true -Dakka.remote.hostname=SEED_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2552 -Dakka.remote.netty.tcp.hostname=SEED_NAME.DOMAIN -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/hermes:VERSION
  ```
* Node
  ```sh
  docker run -dit --name AGENT_NAME -e PARAMS="-Dhermes.client=false -Dakka.remote.hostname=AGENT_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/hermes:VERSION
  ```
  
## FAQ.
* **Is it neeeded Zookeeper to run KHermes?.** Yes, at this moment it is mandatory to have a instance of Zookeper in order to run KHermes.
* **Is it needed Apache Kafka to run KHermes?.** Yes, at the end all generated event will be persisted in Kafka and right now there are not any other possibility.
* **Is there any limitation of throughput?.** No, KHermes is designed to scale out of the box adding infinite nodes in our Akka's cluster.

## Roadmap.
* Awesome UI.
* No Zookeeper dependency using Akka Distributed Data.

## Licenses.
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Tech.
KHermes uses a number of open source projects to work properly:
* [Twirl](https://github.com/playframework/twirl) - Twirl is the Play template engine.
* [Akka](http://akka.io) - Akka is a toolkit and runtime for building highly concurrent, distributed, and resilient message-driven applications on the JVM.
* [Apache Kafka]() - Kafka™ is used for building real-time data pipelines and streaming apps.

And of course itself is open source with a  on GitHub [KHermes](https://github.com/stratio/hermes)

## Development.
Want to contribute? Great!
**KHermes is open source and we need you to keep growing.**

## Contributors.
* [Alberto Rodriguez](https://github.com/albertostratio)
* [Alicia Doblas](https://github.com/adoblas)
* [Alvaro Nistal](https://github.com/anistal)
* [Emilio Ambrosio](https://github.com/eambrosio)
* [Enrique Ruiz](https://github.com/eruizgar)
* [Juan Pedro Gilaberte](https://github.com/jpgilaberte)
