package models

import play.api.libs.json._

abstract class Message()
case class TalkMessage(kind: String, user: User, comment: Comment) extends Message
case class UpdateMembers(kind: String, user: User, members: Set[User]) extends Message

object JsonWrites {

  implicit val implicitUserSettingWrites = new Writes[Option[UserSetting]] {
    def writes(userSetting: Option[UserSetting]): JsValue = {
      userSetting match {
        case Some(s) =>
          Json.obj(
            "id" -> s.id,
            "user_id" -> s.user_id,
            "desktopNotifications" -> s.desktopNotifications
          )
        case None =>
          Json.obj()
      }
    }
  }

  implicit val implicitUserWrites = new Writes[User] {
    def writes(user: User): JsValue = {
      Json.obj(
        "id" -> user.uid.get,
        "fullName" -> user.fullName,
        "firstName" -> user.firstName,
        "avatarUrl"   -> user.avatarUrl.get
      )
    }
  }

  implicit val implicitCommentWrites = new Writes[Comment] {
    def writes(comment: Comment): JsValue = {
      Json.obj(
        "user" -> comment.user,
        "message" -> comment.message,
        "created" -> comment.created
      )
    }
  }

  implicit val implicitRoomWrites = new Writes[Room] {
    def writes(room: Room): JsValue = {
      val latest_post: JsValue = room.latest_post match {
        case Some(post) => Json.toJson(post)
        case None => JsNull
      }
      val result = Json.obj(
        "id" -> room.id.get,
        "name" -> room.name,
        "owner" -> room.owner,
        "is_private" -> room.isPrivate,
        "latest_post" -> latest_post
      )
      result
    }
  }

  implicit val implicitMessageWrites = new Writes[Message] {
    def writes(message: Message): JsValue = {
      message match {
        case m:TalkMessage =>
          Json.obj(
            "kind"     -> m.kind,
            "user"     -> m.user,
            "comment"  -> m.comment
          )
        case m:UpdateMembers =>
          Json.obj(
            "kind"     -> m.kind,
            "user"     -> m.user,
            "members"  -> m.members.toSeq
          )
      }
    }
  }
}
