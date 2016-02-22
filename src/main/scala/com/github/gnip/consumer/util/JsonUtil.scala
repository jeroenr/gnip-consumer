package com.github.gnip.consumer.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.element._

/**
 * Created by jero on 7-5-15.
 */
object JsonUtil {
  val mapper = new ObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def toJson(t: Any) = mapper.writeValueAsString(t)

  def fromJson(s: String) = mapper.readValue(s, classOf[Map[String, Any]])

  def toBson(map: Map[String, Any]): BsonDocument = {
    BsonDocument(map.map {
      case (k, null) => BsonNull(k)
      case (k, v: Int) => BsonInteger(k, v)
      case (k, v: Double) => BsonDouble(k, v)
      case (k, v: Boolean) => BsonBoolean(k, v)
      case (k, v: Long) => BsonLong(k, v)
      case (k, v: String) => BsonString(k, v)
      case (k, v: List[_]) => BsonArray(k, toBsonValueArray(v))
      case (k, v: Map[String @unchecked, _]) => BsonObject(k, BsonValueObject(toBson(v)))
    })
  }

  private def toBsonValueArray(list: List[_]): BsonValueArray = {
    BsonValueArray(BsonDocument(list.zipWithIndex.map {
      case (value: List[_], index) => s"$index" := toBsonValueArray(value)
      case (value: Map[String @unchecked, _], index) => s"$index" := toBson(value)
      case (value, index) => s"$index" := value
    }: _*))
  }

}
