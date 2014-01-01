package controllers

import securesocial.controllers.DefaultTemplatesPlugin
import play.api.mvc.{RequestHeader, Request}
import play.api.data.Form
import play.api.templates.Html

class Login(application: play.api.Application) extends DefaultTemplatesPlugin(application) {

  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String]): Html = {
    views.html.login(form, msg)
  }

}
