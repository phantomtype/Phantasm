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

  val rooms = new mutable.HashMap[Long, ActorRef]

  def room(roomId: Long): ActorRef = {
    if (rooms.contains(roomId)) {
      rooms.get(roomId).get
    } else {
      val room = Akka.system.actorOf(Props(new ChatRoom(roomId)))
      rooms.put(roomId, room)
      room
    }
  }

  def join(roomId: Long, user: User):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (room(roomId) ? Join(user)).map {

      case Connected(enumerator) =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          room(roomId) ! Talk(user, (event \ "text").as[String])
        }.map { _ =>
          room(roomId) ! Quit(user)
        }
        (iteratee,enumerator)

      case CannotConnect(error) =>
        val iteratee = Done[JsValue,Unit]((),Input.EOF)
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))
        (iteratee,enumerator)
    }
  }
}

class ChatRoom(roomId: Long) extends Actor {
  var members = Set.empty[User]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
  val room = RoomService.findRoom(roomId)

  def receive = {

    case Join(user) =>
      if (members.contains(user)) {
        self ! CannotConnect("user already")
      } else {
        members = members + user
        sender ! Connected(chatEnumerator)
        self ! NotifyJoin(user)
        sender ! Talk(user, "hogehoge")
      }

    case NotifyJoin(user) =>
      val msg = UpdateMembers("join", user, members)
      notifyAll(Json.toJson(msg))

    case Talk(user, text) =>
      val comment = room.createComment(user, text)
      val msg = Message("talk", user, comment)
      notifyAll(Json.toJson(msg))

    case Quit(user) =>
      members = members - user
      val msg = UpdateMembers("quit", user, members)
      notifyAll(Json.toJson(msg))
  }

  def notifyAll(msg: JsValue) {
      chatChannel.push(msg)
  }
}

case class Join(user: User)
case class Quit(user: User)
case class Talk(user: User, text: String)
case class NotifyJoin(user: User)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
