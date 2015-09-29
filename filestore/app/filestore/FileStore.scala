package filestore

import java.io.InputStream

/**
 * Created by nickdeyoung on 9/29/15.
 */
trait FileStore {

  def read(path: String) : Option[InputStream]
  def write(source: InputStream, dest: String): Unit
  def list(dir: String, prefixOpt: Option[String]=None) : List[String]
  def exists(path: String): Boolean
  def size(path: String): Option[Long]
}
