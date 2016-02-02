package filestore.controllers.api

import java.io.{File, FileInputStream, InputStream}

import com.sosacorp.binder.Refs
import com.sosacorp.logging.Logging
import filestore.FileStore
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._


/**
 * Created by nickdeyoung on 9/28/15.
 */
object FilestoreApi extends Controller with Logging {

  lazy val fileStore = Refs.refTo[FileStore]

  def read(path: String) = Action.async { implicit request =>
    future {
      (for {
        is <- fileStore.read(path)
        length <- fileStore.size(path)
      } yield {
        new SimpleResult(header = ResponseHeader(OK, Map(CONTENT_LENGTH -> length.toString)), body = Enumerator.fromStream(is))
      }).getOrElse(
          BadRequest("cant read file at $path")
        )
    }
  }

  def size(path: String) = Action.async { implicit request =>
    future {
      readSize(path)
    }
  }

  def write(dest: String) = Action.async(parse.file(to = File.createTempFile("uploaded", ""))) { implicit request =>
    future {
      logger.info(s"writing $dest")
      lclWrite(new FileInputStream(request.body), dest)
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

    fileStore.size(path).map { size =>
      Ok(Json.toJson(size))
    }.getOrElse(BadRequest(s"cannot write file $path"))
  }

}
