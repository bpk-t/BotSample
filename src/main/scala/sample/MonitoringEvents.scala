package sample

import akka.actor.Actor
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import twitter4j.TwitterFactory
import scala.util.Try

@JsonIgnoreProperties(ignoreUnknown=true)
case class EventResponse(result: List[Result], count: Int)

@JsonIgnoreProperties(ignoreUnknown=true)
case class Result(id: String, name: String)

class CheckEventActor extends Actor {
  override def receive: Receive = {
    case _ => {

      for {
        events <- getEvents
        // TODO すでにTweetしたやつをフィルタリングする仕組み
      } yield {
        events.result.foreach(event =>
          tweet(s"イベント予告が登録されました！ | ${event.name} ${eventDetailUrl(event.id)} #transiru"))
      }
    }
  }

  private def getEvents: Try[EventResponse] = Try {
    val response = io.Source.fromURL("https://www.transiru.net/api/events?q={\"state\":\"notstart\",\"limit\":10,\"order\":\"startDate.asc\"}")
    val json = response.getLines().mkString
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.readValue(json, classOf[EventResponse])
  }

  private def eventDetailUrl(id: String) = s"https://www.transiru.net/event/detail/?id=${id}"

  private def tweet(message: String): Unit = {
    val twitter = TwitterFactory.getSingleton()
    twitter.updateStatus(message)
  }
}

