package models

import play.api.db.slick.Config.driver.simple._

import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

case class Comment(id: Option[Long], userId: Long, roomId: Long, message: String, created: DateTime)

class Comments(tag: Tag) extends Table[Comment](tag, "comments") {
  def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId  = column[Long]("userId", O.NotNull)
  def roomId  = column[Long]("roomId", O.NotNull)
  def message = column[String]("comment", O.NotNull)
  def created = column[DateTime]("created", O.NotNull)

  def * = (id.?, userId, roomId, message, created) <> (Comment.tupled, Comment.unapply)
}

object Comments {
  val comments = TableQuery[Comments]
}
