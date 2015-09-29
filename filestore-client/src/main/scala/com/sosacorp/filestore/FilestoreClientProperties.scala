package com.sosacorp.filestore

import com.sosacorp.property.PropertyLoader

/**
 * Created by nickdeyoung on 9/29/15.
 */
trait FilestoreClientProperties extends PropertyLoader {

  override def propertyFilenameList = super.propertyFilenameList ::: List("filestore.client.properties")

}