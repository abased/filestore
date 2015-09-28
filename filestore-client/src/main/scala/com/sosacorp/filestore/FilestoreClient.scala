package com.sosacorp.filestore


import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets


import dispatch._, Defaults._
import com.sosacorp.logging.Logging
import com.sosacorp.property.PropertyLoader
import play.api.libs.json._

import scala.concurrent.Future


/**
 * Created by nickdeyoung on 9/28/15.
 */
object FilestoreClient extends PropertyLoader with Logging {

  val filestoreHost = getPropertyReq("filestore.host")
  val filestorePort = getPropertyReq("filestore.port").toInt

  def read(path:String):Future[Either[String, InputStream]] = {
    logger.info(s"Attempting to read the file at $path from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "read" <<? List(("path", path)) OK as.String).either

    val e = for ( e <- req.left ) yield {
      val msg = s"Attempt to read $path failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    for ( s <- e.right ) yield {
      new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)) // WTF, HOW THE HELL DO I KNOW THE CHARSET OF THE FILE THAT WE SAVED ??? THIS IS A MAJOR FLAW, THE FILE STORE MUST SAVE THIS SOME HOW
    }
  }

  def size(path:String):Future[Either[String, Long]] = {
    logger.info(s"Attempting to get size of the file at $path from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "size" <<? List(("path", path)) OK as.String).either

    val e = for ( e <- req.left ) yield {
      val msg = s"Attempt to get size of $path failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    for ( s <- e.right ) yield {
      s.toInt
    }
  }

  def write(source:InputStream, dest:String):Future[Either[String,Long]] = {
    ???
  }

  def list(dir:String, prefixOpt:Option[String]):Future[Either[String, List[String]]] = {
    logger.info(s"Attempting to list the directory at $dir from the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "list" <<? List(("path", dir), ("prefix", prefixOpt.getOrElse(""))) OK as.String).either

    val e = for ( e <- req.left ) yield {
      val msg = s"Attempt to list $dir failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    for (s <- e.right) yield {
      val json = Json.parse(s)
      json match {
        case x:JsArray =>
          x.value.map { v => v.toString()}.toList
        case _ => Nil
      }
    }
  }

  def exists(path:String):Future[Either[String, Boolean]] = {
    logger.info(s"Attempting to see if file at $path exists in the file store")

    val req = Http(host(filestoreHost, filestorePort) / "api" / "exists" <<? List(("path", path)) OK as.String).either

    val e = for ( e <- req.left ) yield {
      val msg = s"Attempting to see if file at $path exists in the file store failed with exception: ${e.getMessage}"
      logger.error(msg)
      msg
    }

    for (s <- e.right) yield {
      val json = Json.parse(s)
      json match {
        case x:JsBoolean =>
          x.value
        case _ => false
      }
    }
  }

}
