package models

import play.api.libs.json.{Json, JsValue, Writes}

abstract class Message()
case class TalkMessage(kind: String, user: User, comment: Comment) extends Message
case class UpdateMembers(kind: String, user: User, members: Set[User]) extends Message

object JsonWrites {

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
        "message" -> comment.message,
        "created" -> comment.created
      )
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
