package controllers

import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecuredRequest, SecureSocial, Identity}
import models._
import play.api.mvc.WebSocket
import scala.Some
import play.api.libs.json.{JsNull, JsValue, Writes, Json}
import models.JsonWrites._
import play.api.data.Form
import play.api.data.Forms._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}

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

  def index = SecuredAction { implicit rs =>
    Ok(views.html.rooms())
  }

  def account = SecuredAction { implicit rs =>
    Ok(views.html.account())
  }

  def user_setting = SecuredAction { implicit rs =>
    Ok(Json.toJson(user.get.userSetting()))
  }

  def saveAccount = SecuredAction { implicit rs =>
    Form(("desktopNotifications" -> boolean)).bindFromRequest.fold(
      errors => BadRequest,
      (form) => {
        user.get.saveUserSetting(desktopNotifications = form)
        NoContent
      }
    )
  }

  def room(id: Long) = SecuredAction { implicit rs =>
    Tables.Rooms.findRoom(id) match {
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
        Tables.Rooms.createRoom(room)
        NoContent
      }
    )
  }

  def chat(roomId: Long) = WebSocket.async[JsValue] { implicit request  =>
    ChatRoom.join(roomId, user.get)
  }

  def pathToRoom(roomId: Long) = SecuredAction { implicit rs =>
    Ok(Json.toJson(Json.obj("path" -> routes.Application.chat(roomId).webSocketURL())))
  }

  def recentlyMessage(roomId: Long) = SecuredAction { implicit request =>
    val messages = Tables.Rooms.recent_comments(roomId, 20).map { t =>
      TalkMessage("talk", t._2, t._1)
    }
    Ok(Json.toJson(messages))
  }

  def messages(roomId: Long, to: Long) = SecuredAction { implicit request =>
    val messages = Tables.Rooms.comments(roomId, 20, new DateTime(to)).map { c =>
      TalkMessage("talk", c._2, c._1)
    }
    Ok(Json.toJson(messages))
  }

  def rooms = SecuredAction { implicit rs =>
    Ok(Json.toJson(Tables.Rooms.all))
  }

}