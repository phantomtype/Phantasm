package models

import play.api.db.slick.Config.driver.simple._

//import play.api.db.slick.DB

import play.api.Play.current
import play.api.db.slick._

import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport._

case class Room(id: Option[Long], ownerId: Long, name: String, isPrivate: Boolean, created: DateTime)

object Room extends Table[Room]("rooms") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def ownerId = column[Long]("ownerId", O.NotNull)

  def name = column[String]("name", O.NotNull)

  def isPrivate = column[Boolean]("private", O.NotNull)

  def created = column[DateTime]("created", O.NotNull)

  def * = id.? ~ ownerId ~ name ~ isPrivate ~ created <>(Room.apply _, Room.unapply _)

  def autoinc = * returning id

  def recent_messages(roomId: Long): Seq[(Comment, User)] = DB.withSession {
    implicit session =>
      val q = for {
        (comment, user) <- Comment leftJoin Users on (_.userId === _.uid)
        if comment.roomId is roomId
      } yield (comment, user)
      q.sortBy(_._1.created.desc).take(10).list
  }
}

case class Message(user: User, comment: Comment)

