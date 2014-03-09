package service

import models._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.Play.current
import org.joda.time.DateTime
import scala.Some

case class RoomService()

object RoomService {
  val rooms = TableQuery[Rooms]
  val roomUsers = TableQuery[RoomUsers]
  val users = TableQuery[Users]
  val comments = TableQuery[Comments]

  def findOwnedRoom(u: User)(implicit s:Session): Option[Room] = {
    val q = for {
      (room, roomUser) <- rooms leftJoin roomUsers on (_.id === _.roomId)
      (roomUser, user) <- roomUsers leftJoin users on (_.userId === _.uid)
      if roomUser.userId is u.uid
    } yield (room, roomUser, user)

    q.map(_._1).firstOption
  }

  def createPrivateRoomUnlessExist(u: User): Long = {
    DB.withSession { implicit session =>
      findOwnedRoom(u) match {
        case Some(room) => room.id.get
        case None =>
          val room = Room(None, u.uid.get, "myroom", true, DateTime.now)
          val savedId = rooms.insert(room)
          val roomUser = RoomUser(None, u.uid.get, savedId, DateTime.now)
          roomUsers.insert(roomUser)
      }
    }
  }

  def createComment(comment: Comment) = {
    DB.withSession { implicit session =>
      comments.insert(comment)
    }
  }
}