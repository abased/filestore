package filestore

import java.io.{File, FileInputStream, InputStream}

import com.sosacorp.property.PropertyLoader
import org.apache.commons.io.FileUtils

import scala.collection.convert.WrapAsScala

class LocalFileStore extends FileStore with WrapAsScala {
  self: PropertyLoader  =>

  lazy val rootDir = getPropertyReq("fs.local.root.directory")

  override def write(source: InputStream, dest: String): Unit = {
    val fh = new File(rootDir, dest)
    val parentDir = fh.getParentFile
    FileUtils.forceMkdir(parentDir)
    FileUtils.copyInputStreamToFile(source, fh)
  }

  override def read(path: String) : Option[InputStream] = {
    val fh = new File(rootDir, path)
    if (fh.exists()) {
      Option(new FileInputStream(fh))
    } else {
      None
    }
  }

  override def list(dir: String, prefixOpt: Option[String]) : List[String] = {
    val fh = new File(rootDir, dir)
    if ( fh.exists() && fh.isDirectory ) {
      val files = FileUtils.listFiles(fh, null, false)
      (prefixOpt match {
        case Some(prefix) =>
          val prefixLC = prefix.toLowerCase
          files.filter(_.getName.toLowerCase.startsWith(prefixLC)).map(_.getName)
        case _ => files.map(_.getName)
      }).toList
    } else {
      Nil
    }
  }

  override def exists(path: String): Boolean = new File(rootDir, path).exists()

  override def size(path: String): Option[Long] = {
    val fh = new File(rootDir, path)
    if ( fh.exists() && fh.isFile ) {
      Option(fh.length())
    } else {
      None
    }
  }
}
