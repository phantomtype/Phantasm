package service

import models._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.Play.current
import org.joda.time.DateTime
import scala.Some

case class RoomService()

object RoomService {
  def findRoom(id: Long): Option[Room] = {
    DB.withSession {
      implicit session =>
        val q = for {
          (room) <- Tables.Rooms if room.id is id
        } yield room

        q.firstOption
    }
  }

  def findJoinedRooms(u: User)(implicit s:Session): Option[Room] = {
    val q = for {
      (room, roomUser) <- Tables.Rooms leftJoin Tables.RoomUsers on (_.id === _.roomId)
      (roomUser, user) <- Tables.RoomUsers leftJoin Tables.Users on (_.userId === _.uid)
      if roomUser.userId is u.uid
    } yield room

    q.firstOption
  }

  def findOwnedRoom(u: User)(implicit s:Session): Option[Room] = {
    val q = for {
      (room) <- Tables.Rooms
      if room.ownerId is u.uid
    } yield room

    q.firstOption
  }

  def createPrivateRoomUnlessExist(u: User): Long = {
    DB.withSession { implicit session =>
      findOwnedRoom(u) match {
        case Some(room) => room.id.get
        case None =>
          val room = Room(None, u.uid.get, u.fullName + "'s room", true, DateTime.now)
          val savedId = Tables.Rooms.insert(room)
          val roomUser = RoomUser(None, u.uid.get, savedId, DateTime.now)
          Tables.RoomUsers.insert(roomUser)
      }
    }
  }

  def recent_comments(roomId: Long): Seq[(Comment, User)] = DB.withSession {
    implicit session =>
      val q = for {
        comment <- Tables.Comments if comment.roomId is roomId
        user <- Tables.Users if comment.userId === user.uid
      } yield (comment, user)
      q.sortBy(_._1.created.desc).take(20).list
  }

  def createComment(comment: Comment): Comment = {
    DB.withSession { implicit session =>
      Tables.Comments.insert(comment)
      comment
    }
  }
}