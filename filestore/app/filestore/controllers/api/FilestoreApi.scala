package filestore.controllers.api

import java.io.{FileInputStream, InputStream}

import com.sosacorp.binder.Refs
import com.sosacorp.content.file.FileStore
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

  def read(path: String) = Action.async { implicit request =>
    future {
      fileStore.read(path).map { stream =>
        new SimpleResult(header = ResponseHeader(OK, Map()), body = Enumerator.fromStream(stream))
      }.getOrElse {
        BadRequest(s"cannot load path $path")
      }
    }
  }

  def size(path: String) = Action.async { implicit request =>
    future {
      readSize(path)
    }
  }

  def write(dest: String) = Action.async(parse.raw) { implicit request =>
    future {
      val content = request.body
      val file = content.asFile
      lclWrite(new FileInputStream(file), dest)
      readSize(dest)
    }
  }

  def list(dir: String, prefixOpt: Option[String]) = Action.async { implicit request =>
    future {
      Ok(Json.toJson(fileStore.list(dir, prefixOpt)))
    }
  }

  def exists(path: String) = Action.async { implicit request =>
    future {
      Ok(Json.toJson(fileStore.exists(path)))
    }
  }

  private def lclWrite(stream: InputStream, path: String) = {
    fileStore.write(stream, path)
  }

  private def readSize(path: String) = {
    fileStore.read(path).map { stream =>
      Ok(Json.toJson(Source.fromInputStream(stream).size))
    }.getOrElse(BadRequest(s"cannot write file $path"))
  }

}
