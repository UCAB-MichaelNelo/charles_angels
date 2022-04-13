package org.charles.angels.houses.logging

import org.charles.angels.houses.compiler.CompilerLanguage
import cats.data.EitherT
import cats.free.Free
import cats.InjectK

enum LoggingAction[A]:
  case Info(msg: String) extends LoggingAction[Unit]
  case Debug(msg: String) extends LoggingAction[Unit]
  case Trace(msg: String) extends LoggingAction[Unit]
  case Warn(msg: String) extends LoggingAction[Unit]
  case Error(msg: String) extends LoggingAction[Unit]

trait LoggingLanguage[F[_]](using InjectK[LoggingAction, F]):
  def info(msg: String): CompilerLanguage[F, Unit] =
    EitherT.right(Free.liftInject(LoggingAction.Info(msg)))
  def debug(msg: String): CompilerLanguage[F, Unit] =
    EitherT.right(Free.liftInject(LoggingAction.Debug(msg)))
  def trace(msg: String): CompilerLanguage[F, Unit] =
    EitherT.right(Free.liftInject(LoggingAction.Trace(msg)))
  def warn(msg: String): CompilerLanguage[F, Unit] =
    EitherT.right(Free.liftInject(LoggingAction.Warn(msg)))
  def error(msg: String): CompilerLanguage[F, Unit] =
    EitherT.right(Free.liftInject(LoggingAction.Error(msg)))
