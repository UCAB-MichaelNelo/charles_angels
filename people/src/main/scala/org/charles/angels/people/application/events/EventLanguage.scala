package org.charles.angels.people.application.events

import org.charles.angels.people.application.Language
import org.charles.angels.people.domain.events.ChildEvent
import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum EventAction[A] {
  case Publish(event: ChildEvent) extends EventAction[Unit]
}

trait EventLanguage[F[_]](using InjectK[EventAction, F]) {
  def publish(event: ChildEvent): Language[F, Unit] = EitherT.liftF(
    Free.liftInject(EventAction.Publish(event))
  )
}
