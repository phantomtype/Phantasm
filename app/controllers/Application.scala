package controllers

import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecuredRequest, SecureSocial, Identity}
import service.RoomService
import models._
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import scala.Some

object Application extends Controller with securesocial.core.SecureSocial {

  implicit def identity(implicit request: RequestHeader):Option[Identity] = {
    SecureSocial.currentUser
  }

  implicit def user(implicit  request: RequestHeader): Option[User] = {
    identity match {
      case Some(i) => Users.findByUserId(i.identityId)
      case None => None
    }
  }

  def index = UserAwareAction { implicit rs =>
    user match {
      case Some(u) =>
        val id = RoomService.createPrivateRoomUnlessExist(u)
        Redirect(routes.Application.room(id))
      case None =>
        Ok(views.html.index())
    }
  }
  def room(id: Long) = SecuredAction { implicit rs =>
    val messages = Room.recent_messages(id).map { t =>
      val user = t._2
      val comment = t._1
      Message(user.avatarUrl.getOrElse(""), user.fullName, comment.message)
    }
    Ok(views.html.room(id, messages))
  }

  def chat(roomId: Long, userId: Long) = WebSocket.async[JsValue] { request  =>
    ChatRoom.join(roomId, userId)
  }

  def chatRoomJs(roomId: Long, userId: Long) = Action { implicit request =>
    Ok(views.js.chatRoom(roomId, userId))
  }
}