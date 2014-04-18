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
}

object UserFromIdentity {
  def apply(i: Identity): User = User(None, i.identityId, i.firstName, i.lastName, i.fullName,
    i.email, i.avatarUrl, i.authMethod, i.oAuth1Info, i.oAuth2Info)
}

case class UserSetting(id: Option[Long], user_id: Long, desktopNotifications: Boolean, created: DateTime, updated: DateTime)

case class Room(id: Option[Long], ownerId: Long, name: String, isPrivate: Boolean, created: DateTime) {
  def createComment(user: User, text: String): Comment = {
    val comment = Comment(None, user.uid.get, id.get, text, DateTime.now)
    Tables.Rooms.createComment(comment)
  }

  def owner: User = {
    Tables.Users.findById(ownerId).get
  }

  def latest_post: Option[Comment] = {
    Tables.Rooms.recent_comments(this.id.get, 1).headOption match {
      case Some(comment) =>
        Some(comment._1)
      case None =>
        None
    }
  }
}

case class Comment(id: Option[Long], userId: Long, roomId: Long, message: String, created: DateTime) {
  def user: User = {
    Tables.Users.findById(userId).get
  }
}

case class RoomUser(id: Option[Long], userId: Long, roomId: Long, created: DateTime)
