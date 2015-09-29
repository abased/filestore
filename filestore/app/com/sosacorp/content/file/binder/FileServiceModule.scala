package com.sosacorp.content.file.binder

import com.sosacorp.binder.Module
import com.sosacorp.content.file._

class FileServiceModule extends Module {

  override def configure() = {
    val fileStoreProvider = new FileStoreProvider with FilestoreProperties
    bind[FileStore].toInstance(fileStoreProvider.getFileStore())
  }
}
//
//class FileServiceTestModule extends Module {
//
//  override def configure() = {
//    val fileStoreProvider = new FileStoreProvider with CoreProperties with CoreTestProperties
//    bind[FileStore].toInstance(fileStoreProvider.getFileStore())
//  }
//
//}
