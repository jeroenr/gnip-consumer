package com.github.gnip.consumer.processing

import akka.actor.ActorRef
import akka.http.scaladsl.coding.Gzip
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.gnip.consumer.util.JsonUtil
import com.typesafe.config.Config
import com.github.jeroenr.bson.Bulk
import com.github.jeroenr.tepkin.MongoClient

import scala.concurrent.duration._
import scala.util.{ Success, Try }

/**
 * Created by jero on 16-7-15.
 */

class ByteStringStreamProcessorBuilder(config: Config)(implicit val materializer: akka.stream.Materializer) {

  private val mongoClient = MongoClient(s"mongodb://${config.getString("mongo.host")}")
  private val mongoCollection = mongoClient("mydb")("mycollection")

  val src = Source.actorRef[ByteString](
    1000,
    OverflowStrategy.dropHead // drop when we exceed buffer, cannot apply backpressure here
  )

  val runnableByteStringProcessingFlow =
    RunnableGraph.fromGraph {
      GraphDSL.create(src) { implicit builder =>
        byteStringSource =>
          import GraphDSL.Implicits._
          // TODO: 1. filter out tweets from the stream (we are not interested in keep alive / system messages etc)
          // TODO: 2. group them in bulks
          // TODO: 3. index bulk of tweets to Mongo
          ClosedShape
      }
    }

  def build: ActorRef = runnableByteStringProcessingFlow.run() // this materializes the flow
}