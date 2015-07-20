package com.github.gnip.consumer.client

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.http._
import akka.http.scaladsl._
import akka.http.scaladsl.client.TransformerPipelineSupport._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers._
import akka.pattern.pipe
import akka.stream._
import akka.stream.scaladsl.{ Sink, Source }

/**
 * Created by jero on 7-5-15.
 */

class GnipStreamHttpClient(host: String, port: Int, account: String, processor: ActorRef)(implicit val materializer: Materializer) extends Actor with ActorLogging {
  this: Authorization =>

  private val system = context.system
  private implicit val executionContext = system.dispatcher

  val client = Http(system).outgoingConnectionTls(host, port, settings = ClientConnectionSettings(system))

  override def receive: Receive = {
    case response: HttpResponse if response.status.intValue / 100 == 2 =>
      log.info(s"Got successful response $response")
      response.entity.dataBytes.map(processor ! _).runWith(Sink.ignore)
    case response: HttpResponse =>
      log.info(s"Got unsuccessful response $response")
      system.shutdown()
    case _ =>
      val req = HttpRequest(GET, Uri(s"/accounts/$account/publishers/twitter/streams/track/prod.json"))
        .withHeaders(`Accept-Encoding`(gzip), Connection("Keep-Alive")) ~> authorize
      Source.single(req)
        .via(client)
        .runWith(Sink.head)
        .pipeTo(self)
  }

}

