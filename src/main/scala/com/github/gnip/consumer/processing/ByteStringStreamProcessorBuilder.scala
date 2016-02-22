package com.github.gnip.consumer.processing

import akka.actor.ActorRef
import akka.http.scaladsl.coding.Gzip
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.gnip.consumer.util.JsonUtil
import com.typesafe.config.Config
import net.fehmicansaglam.bson.Bulk
import net.fehmicansaglam.tepkin.MongoClient

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

          // create broadcaster with as many outlets as we have processors
          val tweetBroadCaster = builder.add(Broadcast[Tweet](2))

          val byteStringToTweetFlow = Flow[ByteString]
            .map(byteString => byteString.utf8String) // decode bytestring to string
            .map(unzipped => Try(JsonUtil.fromJson(unzipped))) // try parse string to Map[String, Any]
            .collect { case Success(json) => json } // filter unsuccessfully parsed items
            .filter(_.contains("actor")) // filter tweets (drop system messages)

          val tweetToBulk = Flow[Tweet]
            .map(JsonUtil.toBson) // transform tweet to Bson
            .groupedWithin(100, 10 millis) // try to group up to 100 BsonDocuments in 10 milliseconds
            .map(_.toList)
            .map(Bulk) // create a Bulk insert operation

          val mongoDbSink = mongoCollection.sink(parallelism = 4)
          val tweetPrintSink = Sink.foreach[Tweet](println)

          // route bytestring source via bytestring-to-tweet transformer to broadcaster
          byteStringSource.out ~> Gzip.decoderFlow ~> byteStringToTweetFlow ~> tweetBroadCaster.in
          tweetBroadCaster.out(0) ~> tweetToBulk ~> mongoDbSink
          tweetBroadCaster.out(1) ~> tweetPrintSink
          ClosedShape
      }
    }

  def build: ActorRef = runnableByteStringProcessingFlow.run() // this materializes the flow
}