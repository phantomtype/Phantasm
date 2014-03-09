package models

import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import com.github.tototoshi.slick.MySQLJodaSupport._

case class RoomUser(id: Option[Long], userId: Long, roomId: Long, created: DateTime)

class RoomUsers(tag: Tag) extends Table[RoomUser](tag, "room_users") {
  def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId  = column[Long]("userId", O.NotNull)
  def roomId  = column[Long]("roomId", O.NotNull)
  def created = column[DateTime]("created", O.NotNull)

  def * = (id.?, userId, roomId, created) <> (RoomUser.tupled, RoomUser.unapply)
}
