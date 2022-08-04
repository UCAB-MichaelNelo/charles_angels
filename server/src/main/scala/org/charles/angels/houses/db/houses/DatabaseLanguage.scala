package org.charles.angels.houses.db.houses

import java.util.UUID
import java.io.File
import cats.data.Chain

import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.House
import org.charles.angels.houses.domain.Contact
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import org.charles.angels.houses.compiler.CompilerLanguage

enum DatabaseAction[A]:
  case DoesRifExist(rif: Int) extends DatabaseAction[Either[Throwable, Option[Int]]]
  case GetAllHouses extends DatabaseAction[Either[Throwable, Vector[House]]]
  case StoreHouse(
      id: UUID,
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      contactCI: Int,
      scheduleStartTime: LocalTime,
      scheduleEndTime: LocalTime
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateImage(id: UUID, newImg: File)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateName(id: UUID, name: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateRIF(id: UUID, rif: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateAddress(id: UUID, address: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case AddPhone(id: UUID, phone: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case RemovePhone(id: UUID, key: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdatePhone(id: UUID, key: Int, newPhone: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateMaxShares(id: UUID, newMaxShares: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateCurrentShares(id: UUID, newCurrentShares: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateMinimumAge(id: UUID, newMinimumAge: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateMaximumAge(id: UUID, newMaximumAge: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateCurrentGirlsHelped(id: UUID, newCurrentGirlsHelped: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateCurrentBoysHelped(id: UUID, newCurrentBoysHelped: Int)
      extends DatabaseAction[Either[Throwable, Unit]]
  case DeleteHouse(id: UUID) extends DatabaseAction[Either[Throwable, House]]
  case StoreContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case ChangeCI(
      ci: Int,
      newCi: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case ChangeName(
      ci: Int,
      newName: String
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case ChangeLastname(
      ci: Int,
      newLastname: String
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case ChangePhone(
      ci: Int,
      newPhone: String
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case DeleteContact(
      ci: Int
  ) extends DatabaseAction[Either[Throwable, Contact]]
  case UpdateStartScheduleTime(
      id: UUID,
      startTime: LocalTime
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateEndingScheduleTime(
      id: UUID,
      endTime: LocalTime
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateContactCIOfHouse(
    id: UUID,
    ci: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case GetHouse(id: UUID)
      extends DatabaseAction[Either[Throwable, Option[House]]]
  case GetContact(ci: Int)
      extends DatabaseAction[Either[Throwable, Option[Contact]]]
  case GetAllContacts
      extends DatabaseAction[Either[Throwable, Vector[Contact]]]

trait DatabaseLanguage[F[_]](using InjectK[DatabaseAction, F]):
  def doesRifExist(rif: Int) = EitherT(
    Free.liftInject(DatabaseAction.DoesRifExist(rif))
  )
  def getAllHouses = EitherT(Free.liftInject(DatabaseAction.GetAllHouses))
  def getHouse(id: UUID): CompilerLanguage[F, Option[House]] = EitherT(
    Free.liftInject(DatabaseAction.GetHouse(id))
  )
  def storeHouse(
      id: UUID,
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      contactCI: Int,
      scheduleStartTime: LocalTime,
      scheduleEndTime: LocalTime
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(
      DatabaseAction.StoreHouse(
        id,
        img,
        name,
        rif,
        phones,
        address,
        maxShares,
        currentShares,
        minimumAge,
        maximumAge,
        currentGirlsHelped,
        currentBoysHelped,
        contactCI,
        scheduleStartTime,
        scheduleEndTime
      )
    )
  )
  def updateImage(id: UUID, file: File): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateImage(id, file))
  )
  def updateName(id: UUID, name: String): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateName(id, name))
  )
  def updateAddress(id: UUID, address: String): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateAddress(id, address))
  )
  def updateRIF(id: UUID, rif: Int): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateRIF(id, rif))
  )
  def addPhone(id: UUID, phone: String): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.AddPhone(id, phone))
  )
  def removePhone(id: UUID, key: Int): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.RemovePhone(id, key))
  )
  def updatePhone(
      id: UUID,
      key: Int,
      newPhone: String
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdatePhone(id, key, newPhone))
  )
  def updateMaxShares(id: UUID, newMaxShares: Int): CompilerLanguage[F, Unit] =
    EitherT(
      Free.liftInject(DatabaseAction.UpdateMaxShares(id, newMaxShares))
    )
  def updateCurrentShares(
      id: UUID,
      newCurrentShares: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateCurrentShares(id, newCurrentShares))
  )
  def updateMinimumAge(
      id: UUID,
      newMinimumAge: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateMinimumAge(id, newMinimumAge))
  )
  def updateMaximumAge(
      id: UUID,
      newMaximumAge: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateMaximumAge(id, newMaximumAge))
  )
  def updateCurrentGirlsHelped(
      id: UUID,
      newCurrentGirlsHelped: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateCurrentGirlsHelped(id, newCurrentGirlsHelped)
    )
  )
  def updateCurrentBoysHelped(
      id: UUID,
      newCurrentBoysHelped: Int
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateCurrentBoysHelped(id, newCurrentBoysHelped)
    )
  )
  def setContactCIOfHouse(
    id: UUID,
    ci: Int
  ): CompilerLanguage[F, Unit] = EitherT(Free.liftInject(DatabaseAction.UpdateContactCIOfHouse(id, ci)))
  def deleteHouse(id: UUID): CompilerLanguage[F, House] = EitherT(
    Free.liftInject(DatabaseAction.DeleteHouse(id))
  )
  def updateStartScheduleTime(id: UUID, startTime: LocalTime): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateStartScheduleTime(id, startTime))
  )
  def updateEndingScheduleTime(id: UUID, endTime: LocalTime): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.UpdateEndingScheduleTime(id, endTime))
  ) 
  def getContact(ci: Int): CompilerLanguage[F, Option[Contact]] = EitherT(
    Free.liftInject(DatabaseAction.GetContact(ci))
  )
  def getAllContacts: CompilerLanguage[F, Vector[Contact]] = EitherT(
    Free.liftInject(DatabaseAction.GetAllContacts)
  )
  def storeContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.StoreContact(ci, name, lastname, phone))
  )
  def changeCI(ci: Int, newCi: Int): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.ChangeCI(ci, newCi))
  )
  def changeName(ci: Int, newName: String): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.ChangeName(ci, newName))
  )
  def changeLastname(ci: Int, lastname: String): CompilerLanguage[F, Unit] =
    EitherT(
      Free.liftInject(DatabaseAction.ChangeLastname(ci, lastname))
    )
  def changePhone(ci: Int, phone: String): CompilerLanguage[F, Unit] = EitherT(
    Free.liftInject(DatabaseAction.ChangePhone(ci, phone))
  )
  def deleteContact(ci: Int): CompilerLanguage[F, Contact] = EitherT(
    Free.liftInject(DatabaseAction.DeleteContact(ci))
  )