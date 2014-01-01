package controllers

import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

import models._
import play.api.data._
import play.api.data.Forms._
import service.AWSService
import play.api.mvc.{Action, RequestHeader, Controller}
import securesocial.core.{SecureSocial, Identity}
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Writes, Json}
import javax.imageio.ImageIO
import org.imgscalr.Scalr
import java.io.File
import java.nio.file.{Paths, Path}
import org.imgscalr.Scalr.Method

object Application extends Controller with securesocial.core.SecureSocial {

  implicit def user(implicit request: RequestHeader):Option[Identity] = {
    SecureSocial.currentUser
  }

  def index = UserAwareAction { implicit rs =>
    Ok(views.html.index(Photos.listWithUser(), photoForm))
  }

  val photoForm = Form {
    mapping(
      "description" -> optional(text),
      "title" -> optional(text)
    )(PhotoForm.apply)(PhotoForm.unapply)
  }

  case class Upload(files: List[UploadFile])
  case class UploadFile(name: String, size: Long, url: String, thumbnailUrl: String)

  implicit val implicitUploadFileWrites = new Writes[UploadFile] {
    def writes(uploadFile: UploadFile): JsValue = {
      Json.obj(
        "name" -> uploadFile.name,
        "size" -> uploadFile.size,
        "url"  -> uploadFile.url,
        "thumbnailUrl" -> uploadFile.thumbnailUrl
      )
    }
  }

  implicit val implicitUploadWrites = new Writes[Upload] {
    def writes(upload: Upload): JsValue = {
      Json.obj(
        "files" -> upload.files
      )
    }
  }

  def save = SecuredAction(parse.multipartFormData) { implicit request =>
    val picture = request.body.file("picture")
    picture match {
      case Some(p) => {
        val image = ImageIO.read(p.ref.file)
        val thumb = Scalr.resize(image, Method.ULTRA_QUALITY, 320, Scalr.OP_ANTIALIAS)
        val tempFile = File.createTempFile("thumb", p.filename)

        val filename = p.filename.substring(0, p.filename.lastIndexOf("."))
        val ext = p.filename.substring(p.filename.lastIndexOf(".") + 1)
        ImageIO.write(thumb, ext, tempFile)

        val url = AWSService.pushS3(p.filename, p.contentType.get, p.ref.file)
        val thumbUrl = AWSService.pushS3(filename + "_thumb." + ext, p.contentType.get, tempFile)

        photoForm.bindFromRequest.fold(
          formWithError => {BadRequest},
          form => {
            val title:String = form.title match {
              case Some(s) => s
              case None => p.filename
            }

            DB withSession { implicit session: Session =>
              val userId = Users.findByUserId(user.get.identityId).get.uid.get
              val photo = Photo(None, userId, url, thumbUrl, title, form.description, DateTime.now)
              Photos.autoinc.insert(photo)
            }

            val jsonFile = UploadFile(title, p.ref.file.length, url, thumbUrl)
            val json = Upload(List(jsonFile))
            Ok(Json.toJson(json))
          }
        )
      }
      case None => {
        BadRequest
      }
    }
  }

  def photo(id: Long) = UserAwareAction { implicit rs =>
    DB withSession { implicit session =>
      Photos.byId(id).firstOption.map { photo =>
        Ok(views.html.photo(id, photo, Comments.list(id), commentForm))
      }.getOrElse(NotFound)
    }
  }

  val commentForm = Form {
    mapping(
      "id" -> optional(longNumber),
      "photoId" -> longNumber,
      "comment" -> nonEmptyText
    )(CommentForm.apply)(CommentForm.unapply)
  }

  def comment(id: Long) = SecuredAction { implicit rs =>
    DB withSession { implicit session =>
      commentForm.bindFromRequest.fold (
        formWithError => {
          val photo = Photos.byId(id).firstOption.get
          BadRequest(views.html.photo(id, photo, Comments.list(id), formWithError))
        },
        form => {
          val userId = Users.findByUserId(user.get.identityId).get.uid.get
          val comment = Comment(form.id, userId, form.photoId, form.comment, DateTime.now)
          Comments.insert(comment)
          Redirect(routes.Application.photo(id))
        }
      )
    }
  }
}