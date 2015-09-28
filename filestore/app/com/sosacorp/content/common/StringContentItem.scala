package com.sosacorp.content.common

class StringContentItem(val buffer: String) extends ContentItem[String] {

  override def getContent(): String = buffer

  override def getBytes(): Array[Byte] = buffer.getBytes("UTF-8")

  override def asString(): String = buffer
}
