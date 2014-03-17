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
import securesocial.core.{AuthenticationMethod, IdentityId}

object ChatRoom {

  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[ChatRoom])
    roomActor
  }

  def join(roomId: Long, user: User):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(roomId, user)).map {

      case Connected(enumerator) =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! Talk(roomId, user, (event \ "text").as[String])
        }.map { _ =>
          default ! Quit(roomId, user)
        }
        (iteratee,enumerator)

      case CannotConnect(error) =>
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
    }
  }
}

class ChatRoom extends Actor {
  val members = new mutable.HashMap[Long, Set[User]]
  val enumerators = new mutable.HashMap[Long, Enumerator[JsValue]]
  val channels = new mutable.HashMap[Long, Channel[JsValue]]

  def receive = {

    case Join(roomId, user) =>
      val enumerator = enumerators.get(roomId) match {
        case Some(e) => e
        case None =>
          val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
          enumerators.put(roomId, chatEnumerator)
          channels.put(roomId, chatChannel)
          chatEnumerator
      }
      sender ! Connected(enumerator)
      self ! NotifyJoin(roomId, user)

    case NotifyJoin(roomId, user) =>
      members.put(roomId, members.get(roomId).getOrElse(Set.empty) + user)
      val msg = UpdateMembers("join", roomId, user, members.get(roomId).get)
      notifyAll(roomId, Json.toJson(msg))

    case Talk(roomId, user, text) =>
      val comment = Comment(None, user.uid.get, roomId, text, DateTime.now)
      RoomService.createComment(comment)
      val msg = Message("talk", roomId, user, comment)
      notifyAll(roomId, Json.toJson(msg))

    case Quit(roomId, user) =>
      members.put(roomId, members.get(roomId).get.filterNot(_.uid == user.uid))
      val msg = UpdateMembers("quit", roomId, user, members.get(roomId).get)
      notifyAll(roomId, Json.toJson(msg))
  }

  def notifyAll(roomId: Long, msg: JsValue) {
    channels.get(roomId) match {
      case Some(channel) => channel.push(msg)
      case None => Unit
    }
  }
}

case class Join(roomId: Long, user: User)
case class Quit(roomId: Long, user: User)
case class Talk(roomId: Long, user: User, text: String)
case class NotifyJoin(roomId: Long, user: User)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
