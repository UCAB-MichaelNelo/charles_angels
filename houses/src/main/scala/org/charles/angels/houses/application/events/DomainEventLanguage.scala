package org.charles.angels.houses.application.events

import org.charles.angels.houses.application.Language
import org.charles.angels.houses.domain.events.HouseEvent
import org.charles.angels.houses.domain.events.ContactEvent
import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum DomainEventAction[A]:
  case NotifyHouseEvent(event: HouseEvent) extends DomainEventAction[Unit]
  case NotifyContactEvent(event: ContactEvent) extends DomainEventAction[Unit]

trait DomainEventAlgebra[F[_]]:
  def notifyHouseEvent(event: HouseEvent): Language[F, Unit]
  def notifyContactEvent(event: ContactEvent): Language[F, Unit]

class DomainEventLanguage[F[_]](using InjectK[DomainEventAction, F])
    extends DomainEventAlgebra[F]:
  def notifyHouseEvent(event: HouseEvent) =
    EitherT.right(Free.liftInject(DomainEventAction.NotifyHouseEvent(event)))
  def notifyContactEvent(event: ContactEvent) =
    EitherT.right(Free.liftInject(DomainEventAction.NotifyContactEvent(event)))
