package service

import scala.util.control.NoStackTrace

trait ThrowableFailure extends Throwable with NoStackTrace with Product with Serializable {
  override def toString: String = scala.runtime.ScalaRunTime._toString(this)
}
