package info.glennengstrand.io

import scredis._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}

/**
 * Created by glenn on 2/17/15.
 */
object RedisService {
  def connect: Redis = {
    new Redis("/home/glenn/Documents/workspaceScalaNewsFeed/feed2/src/main/resources/redis.conf", "scredis")
  }
  lazy val redis = connect
  val timeout: Long = 60000
}

trait RedisCacheAware extends CacheAware {
  def key(o: PersistentDataStoreBindings, criteria: Map[String, Any]): String = {
    o.entity + ":" + criteria.map((f) => f._2).reduce(_ + ":" + _)
  }

  def load(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Map[String, Any] = {
    val fr: Future[Option[String]] = RedisService.redis.get[String](key(o, criteria))
    val frc: Try[Option[String]] = Await.ready(fr, Duration.Inf).value.get
    val retVal: scala.collection.mutable.Map[String, Any] = scala.collection.mutable.Map()
    frc match {
      case Success(Some(value)) => {
        IO.fromJson(value.asInstanceOf[String]).foreach(kv => retVal.put(kv._1, kv._2))
      }
      case Success(None) => Map()
      case Failure(e) => Map()
    }
    retVal.toMap[String, Any]
  }

  def store(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Unit = {
    RedisService.redis.set(key(o, criteria), IO.toJson(state))
  }

  def invalidate(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Unit = {
    RedisService.redis.del(key(o, criteria))
  }
}

class RedisCache extends RedisCacheAware {

}