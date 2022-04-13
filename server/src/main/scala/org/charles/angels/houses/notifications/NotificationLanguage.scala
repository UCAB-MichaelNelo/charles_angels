package org.charles.angels.houses.notifications

import org.charles.angels.people.domain.Child
import org.charles.angels.houses.domain.House
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import org.charles.angels.houses.db.ChildModel
import java.util.UUID

enum Notification {
  case SixMonthsBeforeMaxAge(childId: UUID, house: House)
  case NotificationsStopped
}

enum NotificationAction[A] {
  case Notify(notification: Notification)
      extends NotificationAction[Either[Throwable, Unit]]
}

trait NotificationLanguage[F[_]](using InjectK[NotificationAction, F]) {
  def notify(notification: Notification) = EitherT(
    Free.liftInject(NotificationAction.Notify(notification))
  )
}
