package com.sosacorp.content.common

/**
 * Marker to indicate a piece of content. Can be used to get the content as String, bytes,
 * input stream, etc.
 */
trait ContentItem[+T] {

  /**
   * Get the content
   *
   * @return - the content in the item as parameterized
   */
  def getContent(): T

  /**
   * Get the bytes of the content. Not buffered.
   *
   * @return - the content item in byte array representation
   */
  def getBytes(): Array[Byte]

  /**
   * Get the String of the content. Not buffered.
   *
   * @return - the content item in String representation
   */
  def asString(): String
}
