package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.Config.driver.simple._

import slick.lifted.{Join, MappedTypeMapper}

import play.api.db.slick.DB
import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport._

case class Photo(id: Option[Long], userId: Long, url: String, urlThumb: String, title: String, description: Option[String], created: DateTime)

object Photos extends Table[Photo]("photos") {
  def id          = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId      = column[Long]("user_id", O.NotNull)
  def url         = column[String]("url", O.NotNull)
  def urlThumb   = column[String]("url_thumb", O.NotNull)
  def title       = column[String]("title", O.NotNull)
  def description = column[String]("description", O.Nullable)
  def created     = column[DateTime]("created", O.NotNull)

  def * = id.? ~ userId ~ url ~ urlThumb ~ title ~ description.? ~ created <>(Photo.apply _, Photo.unapply _)
  def autoinc = * returning id

  val byId = createFinderBy(_.id)

  def listWithUser(): List[(Photo, User)] = {
    DB withSession { implicit session =>
      (for {
        (post, user) <- Photos leftJoin Users on (_.userId === _.uid)
      } yield (post, user)).list()
    }
  }
}

case class Comment(id: Option[Long], userId: Long, photo_id: Long, comment: String, created: DateTime)

object Comments extends Table[Comment]("comments") {
  def id          = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId      = column[Long]("user_id", O.NotNull)
  def photoId      = column[Long]("photo_id", O.NotNull)
  def comment     = column[String]("comment", O.NotNull)
  def created     = column[DateTime]("created", O.NotNull)

  def * = id.? ~ userId ~ photoId ~ comment ~ created <>(Comment.apply _, Comment.unapply _)
  def autoinc = * returning id

  def list(photoId: Long)(implicit s: Session): List[(Comment, User)] = {
    (for {
      (comment, user) <- Comments leftJoin Users on (_.userId === _.uid)
      if comment.photoId === photoId
    } yield (comment, user)).list
  }
}

