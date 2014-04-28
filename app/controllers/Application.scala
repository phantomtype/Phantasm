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

  implicit def myRoom(roomId: Long)(implicit request: RequestHeader): Option[Room] = {
    user.get.myRooms().find(_.id.get == roomId)
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

  def room(roomId: Long) = SecuredAction { implicit rs =>
    myRoom(roomId) match {
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
    ChatRoom.join(myRoom(roomId).get.id.get, user.get)
  }

  def pathToRoom(roomId: Long) = SecuredAction { implicit rs =>
    Ok(Json.toJson(Json.obj("path" -> routes.Application.chat(myRoom(roomId).get.id.get).webSocketURL())))
  }

  def messages(roomId: Long, to: Long) = SecuredAction { implicit request =>
    val messages = myRoom(roomId).get.comments(20, new DateTime(to)).map { c =>
      TalkMessage("talk", c._2, c._1)
    }
    Ok(Json.toJson(messages))
  }

  def rooms = SecuredAction { implicit rs =>
    Ok(Json.toJson(user.get.myRooms()))
  }

  def roomMembers(roomId: Long) = SecuredAction { implicit request =>
    Ok(Json.toJson(myRoom(roomId).get.members))
  }

  def addableUsers(roomId: Long) = SecuredAction { implicit request =>
    val users = Tables.Users.all // TODO: check for addable
    val roomMembers = myRoom(roomId).get.members
    val addableUsers = users.filterNot(roomMembers.contains(_))
    Ok(Json.toJson(addableUsers))
  }

  def addMemberToRoom(roomId: Long) = SecuredAction { implicit request =>
    Form(("id" -> number)).bindFromRequest.fold(
      errors => BadRequest,
      (id) => {
        val user = Tables.Users.findById(id).get
        myRoom(roomId).get.addMember(user)
        NoContent
      }
    )
  }
}