package controllers

import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecuredRequest, SecureSocial, Identity}
import service.RoomService
import models._
import play.api.mvc.WebSocket
import scala.Some
import play.api.libs.json.{JsValue, Writes, Json}
import models.JsonWrites._
import play.api.data.Form
import play.api.data.Forms._
import org.joda.time.DateTime

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
    Ok(views.html.rooms())
  }

  def room(id: Long) = SecuredAction { implicit rs =>
    RoomService.findRoom(id) match {
      case Some(room) =>
        Ok(views.html.room(room))
      case None =>
        NotFound
    }
  }

  def createRoom() = SecuredAction { implicit rs =>
    Form(tuple("name" -> nonEmptyText, "isPrivate" -> boolean)).bindFromRequest.fold(
      errors => BadRequest,
      (form) => {
        val room = Room(None, user.get.uid.get, form._1, form._2, DateTime.now)
        RoomService.createRoom(room)
        NoContent
      }
    )
  }

  def chat(roomId: Long, userId: Long) = WebSocket.async[JsValue] { request  =>
    val user = Tables.Users.findById(userId)
    ChatRoom.join(roomId, user.get)
  }

  def pathToRoom(roomId: Long, userId: Long) = SecuredAction { implicit rs =>
    Ok(Json.toJson(Json.obj("path" -> routes.Application.chat(roomId, userId).webSocketURL())))
  }

  def recentlyMessage(roomId: Long) = SecuredAction { implicit request =>
    val messages = RoomService.recent_comments(roomId, 20).map { t =>
      TalkMessage("talk", t._2, t._1)
    }
    Ok(Json.toJson(messages))
  }

  def rooms = SecuredAction { implicit rs =>
    Ok(Json.toJson(RoomService.all))
  }

}