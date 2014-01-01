package controllers

case class PhotoForm(title: Option[String], description: Option[String])
case class CommentForm(id: Option[Long], photoId: Long, comment: String)

