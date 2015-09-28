package com.sosacorp.content.file


import com.sosacorp.content.common.FilestoreProperties
import com.sosacorp.property.PropertyLoader

object FileStoreType extends Enumeration {
  type FileStoreType = Value
  val local = Value("local")
  val hadoop = Value("hadoop")
}

class FileStoreProvider {
  self: PropertyLoader =>

  lazy val hadoopFileStore = new HadoopFileStore with FilestoreProperties

  lazy val localFileStore = new LocalFileStore with FilestoreProperties

  def getFileStore(): FileStore = {
    val fs =  getPropertyOpt("fs.impl")
    val fsType = try {
      FileStoreType.withName(fs.getOrElse(FileStoreType.local.toString))
    } catch {
      case e: Exception =>
        logger.warn(s"Did not understand file store type ${fs}", e)
        FileStoreType.local
    }
    fsType match {
      case FileStoreType.local => localFileStore
      case FileStoreType.hadoop => hadoopFileStore
    }
  }

}
