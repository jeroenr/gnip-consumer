package com.github.gnip.consumer

import akka.actor.{Props, ActorSystem}
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}
import com.github.gnip.consumer.client.{BasicAuthorization, GnipStreamHttpClient}
import com.github.gnip.consumer.processing.ByteStringStreamProcessorBuilder

object Boot extends App {
  implicit val system = ActorSystem("Client")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  //////////////////////////////////////////////////////////////////////////////////////
  //   byteStrings ~> gunzip ~> toTweet ~> tweetBroadCaster.in                        //
  //                                       tweetBroadCaster.out(0) ~> toBulk ~> mongo //
  //                                       tweetBroadCaster.out(1) ~> printToStdOut   //
  //////////////////////////////////////////////////////////////////////////////////////

  val processorBuilder = new ByteStringStreamProcessorBuilder(system.settings.config) // reusable flow builder
  val processor = processorBuilder.build

  val gnipClientActor = system.actorOf(Props(new GnipStreamHttpClient("stream.gnip.com", 443, "MyGnipAccount", processor) with BasicAuthorization))

  gnipClientActor ! "go!"
}

