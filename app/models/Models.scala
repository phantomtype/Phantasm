package models

import org.joda.time.DateTime
import securesocial.core._
import securesocial.core.IdentityId
import securesocial.core.OAuth1Info
import securesocial.core.OAuth2Info
import securesocial.core.PasswordInfo

case class User(uid: Option[Long] = None,
                identityId: IdentityId,
                firstName: String,
                lastName: String,
                fullName: String,
                email: Option[String],
                avatarUrl: Option[String],
                authMethod: AuthenticationMethod,
                oAuth1Info: Option[OAuth1Info],
                oAuth2Info: Option[OAuth2Info],
                passwordInfo: Option[PasswordInfo] = None) extends Identity {

  def userSetting(): Option[UserSetting] = {
    Tables.UserSettings.findBy(this)
  }

  def saveUserSetting(desktopNotifications: Boolean): Unit = {
    Tables.UserSettings.saveUserSetting(this, desktopNotifications)
  }

  def myRooms(): Set[Room] = {
    val publicRooms = Tables.Rooms.public_rooms()
    val joinedRooms = Tables.RoomUsers.findByUserId(this.uid.get)
    (publicRooms ++ joinedRooms).toSet
  }
}

object UserFromIdentity {
  def apply(i: Identity): User = User(None, i.identityId, i.firstName, i.lastName, i.fullName,
    i.email, i.avatarUrl, i.authMethod, i.oAuth1Info, i.oAuth2Info)
}

case class UserSetting(id: Option[Long], user_id: Long, desktopNotifications: Boolean, created: DateTime, updated: DateTime)

case class Room(id: Option[Long], ownerId: Long, name: String, isPrivate: Boolean, created: DateTime) {
  def createComment(user: User, text: String, replyTo: Option[Long]): Comment = {
    val comment = Comment(None, user.uid.get, id.get, text, replyTo, DateTime.now)
    Tables.Rooms.createComment(comment)
  }

  def owner: User = {
    Tables.Users.findById(ownerId).get
  }

  def latest_post: Option[Comment] = {
    Tables.Rooms.comments(this.id.get, 1).headOption match {
      case Some(comment) =>
        Some(comment._1)
      case None =>
        None
    }
  }

  def comments(size: Int, to: DateTime): Seq[(Comment, User)] = {
    Tables.Rooms.comments(this.id.get, size, to)
  }

  def members: Seq[User] = {
    Tables.RoomUsers.findByRoomId(this.id.get)
  }

  def addMember(user: User): Unit = {
    Tables.Rooms.addMember(this, user)
  }
}

case class Comment(id: Option[Long], userId: Long, roomId: Long, message: String, replyTo: Option[Long], created: DateTime) {
  def user: User = {
    Tables.Users.findById(userId).get
  }

  def reply_to: Option[Comment] = {
    replyTo match {
      case Some(id) =>
        Tables.Comments.findById(replyTo.get)
      case None =>
        None
    }
  }
}

case class RoomUser(id: Option[Long], userId: Long, roomId: Long, created: DateTime)
