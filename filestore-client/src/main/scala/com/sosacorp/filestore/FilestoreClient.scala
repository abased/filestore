package com.sosacorp.filestore


import java.io.{ByteArrayInputStream, InputStream}

import com.sosacorp.logging.Logging
import com.sosacorp.property.PropertyLoader
import dispatch._
import org.apache.commons.io.IOUtils
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}
import scala.concurrent._

/**
 * Created by nickdeyoung on 9/28/15.
 */
class FilestoreClient extends PropertyLoader with Logging {

  val filestoreHost = getPropertyReq("filestore.host")
  val filestorePort = getPropertyReq("filestore.port").toInt

  def read(path: String): Future[Either[String, InputStream]] = {
    logger.info(s"Attempting to read the file at $path from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "read" <<? List(("path", path)) OK as.Bytes).either

    // handle the Left[Throwable] into a Left[String]
    // won't execute if Right[Array[Byte]]]
    val e = for (e <- req.left) yield {
      val msg = s"Attempt to read $path failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    // handle the Right[Array[Byte]] into a Right[InputStream]
    // won't execute if Left[String]
    for (s <- e.right) yield {
      new ByteArrayInputStream(s)
    }
  }

  def size(path: String): Future[Either[String, Long]] = {
    logger.info(s"Attempting to get size of the file at $path from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "size" <<? List(("path", path)) OK as.String).either

    // handle the Left[Throwable] into a Left[String]
    // won't execute if Right[String]
    val e = for (e <- req.left) yield {
      val msg = s"Attempt to get size of $path failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    // handle the Right[String] into a Right[Long]
    // won't execute if Left[String]
    // also handle parsing errors
    for {
      s <- e.right
      n <- json2Number(s).right
    } yield {
      n
    }

  }

  private def json2Number(s: String): Future[Either[String, Long]] = {
    future {
      Json.parse(s) match {
        case x: JsNumber =>
          Right(x.value.toLong)
        case _ =>
          Left(s"$s is not a number")
      }
    }
  }

  def write(source: InputStream, dest: String): Future[Either[String, Long]] = {

    logger.info(s"Attempting to write the stream to $dest")
    val ba = IOUtils.toByteArray(source)

    val req = Http((host(filestoreHost, filestorePort) / "api" / "write" <<? List(("path", dest))).POST.setBody(ba) OK as.String).either

    // handle the Left[Throwable] into a Left[String]
    // won't execute if Right[String]
    val e = for (e <- req.left) yield {
      val msg = s"Attempt to write $dest failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    // handle the Right[String] into a Right[Long]
    // won't execute if Left[String]
    // also handle parsing errors
    for {
      s <- e.right
      n <- json2Number(s).right
    } yield {
      n
    }

  }


  def list(dir: String, prefixOpt: Option[String]): Future[Either[String, List[String]]] = {
    logger.info(s"Attempting to list the directory at $dir from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "list" <<? List(("path", dir), ("prefix", prefixOpt.getOrElse(""))) OK as.String).either

    // handle the Left[Throwable] into a Left[String]
    // won't execute if Right[String]
    val e = for (e <- req.left) yield {
      val msg = s"Attempt to list $dir failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    // handle the Right[String] into a Right[List[String]]
    // won't execute if Left[String]
    for {
      s <- e.right
      l <- json2StringList(s).right
    } yield {
      l
    }
  }

  private def json2StringList(s: String):Future[Either[String, List[String]]] = {
    future {
      Json.parse(s) match {
        case x: JsArray =>
          x.value.foldLeft(Right(List.empty[String]): Either[String, List[String]]) {
            case (Right(list), current) =>
              Try(current.as[JsString].value) match {
                case Success(int) => Right(list :+ int)
                case Failure(err) => Left(err.getMessage)
              }
            case (Left(err), _) => Left(err)
          }

        case _ =>
          Left(s"json $s is not an array of string")
      }
    }
  }

  def exists(path: String): Future[Either[String, Boolean]] = {
    logger.info(s"Attempting to see if file at $path exists in the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "exists" <<? List(("path", path)) OK as.String).either

    // handle the Left[Throwable] into a Left[String]
    // won't execute if Right[String]
    val e = for (e <- req.left) yield {
      val msg = s"Attempting to see if file at $path exists in the file store failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    // handle the Right[String] into a Right[Boolean]
    // won't execute if Left[String]
    for (s <- e.right) yield {
      val json = Json.parse(s)
      json match {
        case x: JsBoolean =>
          x.value
        case _ => false
      }
    }
  }

}
