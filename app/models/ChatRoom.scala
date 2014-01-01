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

object ChatRoom {

  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[ChatRoom])
    roomActor
  }

  def join(userId:Long):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])] = {

    (default ? Join(userId)).map {

      case Connected(enumerator) =>
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! Talk(userId, (event \ "text").as[String])
        }.map { _ =>
          default ! Quit(userId)
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

  var members = Set.empty[Long]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {

    case Join(userId) => {
//      if(members.contains(userId)) {
//        sender ! CannotConnect("This userId is already used")
//      } else {
        members = members + userId
        sender ! Connected(chatEnumerator)
        self ! NotifyJoin(userId)
//      }
    }

    case NotifyJoin(userId) => {
      notifyAll("join", userId, "has entered the room")
    }

    case Talk(userId, text) => {
      notifyAll("talk", userId, text)
    }

    case Quit(userId) => {
      members = members - userId
      notifyAll("quit", userId, "has left the room")
    }

  }

  def notifyAll(kind: String, userId: Long, text: String) {
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "userId" -> JsNumber(userId),
        "message" -> JsString(text),
        "members" -> JsArray(
          members.toList.map(JsNumber(_))
        )
      )
    )
    chatChannel.push(msg)
  }

}

case class Join(userId: Long)
case class Quit(userId: Long)
case class Talk(userId: Long, text: String)
case class NotifyJoin(userId: Long)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
