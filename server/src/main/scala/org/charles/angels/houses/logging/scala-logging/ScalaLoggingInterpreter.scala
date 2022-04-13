package org.charles.angels.houses.logging.`scala-logging`

import cats.~>
import cats.syntax.all.*
import com.typesafe.scalalogging.Logger
import org.charles.angels.houses.logging.LoggingAction
import cats.effect.kernel.Sync
import org.charles.angels.houses.logging.Make
import cats.effect.kernel.Resource

class ScalaLoggingInterpreter[F[_]: Sync](logger: Logger)
    extends (LoggingAction ~> F):
  def apply[A](logAction: LoggingAction[A]) = logAction match
    case LoggingAction.Info(msg)  => Sync[F].interruptible { logger.info(msg) }
    case LoggingAction.Debug(msg) => Sync[F].interruptible { logger.debug(msg) }
    case LoggingAction.Trace(msg) => Sync[F].interruptible { logger.trace(msg) }
    case LoggingAction.Warn(msg)  => Sync[F].interruptible { logger.warn(msg) }
    case LoggingAction.Error(msg) => Sync[F].interruptible { logger.error(msg) }

class ScalaLogging(val loggerName: String);

given [F[_]: Sync]: Make[F, ScalaLogging] with
  def make(input: ScalaLogging) = ScalaLoggingInterpreter(
    Logger(input.loggerName)
  )
    .pure[Resource[F, _]]
