package org.charles.angels.houses.db.people

import org.charles.angels.houses.compiler.CompilerLanguage
import org.charles.angels.people.domain.Child
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import org.charles.angels.people.domain.PersonalInformation
import cats.InjectK
import cats.free.Free
import cats.data.EitherT
import java.util.UUID

enum DatabaseAction[A] {
  case GetChild(id: UUID)
      extends DatabaseAction[Either[Throwable, Option[Child]]]
  case GetChildByCI(ci: Int)
      extends DatabaseAction[Either[Throwable, Option[Child]]]
  case StoreChild(info: ChildInformation, wear: Wear, id: UUID)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateInformation(ci: Int, personalInformation: PersonalInformation)
      extends DatabaseAction[Either[Throwable, Unit]]
  case SaveInformation(information: PersonalInformation)
      extends DatabaseAction[Either[Throwable, Unit]]
  case DeleteInformation(ci: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateAttire(id: UUID, wear: Wear)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdatePhoto(id: UUID, filename: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case DeleteChild(id: UUID) extends DatabaseAction[Either[Throwable, Unit]]
}

trait DatabaseLanguage[F[_]](using InjectK[DatabaseAction, F]) {
  def getChild(id: UUID): CompilerLanguage[F, Option[Child]] = EitherT(
    Free.liftInject(DatabaseAction.GetChild(id))
  )
  def getChildByCI(ci: Int): CompilerLanguage[F, Option[Child]] = EitherT(
    Free.liftInject(DatabaseAction.GetChildByCI(ci))
  )
  def storeChild(
      info: ChildInformation,
      wear: Wear,
      id: UUID
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.StoreChild(info, wear, id))
  )
  def updateInformation(
      ci: Int,
      info: PersonalInformation
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateInformation(ci, info))
  )
  def saveInformation(
      info: PersonalInformation
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.SaveInformation(info))
  )
  def deleteInformation(
      ci: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.DeleteInformation(ci))
  )
  def updateAttire(
      id: UUID,
      wear: Wear
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateAttire(id, wear))
  )
  def updatePhoto(
      id: UUID,
      filename: String
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdatePhoto(id, filename))
  )
  def deleteChild(id: UUID): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.DeleteChild(id))
  )
}
