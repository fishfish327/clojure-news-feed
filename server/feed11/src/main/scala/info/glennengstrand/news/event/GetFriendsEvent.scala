package info.glennengstrand.news.event

import io.vertx.lang.scala.{ScalaVerticle, ScalaLogger}
import io.vertx.scala.ext.web.RoutingContext
import scala.concurrent.Future
import scala.util.{Success, Failure}
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import info.glennengstrand.news.model.Friend
import info.glennengstrand.news.resource.Topics
import info.glennengstrand.news.service.{CacheWrapper, FriendService}

class GetFriendsEvent extends ScalaVerticle with NewsFeedEvent {
  override def startFuture(): Future[_] = {
      vertx
        .eventBus()
        .consumer[String](Topics.GetFriend.name)
        .handler(msg => {
          id(msg).foreach(nfr => {
            FriendService.get(nfr.id, ep => {
              ep match {
                case Success(op) => {
                  end(nfr.rc, 200, "application/json", op.asJson.noSpaces)
                }
                case Failure(e) => {
                  LOGGER.error(e.getLocalizedMessage)
                  end(nfr.rc, 500, "text/plain", e.getLocalizedMessage)
                }
              }
            })              
          })
        })
        .completionFuture()
  } 
}