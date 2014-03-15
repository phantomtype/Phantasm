package controllers

import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecuredRequest, SecureSocial, Identity}
import service.RoomService
import models._
import play.api.mvc.WebSocket
import scala.Some
import play.api.libs.json.{JsValue, Writes, Json}
import models.JsonWrites._

object Application extends Controller with securesocial.core.SecureSocial {

  implicit def identity(implicit request: RequestHeader):Option[Identity] = {
    SecureSocial.currentUser
  }

  implicit def user(implicit  request: RequestHeader): Option[User] = {
    identity match {
      case Some(i) => Tables.Users.findByIdentityId(i.identityId)
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
    ChatRoom.join(roomId, userId)
  }

  def pathToRoom(roomId: Long, userId: Long) = SecuredAction { implicit rs =>
    Ok(Json.toJson(Json.obj("path" -> routes.Application.chat(roomId, userId).webSocketURL())))
  }

  def recentlyMessage(roomId: Long) = SecuredAction { implicit request =>
    val messages = RoomService.recent_comments(roomId).map { t =>
      Message(t._2, t._1)
    }
    Ok(Json.toJson(messages))
  }
}