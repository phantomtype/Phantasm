package service

import models._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.Play.current
import org.joda.time.DateTime
import scala.Some

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

  def createPrivateRoomUnlessExist(u: User): Long = {
    DB.withSession { implicit session: Session =>
      findOwnedRoom(u) match {
        case Some(room) => room.id.get
        case None =>
          val room = Room(None, u.uid.get, "myroom", true, DateTime.now)
          val savedId = Room.autoinc.insert(room)
          val roomUser = RoomUser(None, u.uid.get, savedId, DateTime.now)
          RoomUser.autoinc.insert(roomUser)
      }
    }
  }

  def createComment(comment: Comment) = {
    DB.withSession { implicit session: Session =>
      Comment.autoinc.insert(comment)
    }
  }
}