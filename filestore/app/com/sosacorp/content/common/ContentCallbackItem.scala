package com.sosacorp.content.common

trait ContentCallbackItem[+T] extends ContentItem[T] {

  def withContent[R](f : T => R): R

}
