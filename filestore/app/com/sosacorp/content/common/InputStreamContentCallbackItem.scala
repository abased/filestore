package com.sosacorp.content.common

import java.io.InputStream

import com.sosacorp.logging.Logging
import org.apache.commons.io.IOUtils

class InputStreamContentCallbackItem(is: InputStream) extends ContentCallbackItem[InputStream] with Logging {

  // this method will handle the stream for you automatically
  override def withContent[R](f: InputStream => R): R = {
    try {
      f(is)
    } finally {
      IOUtils.closeQuietly(is)
    }
  }

  // Use the following methods if you want to correctly handle the stream
  override def getContent(): InputStream = is

  override def getBytes(): Array[Byte] = IOUtils.toByteArray(is)

  override def asString(): String = new String(getBytes)
}
