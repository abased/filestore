package filestore

import java.io.InputStream
import java.security.PrivilegedExceptionAction

import com.sosacorp.property.PropertyLoader
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, RemoteIterator, Path => HadoopPath}
import org.apache.hadoop.security.UserGroupInformation

class HadoopFileStore extends FileStore {
  self: PropertyLoader =>

  private lazy val configuration = {
    val config = new Configuration()

    val DEFAULT_BLOCK_SIZE = "2097152" // 2mb

    val fsDefaultName = getPropertyReq("fs.hadoop.fs.default.name")
    config.set("fs.default.name", fsDefaultName)

    val fsDefaultBlockSize = getPropertyOpt("fs.hadoop.dfs.block.size").getOrElse(DEFAULT_BLOCK_SIZE)

    config.set("dfs.block.size", fsDefaultBlockSize)

    //NOTE(timc) I was getting an error about FileSystem closed which online
    // threads lead me to believe may be due to caching of FileSystem objects
    //config.setBoolean("fs.hdfs.impl.disable.cache", true)

    config
  }


  /**
   * Copies data from an InputStream to a Hadoop destination. First
   * writes file to a temp file and then moves it atomically into place.
   *
   * @param source Local path to source file
   * @param dest Hadoop path to destination file
   * @return
   */
  override def write(source: InputStream, dest: String): Unit = {

    val tempPath = new HadoopPath(s"${dest}_tmp")
    val destPath = new HadoopPath(dest)

    withPrivilegedFilesystem(fs => {
      cleanUpBeforeWrite(fs, dest)
      var os = fs.create(tempPath, true)
      var is = source
      try {
        IOUtils.copy(is, os)
        is.close()
        is = null
        os.close()
        os = null

        fs.rename(tempPath, destPath)
      } finally {
        if (null != is) {
          is.close()
        }
        if (null != os) {
          os.close()
        }
      }
    })
  }

  override def read(path: String) : Option[InputStream] = {
    withPrivilegedFilesystem { fs =>
      val hp = new HadoopPath(path)
      if (!fs.exists(hp)) {
        None
      } else {
        Option(fs.open(hp))
      }
    }
  }

  override def list(dir: String, prefixOpt: Option[String]=None) : List[String] = {
    val path = new HadoopPath(dir)

    withPrivilegedFilesystem(fs => {
      if (!fs.isDirectory(path)) {
        Nil
      } else {
        val ii = remoteIteratorToIterable(fs.listFiles(path, false))

        prefixOpt match {
          case Some(prefix) =>
            val prefixLC = prefix.toLowerCase
            (for {
              f <- ii
              if f.isFile && f.getPath.getName.toLowerCase.startsWith(prefixLC)
            } yield {
              f.getPath.getName
            }).toList
          case _ =>
            ii.map(_.getPath.getName).toList
        }
      }
    })
  }

  override def exists(path: String): Boolean = {
    withPrivilegedFilesystem(fs => fs.exists(new HadoopPath(path)))
  }

  override def size(path: String): Option[Long] = {
    withPrivilegedFilesystem({fs =>
      try {
        val s = fs.getFileStatus(new HadoopPath(path))
        Option(s.getLen)
      } catch {
        case _:Exception => None
      }
    })
  }

  //========= some helper methods

  private def cleanUpBeforeWrite(fs: FileSystem, path: String) = {
    val destPath = new HadoopPath(path)
    if (fs.exists(destPath)) {
      val oldPath = new HadoopPath(s"${path}_old")
      if (fs.exists(oldPath)) {
        fs.delete(oldPath, false)
      }
      fs.rename(destPath, oldPath)
    }
  }

  private def remoteIteratorToIterable[E](remoteIterator: RemoteIterator[E]): Iterable[E] = {
    new Iterable[E] {
      override def iterator: Iterator[E] = new Iterator[E] {
        override def hasNext: Boolean = remoteIterator.hasNext

        override def next(): E = remoteIterator.next
      }
    }
  }

  private def withPrivilegedFilesystem[R](f : FileSystem => R): R = {

    //NOTE(timc) 2014-10-02 seems like the name of the user should be configurable
    val ugi = UserGroupInformation.createRemoteUser("hadoop")
    ugi.doAs(new PrivilegedExceptionAction[R]() {
      @throws[Exception]
      def run(): R = {
        val fileSystem = FileSystem.get(configuration)
        f(fileSystem)
      }
    })
  }
}

