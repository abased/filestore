package filestore

import com.sosacorp.binder.Binder
import filestore.binder.FileServiceModule
import play.api.mvc.WithFilters




object Global extends WithFilters() {

  override def onStart(application: play.api.Application) {

    if(!Lock.locked) {
      synchronized {
        Binder.init(List(
          new FileServiceModule
        ))

        Lock.locked = true
      }
    }

  }

  override def onStop(application: play.api.Application): Unit = {
    Binder.destroy()
  }
}

