package info.glennengstrand.news.service

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

import info.glennengstrand.news.model._
import info.glennengstrand.news.dao._
import info.glennengstrand.news.resource._

class OutboundService @Inject()(
    routerProvider: Provider[ParticipantRouter],
    outboundDao: OutboundDao)(implicit ec: ExecutionContext) {

  def create(postInput: Outbound)(
      implicit mc: MarkerContext): Future[Outbound] = {
    outboundDao.create(postInput)
  }

  def lookup(id: Int)(
      implicit mc: MarkerContext): Future[Seq[Outbound]] = {
    outboundDao.get(id)
  }
  
  def search(keywords: String) (
      implicit mc: MarkerContext): Future[Seq[String]] = {
    outboundDao.search(keywords)
  }

}
