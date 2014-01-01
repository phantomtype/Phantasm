package controllers


import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.{SecureSocial, Identity}

object Application extends Controller with securesocial.core.SecureSocial {

  implicit def user(implicit request: RequestHeader):Option[Identity] = {
    SecureSocial.currentUser
  }

  def index = UserAwareAction { implicit rs =>
    Ok(views.html.index())
  }

}