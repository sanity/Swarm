package org.swarmframework.internal

import org.apache.log4j._

/**
 * Extending this trait provides some convenience methods for logging to log4j
 * without needing to check isEnabledFor(<level>)
 */
trait Logs {

  @scala.transient private[this] lazy val logger = Logger.getLogger(getClass().getName());

  import org.apache.log4j.Level._

  def debug(message: => String) = if (logger.isEnabledFor(DEBUG)) logger.debug(message)

  def debug(message: => String, ex: Throwable) = if (logger.isEnabledFor(DEBUG)) logger.debug(message, ex)

  def debugValue[T](valueName: String, value: => T): T = {
    val result: T = value
    debug(valueName + " == " + result.toString)
    result
  }

  def info(message: => String) = if (logger.isEnabledFor(INFO)) logger.info(message)

  def info(message: => String, ex: Throwable) = if (logger.isEnabledFor(INFO)) logger.info(message, ex)

  def warn(message: => String) = if (logger.isEnabledFor(WARN)) logger.warn(message)

  def warn(message: => String, ex: Throwable) = if (logger.isEnabledFor(WARN)) logger.warn(message, ex)

  def error(ex: Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(ex.toString, ex)

  def error(message: => String) = if (logger.isEnabledFor(ERROR)) logger.error(message)

  def error(message: => String, ex: Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(message, ex)

  def fatal(ex: Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(ex.toString, ex)

  def fatal(message: => String) = if (logger.isEnabledFor(FATAL)) logger.fatal(message)

  def fatal(message: => String, ex: Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(message, ex)
}
