package filestore.controllers.api

import java.io.{InputStream, File, FileInputStream}

import com.sosacorp.binder.Refs
import com.sosacorp.content.file.FileStore
import play.api.libs.Files.TemporaryFile
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.io.Source


/**
 * Created by nickdeyoung on 9/28/15.
 */
object FilestoreApi extends Controller {

  lazy val fileStore = Refs.refTo[FileStore]

  def read(path: String) = Action.async { implicit request => future {
    fileStore.read(path).map { stream =>
      new SimpleResult(header = ResponseHeader(OK, Map()), body = Enumerator.fromStream(stream))
    }.getOrElse {
      BadRequest(s"cannot load path $path")
    }
  }}

  def size(path: String) = Action.async { implicit request => future {
    lclRead(path)
  }}

  def write(dest: String) = Action.async { implicit request => future {
    request.body match {
      case AnyContentAsMultipartFormData(formData: MultipartFormData[TemporaryFile]) =>
        formData.files.headOption.map { tmpFile =>
          lclWrite(new FileInputStream(new File(tmpFile.ref.file.getAbsolutePath)), dest)
          lclRead(dest)
        }.getOrElse(BadRequest(s"cannot write file, no file to write $dest"))

      case _ => BadRequest("Expected request of type multipart/form-data")
    }
  }}

  def list(dir: String, prefixOpt: Option[String]) = Action.async { implicit request => future {
    Ok(Json.toJson(fileStore.list(dir, prefixOpt)))
  }}

  def exists(path: String) = Action.async { implicit request => future {
    Ok(Json.toJson(fileStore.exists(path)))
  }}

  private def lclWrite(stream:InputStream, path:String) = {
    fileStore.write(stream, path)
  }

  private def lclRead(path:String) = {
    fileStore.read(path).map { stream =>
      Ok(Json.toJson(Source.fromInputStream(stream).size))
    }.getOrElse(BadRequest(s"cannot write file $path"))
  }

}
