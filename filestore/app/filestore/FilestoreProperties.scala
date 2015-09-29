package filestore

import com.sosacorp.property.PropertyLoader

/**
  * Created by nickdeyoung on 9/28/15.
  */
trait FilestoreProperties extends PropertyLoader {

   override def propertyFilenameList = super.propertyFilenameList ::: List("filestore.properties")

 }
