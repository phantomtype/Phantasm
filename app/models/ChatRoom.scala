package models

import akka.actor._
import scala.concurrent.duration._
import scala.language.postfixOps

import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import models.JsonWrites._

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
          room(roomId) ! Talk(user, (event \ "text").as[String], (event \ "replyTo").as[Option[Long]])
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
  val room = Tables.Rooms.findRoom(roomId).get

  def receive = {

    case Join(user) =>
      if (members.contains(user)) {
        self ! CannotConnect("user already")
      } else {
        members = members + user
        sender ! Connected(chatEnumerator)
        Thread.sleep(1000) // for traffic jam
        self ! NotifyJoin(user)
      }

    case NotifyJoin(user) =>
      val msg = UpdateMembers("join", user, members)
      notifyAll(msg)

    case Talk(user, text, replyTo) =>
      val comment = room.createComment(user, text, replyTo)
      val msg = TalkMessage("talk", user, comment)
      notifyAll(msg)

    case Quit(user) =>
      members = members - user
      val msg = UpdateMembers("quit", user, members)
      notifyAll(msg)
  }

  def notifyAll(msg: Message) {
      chatChannel.push(Json.toJson(msg))
  }
}

case class Join(user: User)
case class Quit(user: User)
case class Talk(user: User, text: String, replyTo: Option[Long])
case class NotifyJoin(user: User)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
