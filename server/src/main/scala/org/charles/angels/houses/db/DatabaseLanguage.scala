package org.charles.angels.houses.db

import org.charles.angels.houses.compiler.CompilerLanguage
import java.util.UUID
import java.io.File
import cats.data.Chain
import org.charles.angels.houses.domain.ScheduleBlock
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.House
import org.charles.angels.houses.domain.Contact
import org.charles.angels.houses.domain.Schedule
import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum DatabaseAction[A]:
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
      scheduleId: UUID
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateImage(id: UUID, newImg: File)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateName(id: UUID, name: String)
      extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateRIF(id: UUID, rif: Int)
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
  case StoreSchedule(
      id: UUID,
      monday: Chain[ScheduleBlock],
      tuesday: Chain[ScheduleBlock],
      wednesday: Chain[ScheduleBlock],
      thursday: Chain[ScheduleBlock],
      friday: Chain[ScheduleBlock]
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case AddBlock(
      id: UUID,
      day: Int,
      startTime: LocalTime,
      duration: FiniteDuration
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case RemoveBlock(
      id: UUID,
      day: Int,
      key: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateStartHourOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartHour: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateStartMinuteOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartMinute: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateDurationHoursOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationHours: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case UpdateDurationMinutesOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationMinutes: Int
  ) extends DatabaseAction[Either[Throwable, Unit]]
  case DeleteSchedule(id: UUID)
      extends DatabaseAction[Either[Throwable, Schedule]]
  case GetHouse(id: UUID)
      extends DatabaseAction[Either[Throwable, Option[House]]]
  case GetContact(ci: Int)
      extends DatabaseAction[Either[Throwable, Option[Contact]]]
  case GetSchedule(id: UUID)
      extends DatabaseAction[Either[Throwable, Option[Schedule]]]

trait DatabaseOperation[F[_]]:
  def getHouse(id: UUID): CompilerLanguage[F, Option[House]]
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
      scheduleId: UUID
  ): CompilerLanguage[F, Unit]
  def updateImage(id: UUID, file: File): CompilerLanguage[F, Unit]
  def updateName(id: UUID, name: String): CompilerLanguage[F, Unit]
  def updateRIF(id: UUID, rif: Int): CompilerLanguage[F, Unit]
  def addPhone(id: UUID, phone: String): CompilerLanguage[F, Unit]
  def removePhone(id: UUID, key: Int): CompilerLanguage[F, Unit]
  def updatePhone(
      id: UUID,
      key: Int,
      newPhone: String
  ): CompilerLanguage[F, Unit]
  def updateMaxShares(id: UUID, newMaxShares: Int): CompilerLanguage[F, Unit]
  def updateCurrentShares(
      id: UUID,
      newCurrentShares: Int
  ): CompilerLanguage[F, Unit]
  def updateMinimumAge(id: UUID, newMinimumAge: Int): CompilerLanguage[F, Unit]
  def updateMaximumAge(id: UUID, newMaximumAge: Int): CompilerLanguage[F, Unit]
  def updateCurrentGirlsHelperd(
      id: UUID,
      newCurrentGirlsHelped: Int
  ): CompilerLanguage[F, Unit]
  def updateCurrentBoysHelped(
      id: UUID,
      newCurrentBoysHelped: Int
  ): CompilerLanguage[F, Unit]
  def deleteHouse(id: UUID): CompilerLanguage[F, House]

  def getContact(ci: Int): CompilerLanguage[F, Option[Contact]]
  def storeContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ): CompilerLanguage[F, Unit]
  def changeCI(ci: Int, newCi: Int): CompilerLanguage[F, Unit]
  def changeName(ci: Int, newName: String): CompilerLanguage[F, Unit]
  def changeLastname(ci: Int, lastname: String): CompilerLanguage[F, Unit]
  def changePhone(ci: Int, phone: String): CompilerLanguage[F, Unit]
  def deleteContact(ci: Int): CompilerLanguage[F, Contact]

  def getSchedule(id: UUID): CompilerLanguage[F, Option[Schedule]]
  def storeSchedule(
      id: UUID,
      monday: Chain[ScheduleBlock],
      tuesday: Chain[ScheduleBlock],
      wednesday: Chain[ScheduleBlock],
      thursday: Chain[ScheduleBlock],
      friday: Chain[ScheduleBlock]
  ): CompilerLanguage[F, Unit]
  def addBlock(
      id: UUID,
      day: Int,
      startTime: LocalTime,
      duration: FiniteDuration
  ): CompilerLanguage[F, Unit]
  def removeBlock(
      id: UUID,
      day: Int,
      key: Int
  ): CompilerLanguage[F, Unit]
  def updateStartHourOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartHour: Int
  ): CompilerLanguage[F, Unit]
  def updateStartMinuteOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartMinute: Int
  ): CompilerLanguage[F, Unit]
  def updateDurationHoursOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationHours: Int
  ): CompilerLanguage[F, Unit]
  def updateDurationMinutesOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationMinutes: Int
  ): CompilerLanguage[F, Unit]
  def deleteSchedule(id: UUID): CompilerLanguage[F, Schedule]

class DatabaseLanguage[F[_]](using InjectK[DatabaseAction, F])
    extends DatabaseOperation[F]:
  def getHouse(id: UUID) = EitherT(Free.liftInject(DatabaseAction.GetHouse(id)))
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
      scheduleId: UUID
  ) = EitherT(
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
        scheduleId
      )
    )
  )
  def updateImage(id: UUID, file: File) = EitherT(
    Free.liftInject(DatabaseAction.UpdateImage(id, file))
  )
  def updateName(id: UUID, name: String) = EitherT(
    Free.liftInject(DatabaseAction.UpdateName(id, name))
  )
  def updateRIF(id: UUID, rif: Int) = EitherT(
    Free.liftInject(DatabaseAction.UpdateRIF(id, rif))
  )
  def addPhone(id: UUID, phone: String) = EitherT(
    Free.liftInject(DatabaseAction.AddPhone(id, phone))
  )
  def removePhone(id: UUID, key: Int) = EitherT(
    Free.liftInject(DatabaseAction.RemovePhone(id, key))
  )
  def updatePhone(
      id: UUID,
      key: Int,
      newPhone: String
  ) = EitherT(Free.liftInject(DatabaseAction.UpdatePhone(id, key, newPhone)))
  def updateMaxShares(id: UUID, newMaxShares: Int) = EitherT(
    Free.liftInject(DatabaseAction.UpdateMaxShares(id, newMaxShares))
  )
  def updateCurrentShares(
      id: UUID,
      newCurrentShares: Int
  ) = EitherT(
    Free.liftInject(DatabaseAction.UpdateCurrentShares(id, newCurrentShares))
  )
  def updateMinimumAge(id: UUID, newMinimumAge: Int) = EitherT(
    Free.liftInject(DatabaseAction.UpdateMinimumAge(id, newMinimumAge))
  )
  def updateMaximumAge(id: UUID, newMaximumAge: Int) = EitherT(
    Free.liftInject(DatabaseAction.UpdateMaximumAge(id, newMaximumAge))
  )
  def updateCurrentGirlsHelperd(
      id: UUID,
      newCurrentGirlsHelped: Int
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateCurrentGirlsHelped(id, newCurrentGirlsHelped)
    )
  )
  def updateCurrentBoysHelped(
      id: UUID,
      newCurrentBoysHelped: Int
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateCurrentBoysHelped(id, newCurrentBoysHelped)
    )
  )
  def deleteHouse(id: UUID) = EitherT(
    Free.liftInject(DatabaseAction.DeleteHouse(id))
  )

  def getContact(ci: Int) = EitherT(
    Free.liftInject(DatabaseAction.GetContact(ci))
  )
  def storeContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ) = EitherT(
    Free.liftInject(DatabaseAction.StoreContact(ci, name, lastname, phone))
  )
  def changeCI(ci: Int, newCi: Int) = EitherT(
    Free.liftInject(DatabaseAction.ChangeCI(ci, newCi))
  )
  def changeName(ci: Int, newName: String) = EitherT(
    Free.liftInject(DatabaseAction.ChangeName(ci, newName))
  )
  def changeLastname(ci: Int, lastname: String) = EitherT(
    Free.liftInject(DatabaseAction.ChangeLastname(ci, lastname))
  )
  def changePhone(ci: Int, phone: String) = EitherT(
    Free.liftInject(DatabaseAction.ChangePhone(ci, phone))
  )
  def deleteContact(ci: Int) = EitherT(
    Free.liftInject(DatabaseAction.DeleteContact(ci))
  )

  def getSchedule(id: UUID) = EitherT(
    Free.liftInject(DatabaseAction.GetSchedule(id))
  )
  def storeSchedule(
      id: UUID,
      monday: Chain[ScheduleBlock],
      tuesday: Chain[ScheduleBlock],
      wednesday: Chain[ScheduleBlock],
      thursday: Chain[ScheduleBlock],
      friday: Chain[ScheduleBlock]
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.StoreSchedule(
        id,
        monday,
        tuesday,
        wednesday,
        thursday,
        friday
      )
    )
  )
  def addBlock(
      id: UUID,
      day: Int,
      startTime: LocalTime,
      duration: FiniteDuration
  ) = EitherT(
    Free.liftInject(DatabaseAction.AddBlock(id, day, startTime, duration))
  )
  def removeBlock(
      id: UUID,
      day: Int,
      key: Int
  ) = EitherT(Free.liftInject(DatabaseAction.RemoveBlock(id, day, key)))
  def updateStartHourOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartHour: Int
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateStartHourOnBlock(id, day, key, newStartHour)
    )
  )
  def updateStartMinuteOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newStartMinute: Int
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateStartMinuteOnBlock(id, day, key, newStartMinute)
    )
  )
  def updateDurationHoursOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationHours: Int
  ) = EitherT(
    Free.liftInject(
      DatabaseAction.UpdateDurationHoursOnBlock(id, day, key, newDurationHours)
    )
  )
  def updateDurationMinutesOnBlock(
      id: UUID,
      day: Int,
      key: Int,
      newDurationMinutes: Int
  ) = EitherT(
    Free
      .liftInject(
        DatabaseAction.UpdateDurationMinutesOnBlock(
          id,
          day,
          key,
          newDurationMinutes
        )
      )
  )
  def deleteSchedule(id: UUID) = EitherT(
    Free.liftInject(DatabaseAction.DeleteSchedule(id))
  )
