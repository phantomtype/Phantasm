package controllers

import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecuredRequest, SecureSocial, Identity}
import service.RoomService
import models.{ChatRoom, User, Users}
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue

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
    Ok(views.html.room(id))
  }

  def chat(roomId: Long, userId: Long) = WebSocket.async[JsValue] { request  =>
    ChatRoom.join(userId)
  }

  def chatRoomJs(roomId: Long, userId: Long) = Action { implicit request =>
    Ok(views.js.chatRoom(roomId, userId))
  }
}