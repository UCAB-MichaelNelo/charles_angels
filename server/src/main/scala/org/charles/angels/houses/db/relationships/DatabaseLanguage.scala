package org.charles.angels.houses.db.relationships

import java.util.UUID
import org.charles.angels.houses.db.DatabaseLanguage
import org.charles.angels.houses.compiler.CompilerLanguage
import cats.InjectK
import cats.free.Free
import cats.data.EitherT
import org.charles.angels.people.domain.Child

enum DatabaseAction[A] {
  case BindChildToHouse(houseId: UUID, child: UUID)
      extends DatabaseAction[Either[Throwable, Unit]]
  case BindPersonToHouse(houseId: UUID, ci: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case GetChildrenIdsOfHouse(houseId: UUID)
      extends DatabaseAction[Either[Throwable, List[UUID]]]
  case GetHouseIdHousingChild(id: UUID)
      extends DatabaseAction[Either[Throwable, Option[UUID]]]
  case GetHouseIdHousingPerson(ci: Int)
      extends DatabaseAction[Either[Throwable, Option[UUID]]]
  case UnbindPersonFromHouse(ci: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UnbindChildFromHouse(id: UUID)
      extends DatabaseAction[Either[Throwable, Unit]]
}

trait DatabaseLanguage[F[_]](using InjectK[DatabaseAction, F]) {
  def bindChildToHouse(houseId: UUID, child: UUID): CompilerLanguage[F, Unit] =
    EitherT(Free.liftInject(DatabaseAction.BindChildToHouse(houseId, child)))
  def bindPersonToHouse(houseId: UUID, person: Int): CompilerLanguage[F, Unit] =
    EitherT(Free.liftInject(DatabaseAction.BindPersonToHouse(houseId, person)))
  def getHouseHousingChild(childId: UUID): CompilerLanguage[F, Option[UUID]] =
    EitherT(Free.liftInject(DatabaseAction.GetHouseIdHousingChild(childId)))
  def getHouseHousingPerson(ci: Int): CompilerLanguage[F, Option[UUID]] =
    EitherT(Free.liftInject(DatabaseAction.GetHouseIdHousingPerson(ci)))
  def unbindChildFromHouse(id: UUID): CompilerLanguage[F, Unit] =
    EitherT(Free.liftInject(DatabaseAction.UnbindChildFromHouse(id)))
  def unbindPersonFromHouse(ci: Int): CompilerLanguage[F, Unit] =
    EitherT(Free.liftInject(DatabaseAction.UnbindPersonFromHouse(ci)))
  def getChildrenOfHouse(house: UUID): CompilerLanguage[F, List[UUID]] =
    EitherT(Free.liftInject(DatabaseAction.GetChildrenIdsOfHouse(house)))
}
