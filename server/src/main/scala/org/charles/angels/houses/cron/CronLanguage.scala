package org.charles.angels.houses.cron

import cats.implicits.*
import org.charles.angels.houses.compiler.CompilerLanguage
import org.charles.angels.houses.compiler.ServerAction
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import cats.Eval
import java.time.LocalDate

enum CronAction[A] {
  case Schedule(
      at: LocalDate,
      action: Eval[CompilerLanguage[ServerAction, Unit]]
  ) extends CronAction[Either[Throwable, Unit]]
}

trait CronLanguage[F[_]](using InjectK[CronAction, F]) {
  def schedule[A](date: LocalDate)(
      action: => CompilerLanguage[ServerAction, A]
  ) =
    EitherT(
      Free
        .liftInject[F]
        .apply[CronAction, Either[Throwable, Unit]](
          CronAction.Schedule[F](date, Eval.later(action.void))
        )
    )
}
