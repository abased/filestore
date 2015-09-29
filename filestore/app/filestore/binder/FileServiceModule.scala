package filestore.binder

import com.sosacorp.binder.Module
import filestore.{FileStoreProvider, FilestoreProperties, FileStore}

class FileServiceModule extends Module {

  override def configure() = {
    val fileStoreProvider = new FileStoreProvider with FilestoreProperties
    bind[FileStore].toInstance(fileStoreProvider.getFileStore())
  }
}

