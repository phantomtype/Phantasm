package controllers

import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.{SecureSocial, Identity}
import service.RoomService
import models.{User, Users}

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
      case Some(u) => RoomService.createPrivateRoomUnlessExist(u)
      case None => {}
    }
    Ok(views.html.index())
  }

}