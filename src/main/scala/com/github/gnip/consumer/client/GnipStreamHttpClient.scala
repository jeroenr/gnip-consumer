package com.github.gnip.consumer.client

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.http.scaladsl._
import akka.http.scaladsl.client.TransformerPipelineSupport._
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.settings.ClientConnectionSettings
import akka.pattern.pipe
import akka.stream._
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString

/**
 * Created by jero on 7-5-15.
 */

class GnipStreamHttpClient(host: String, port: Int, account: String, processor: ActorRef)(implicit val materializer: Materializer) extends Actor with ActorLogging {
  this: Authorization =>

  private val system = context.system
  private implicit val executionContext = system.dispatcher

  val client = Http(system).outgoingConnectionHttps(host, port, settings = ClientConnectionSettings(system))

  override def receive: Receive = {
    case response: HttpResponse if response.status.intValue / 100 == 2 =>
      log.info(s"Got successful response $response")
      response.entity.dataBytes.map(processor ! _).runWith(Sink.ignore)
    case response: HttpResponse =>
      log.info(s"Got unsuccessful response $response")
      system.terminate()
    case _ =>
      val req = HttpRequest(GET, Uri(s"/accounts/$account/publishers/twitter/streams/track/prod.json"))
        .withHeaders(`Accept-Encoding`(gzip), Connection("Keep-Alive")) ~> authorize
      Source.single(req)
        .via(client)
        .runWith(Sink.head)
        .pipeTo(self)
  }

}

class StubGnipClient(processor: ActorRef)(implicit val materializer: Materializer) extends Actor with ActorLogging {
  val tweet = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/sample_tweet.json")).mkString

  override def receive: Receive = {
    case _ =>
      Source
        .repeat(tweet)
        .map(ByteString.apply)
        .via(Gzip.encoderFlow)
        .map(processor ! _)
        .runWith(Sink.ignore)
  }
}