package service

import models.{Room, Users, RoomUser, User}
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.Play.current
import org.joda.time.DateTime

case class RoomService()

object RoomService {
  def findOwnedRoom(u: User)(implicit s:Session): Option[Room] = {
    val q = for {
      (room, roomUser) <- Room leftJoin RoomUser on (_.id === _.roomId)
      (roomUser, user) <- RoomUser leftJoin Users on (_.userId === _.uid)
      if roomUser.userId is u.uid
    } yield (room, roomUser, user)

    q.map(_._1).firstOption
  }

  def createPrivateRoomUnlessExist(u: User) = {
    DB.withSession { implicit session: Session =>
      findOwnedRoom(u) match {
        case Some(room) => Unit
        case None =>
          val room = Room(None, u.uid.get, "myroom", true, DateTime.now)
          val savedId = Room.autoinc.insert(room)
          val roomUser = RoomUser(None, u.uid.get, savedId, DateTime.now)
          RoomUser.autoinc.insert(roomUser)
      }
    }
  }
}