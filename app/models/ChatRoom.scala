package models

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel
import scala.collection.mutable
import org.joda.time.DateTime
import service.RoomService
import models.JsonWrites._

object ChatRoom {

  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[ChatRoom])
    roomActor
  }

  def join(roomId: Long, userId:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(roomId, userId)).map {

      case Connected(enumerator) =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! Talk(roomId, userId, (event \ "text").as[String])
        }.map { _ =>
          default ! Quit(roomId, userId)
        }
        (iteratee,enumerator)

      case CannotConnect(error) =>
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
    }
  }
}

case class RobotComment(message: String) {
  def apply(message: String): Comment =
    Comment(None, 0, 0, message, DateTime.now)
}

class ChatRoom extends Actor {
  implicit def StringToComment(message: String): Comment = Comment(None, 0, 0, message, DateTime.now)

  var member_ids = Set.empty[Long]
  val enumerators = new mutable.HashMap[Long, Enumerator[JsValue]]
  val channels = new mutable.HashMap[Long, Channel[JsValue]]

  def receive = {

    case Join(roomId, userId) => {
        member_ids = member_ids + userId
        val enumerator = enumerators.get(roomId) match {
          case Some(e) => e
          case None => {
            val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
            enumerators.put(roomId, chatEnumerator)
            channels.put(roomId, chatChannel)
            chatEnumerator
          }
        }
        sender ! Connected(enumerator)
        self ! NotifyJoin(roomId, userId)
    }

    case NotifyJoin(roomId, userId) => {
      notifyAll("join", roomId, userId, "has entered the room")
    }

    case Talk(roomId, userId, text) => {
      val comment = Comment(None, userId, roomId, text, DateTime.now)
      RoomService.createComment(comment)
      notifyAll("talk", roomId, userId, comment)
    }

    case Quit(roomId, userId) => {
      member_ids = member_ids - userId
      notifyAll("quit", roomId, userId, "has left the room")
    }

  }

  def notifyAll(kind: String, roomId: Long, userId: Long, comment: Comment) {
    val members = member_ids.toList.map(id => Tables.Users.findById(id).get)
    val user = members.find(_.uid.exists(_ == userId))

    val msg = JsObject(
      Seq(
        "kind"    -> JsString(kind),
        "roomId"  -> JsNumber(roomId),
        "user"    -> Json.toJson(user),
        "comment" -> Json.toJson(comment),
        "members" -> JsArray(members.map(Json.toJson(_)))
      )
    )
    channels.get(roomId) match {
      case Some(channel) => channel.push(msg)
      case None => Unit
    }
  }

}

case class Join(roomId: Long, userId: Long)
case class Quit(roomId: Long, userId: Long)
case class Talk(roomId: Long, userId: Long, text: String)
case class NotifyJoin(roomId: Long, userId: Long)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
