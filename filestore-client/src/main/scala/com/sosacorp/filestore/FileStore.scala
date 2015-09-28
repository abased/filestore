package com.sosacorp.filestore

import java.io.InputStream

trait FileStore {

  def read(path: String) : Option[InputStream]
  def write(source: InputStream, dest: String): Unit
  def list(dir: String, prefixOpt: Option[String]=None) : List[String]
  def exists(path: String): Boolean
  def size(path: String): Option[Long]
}

