package org.charles.angels.houses.cron

import cats.syntax.all.*
import org.charles.angels.houses.compiler.CompilerLanguage
import org.charles.angels.houses.compiler.ServerAction
import cats.InjectK
import cats.data.EitherT
import cats.Eval
import java.time.LocalDate
import cats.free.Free

enum CronAction[A] {
  case Schedule(
      at: LocalDate,
      action: Eval[CompilerLanguage[ServerAction, Unit]]
  ) extends CronAction[Either[Throwable, Unit]]
}

trait CronLanguage[F[_]](using InjectK[CronAction, F]) {
  def schedule[A](date: LocalDate)(
      action: => CompilerLanguage[ServerAction, A]
  ): CompilerLanguage[F, Unit] =
    EitherT(
      Free
        .liftInject[F]
        .apply[CronAction, Either[Throwable, Unit]](
          CronAction.Schedule(date, Eval.later(action.void))
        )
    )
}
