package com.github.gnip.consumer

import akka.actor.{ Props, ActorSystem }
import akka.stream.{ ActorMaterializerSettings, ActorMaterializer }
import com.github.gnip.consumer.client.{StubGnipClient, BasicAuthorization, GnipStreamHttpClient}
import com.github.gnip.consumer.processing.ByteStringStreamProcessorBuilder

object Boot extends App {
  implicit val system = ActorSystem("Client")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  val processorBuilder = new ByteStringStreamProcessorBuilder(system.settings.config) // reusable flow builder
  val processor = processorBuilder.build

//  val gnipClientActor = system.actorOf(Props(new GnipStreamHttpClient("stream.gnip.com", 443, "MyGnipAccount", processor) with BasicAuthorization))

  val gnipClientActor = system.actorOf(Props(new StubGnipClient(processor)))
  gnipClientActor ! "go!"
}

