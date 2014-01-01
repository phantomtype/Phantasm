package models

import play.api.db.slick.Config.driver.simple._
import org.joda.time.DateTime
import com.github.tototoshi.slick.JodaSupport._

case class RoomUser(id: Option[Long], userId: Long, roomId: Long, created: DateTime)

object RoomUser extends Table[RoomUser]("room_users") {
  def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId  = column[Long]("userId", O.NotNull)
  def roomId  = column[Long]("roomId", O.NotNull)
  def created = column[DateTime]("created", O.NotNull)

  def * = id.? ~ userId ~ roomId ~ created <> (RoomUser.apply _, RoomUser.unapply _)
  def autoinc = * returning id
}
