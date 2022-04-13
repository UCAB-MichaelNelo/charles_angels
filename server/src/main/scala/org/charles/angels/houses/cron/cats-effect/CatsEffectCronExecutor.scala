package org.charles.angels.houses.cron.`cats-effect`

import cats.~>
import cats.syntax.all.*
import cats.effect.syntax.all.*
import org.charles.angels.houses.cron.CronAction
import org.charles.angels.houses.compiler.ServerAction
import cats.data.EitherK
import cats.effect.kernel.Async
import cats.Monad
import cats.effect.std.Supervisor
import java.time.temporal.ChronoUnit
import java.time.LocalTime
import cats.kernel.instances.FiniteDurationOrder
import scala.concurrent.duration.FiniteDuration
import cats.InjectK
import java.time.LocalDate
import org.charles.angels.houses.cron.Make
import java.time.ZoneId
import cats.effect.kernel.Sync

private type Lang[F[_], A] = EitherK[CronAction, F, A]

class CatsEffectCronExecutor[F[_], G[_]: Async](
    supervisor: Supervisor[G],
    executor: F ~> G
)(using I: InjectK[ServerAction, Lang[F, _]])
    extends (CronAction ~> G) {

  private val injectedExecutor = I.inj andThen (this or executor)

  def apply[A](action: CronAction[A]) = action match {
    case CronAction.Schedule(at, action) =>
      val actionEffect = Async[G].defer {
        action.value.value.foldMap(injectedExecutor)
      }
      val seconds = Sync[G].delay {
        val zoneId = ZoneId.systemDefault()
        ChronoUnit.SECONDS.between(
          LocalDate.now.atStartOfDay(zoneId),
          at.atStartOfDay(zoneId)
        )
      }

      supervisor
        .supervise(for
          seconds <- seconds
          _ <- Async[G].delayBy(actionEffect, FiniteDuration(seconds, "s"))
        yield ())
        .void
        .attempt
  }
}

final case class CatsEffectScheduler[F[_], G[_]](executor: F ~> G)

object CatsEffectScheduler {
  class CatsEffectSchedulerPartiallyApplied[G[_]] {
    def apply[F[_]](executor: F ~> G) = new CatsEffectScheduler(executor)
  }
  def apply[G[_]] = CatsEffectSchedulerPartiallyApplied[G]
}

given [F[_], G[_]: Async](using
    InjectK[ServerAction, Lang[F, _]]
): Make[G, CatsEffectScheduler[F, G]] with
  def make(schedule: CatsEffectScheduler[F, G]) =
    Supervisor[G].map(CatsEffectCronExecutor(_, schedule.executor))
