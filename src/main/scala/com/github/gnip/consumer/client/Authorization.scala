package com.github.gnip.consumer.client

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{ BasicHttpCredentials, Authorization => AuthHeader}

/**
 * Created by jero on 7-5-15.
 */

trait Authorization {
  def authorize: HttpRequest => HttpRequest
}

trait BasicAuthorization extends Authorization {
  override def authorize: (HttpRequest) => HttpRequest = {
    { httpRequest: HttpRequest =>
      val authorization = AuthHeader(BasicHttpCredentials("user", "password"))
      httpRequest.addHeader(authorization)
    }
  }
}

