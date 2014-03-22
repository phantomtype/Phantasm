package models

import play.api.libs.json.{Json, JsValue, Writes}

case class Message(kind: String, user: User, comment: Comment)
case class UpdateMembers(kind: String, user: User, members: Set[User])

object JsonWrites {

  implicit val implicitUserWrites = new Writes[User] {
    def writes(user: User): JsValue = {
      Json.obj(
        "id" -> user.uid.get,
        "name" -> user.fullName,
        "avatar"   -> user.avatarUrl.get
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
      Json.obj(
        "kind"     -> message.kind,
        "user"     -> message.user,
        "comment"  -> message.comment
      )
    }
  }

  implicit val implicitUpdateMembersWrites = new Writes[UpdateMembers] {
    def writes(message: UpdateMembers): JsValue = {
      Json.obj(
        "kind"     -> message.kind,
        "user"     -> message.user,
        "members"  -> message.members.toSeq
      )
    }
  }
}
