package com.sosacorp.filestore


import java.io.InputStream

import com.sosacorp.logging.Logging
import com.sosacorp.property.PropertyLoader

import scala.concurrent.Future

/**
 * Created by nickdeyoung on 9/28/15.
 */
object FilestoreClient extends PropertyLoader with Logging {

  val filestoreHost = getPropertyReq("filestore.host")
  val filestorePort = getPropertyReq("filestore.port").toInt

  def read(path:String):Future[Either[String, InputStream]] = {
    ???
  }

  def size(path:String):Future[Either[String, Long]] = {
    ???
  }

  def write(source:InputStream, dest:String):Future[Either[String,Long]] = {
    ???
  }

  def list(dir:String, prefixOpt:Option[String]):Future[Either[String, List[String]]] = {
    ???
  }

  def exists(path:String):Future[Either[String, Boolean]] = {
    ???
  }

}
