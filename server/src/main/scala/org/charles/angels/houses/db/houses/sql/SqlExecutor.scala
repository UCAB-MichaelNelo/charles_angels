package org.charles.angels.houses.db.houses.sql

import cats.~>
import cats.effect.implicits.*
import cats.syntax.all.*
import doobie.implicits.*
import doobie.syntax.all.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.ConnectionIO
import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import org.charles.angels.houses.db.houses.DatabaseAction
import org.charles.angels.houses.domain.House
import org.charles.angels.houses.application.models.HouseModel
import java.io.File
import java.util.UUID
import doobie.util.meta.Meta
import org.charles.angels.houses.domain.Contact
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.ScheduleBlock
import org.charles.angels.houses.domain.Schedule
import cats.data.Chain
import doobie.util.fragment.Fragment
import doobie.util.update.Update
import org.charles.angels.houses.application.models.ScheduleModel
import org.charles.angels.houses.errors.ServerError
import cats.effect.kernel.Resource
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import org.charles.angels.houses.db.Make
import doobie.hikari.HikariTransactor

class SqlExecutor[F[_]: Async](xa: Transactor[F])
    extends (DatabaseAction ~> F) {

  import SqlExecutor.given;

  def apply[A](action: DatabaseAction[A]): F[A] =
    interpret(action).adaptError(ServerError.DatabaseError(_))

  private def execute(fragment: Fragment) =
    fragment.update.run.transact(xa).void.attempt

  private def execute[A](fragment: ConnectionIO[A]) =
    fragment.transact(xa).void.attempt

  private def findHouse(id: UUID) =
    sql"""SELECT * FROM "houses" WHERE id = $id"""
      .query[SqlExecutor.HouseModel]
      .option
      .transact(xa)
      .nested
      .map(_.toHouse)
      .value
      .attempt

  private def findContact(ci: Int) =
    sql"""SELECT * FROM "contacts" WHERE ci = $ci"""
      .query[Contact]
      .option
      .attempt
      .transact(xa)

  private def findSchedule(id: UUID) =
    sql"""SELECT * FROM "schedules" WHERE id = $id ORDER BY "day", "key""""
      .query[SqlExecutor.ScheduleModel]
      .to[List]
      .transact(xa)
      .map { list =>
        val days = list.groupBy(_.day).map { case (i, list) =>
          (
            i,
            Chain.fromSeq(list.map(_.toScheduleBlock))
          )
        }
        if (days.size == 0) None
        else
          Schedule
            .unsafe(
              id,
              days.get(1).orEmpty,
              days.get(2).orEmpty,
              days.get(3).orEmpty,
              days.get(4).orEmpty,
              days.get(5).orEmpty
            )
            .some
      }
      .attempt

  private def interpret[A](action: DatabaseAction[A]): F[A] = action match {
    case DatabaseAction.GetAllContactCI =>
      sql"""SELECT ci FROM "contacts""""
        .query[Int]
        .to[Vector]
        .transact(xa)
        .attempt
    case DatabaseAction.DoesRifExist(rif) =>
      sql"""SELECT rif FROM  "houses" WHERE rif = $rif"""
        .query[Int]
        .option
        .transact(xa)
        .attempt
    case DatabaseAction.GetAllHouses => 
      sql"""SELECT * FROM "houses""""
        .query[SqlExecutor.HouseModel]
        .to[Vector]
        .transact(xa)
        .nested
        .map(_.toHouse)
        .value
        .attempt
    case DatabaseAction.GetHouse(id) => findHouse(id)
    case DatabaseAction.StoreHouse(
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
        ) =>
      execute(sql"""INSERT INTO "houses"(
            id, img, name,
            rif, phones, address,
            max_shares, current_shares, minimum_age,
            maximum_age, current_girls_helped, current_boys_helped,
            contact_ci, schedule_id
          ) VALUES (
            $id, ${img.getAbsolutePath}, $name, $rif,
            $phones, $address, $maxShares, $currentShares,
            $minimumAge, $maximumAge, $currentGirlsHelped,
            $currentBoysHelped, $contactCI, $scheduleId
        )""")
    case DatabaseAction.UpdateImage(id, newImage) =>
      execute(
        sql"""UPDATE "houses" SET "img" = ${newImage.getAbsolutePath} WHERE id = $id"""
      )
    case DatabaseAction.UpdateName(id, name) =>
      execute(
        sql"""UPDATE "houses" SET "name" = $name WHERE id = $id"""
      )
    case DatabaseAction.UpdateRIF(id, rif) =>
      execute(
        sql"""UPDATE "houses" SET "rif" = $rif WHERE id = $id"""
      )
    case DatabaseAction.AddPhone(id, newPhone) =>
      execute(
        sql"""UPDATE "houses" SET "phones" = array_append("phones", $newPhone::character(12)) WHERE id = $id"""
      )
    case DatabaseAction.RemovePhone(id, key) =>
      execute(
        sql"""UPDATE "houses" SET "phones" = array_remove("phones", "phones"[$key + 1]) WHERE id = $id"""
      )
    case DatabaseAction.UpdatePhone(id, key, phone) =>
      execute(
        sql"""UPDATE "houses" SET "phones" = array_replace("phones", "phones"[$key + 1], $phone::character(12)) WHERE id = $id"""
      )
    case DatabaseAction.UpdateMaxShares(id, maxShares) =>
      execute(
        sql"""UPDATE "houses" SET "max_shares" = $maxShares WHERE id = $id"""
      )
    case DatabaseAction.UpdateCurrentShares(id, currentShares) =>
      execute(
        sql"""UPDATE "houses" SET "current_shares" = $currentShares WHERE id = $id"""
      )
    case DatabaseAction.UpdateMinimumAge(id, age) =>
      execute(
        sql"""UPDATE "houses" SET "minimum_age" = $age WHERE id = $id"""
      )
    case DatabaseAction.UpdateMaximumAge(id, age) =>
      execute(
        sql"""UPDATE "houses" SET "maximum_age" = $age WHERE id = $id"""
      )
    case DatabaseAction.UpdateCurrentGirlsHelped(id, currentGirls) =>
      execute(
        sql"""UPDATE "houses" SET "current_girls_helped" = $currentGirls WHERE id = $id"""
      )
    case DatabaseAction.UpdateCurrentBoysHelped(id, currentBoys) =>
      execute(
        sql"""UPDATE "houses" SET "current_boys_helped" = $currentBoys WHERE id = $id"""
      )
    case DatabaseAction.DeleteHouse(id) =>
      (for
        result <- findHouse(id).rethrow
        house <- result match
          case Some(house) =>
            execute(sql"""DELETE WHERE id = $id FROM "houses""").rethrow
              .as(house)
          case None => Exception(f"No se encontro CASA con ID: $id").raiseError
      yield house).attempt
    case DatabaseAction.GetContact(ci) => findContact(ci)
    case DatabaseAction.StoreContact(ci, name, lastname, phone) =>
      execute(
        sql"""INSERT INTO "contacts"(ci, name, lastname, phone) VALUES ($ci, $name, $lastname, $phone)"""
      )
    case DatabaseAction.ChangeCI(ci, nCi) =>
      execute(sql"""UPDATE "contacts" SET "ci" = $nCi WHERE "ci" = $ci""")
    case DatabaseAction.ChangeName(ci, name) =>
      execute(sql"""UPDATE "contacts" SET "name" = $name WHERE "ci" = $ci""")
    case DatabaseAction.ChangeLastname(ci, lastname) =>
      execute(
        sql"""UPDATE "contacts" SET "lastname" = $lastname WHERE "ci" = $ci"""
      )
    case DatabaseAction.ChangePhone(ci, phone) =>
      execute(
        sql"""UPDATE "contacts" SET "phone" = $phone WHERE "ci" = $ci"""
      )
    case DatabaseAction.DeleteContact(ci) =>
      (for
        result <- findContact(ci).rethrow
        contact <- result match
          case Some(contact) =>
            execute(sql"""DELETE WHERE "ci" = $ci FROM "contacts""").rethrow
              .as(contact)
          case None =>
            Exception(f"No se encontro CONTACTO con CI: $ci").raiseError
      yield contact).attempt

    case DatabaseAction.GetSchedule(id) => findSchedule(id)
    case DatabaseAction.StoreSchedule(
          id,
          monday,
          tuesday,
          wednesday,
          thursday,
          friday
        ) =>
      Update[SqlExecutor.ScheduleTuple](
        """INSERT INTO "schedules"(id, day, key, start_time, duration) VALUES (?, ?, ?, ?, ?)"""
      )
        .updateMany(
          (monday.mapWithIndex((sb, i) =>
            SqlExecutor.ScheduleModel(id, 1, i, sb.starts, sb.lasts).tuple
          ) ++
            tuesday.mapWithIndex((sb, i) =>
              SqlExecutor.ScheduleModel(id, 2, i, sb.starts, sb.lasts).tuple
            ) ++
            wednesday.mapWithIndex((sb, i) =>
              SqlExecutor.ScheduleModel(id, 3, i, sb.starts, sb.lasts).tuple
            ) ++
            thursday.mapWithIndex((sb, i) =>
              SqlExecutor.ScheduleModel(id, 4, i, sb.starts, sb.lasts).tuple
            ) ++
            friday.mapWithIndex((sb, i) =>
              SqlExecutor.ScheduleModel(id, 5, i, sb.starts, sb.lasts).tuple
            ))
        )
        .transact(xa)
        .void
        .attempt
    case DatabaseAction.AddBlock(id, day, key, startTime, duration) =>
      execute(
        sql"""INSERT INTO "schedules"(id, day, key, start_time, duration) VALUES ($id, $day, $key, $startTime, $duration)"""
      )
    case DatabaseAction.RemoveBlock(id, day, key) =>
      execute(
        sql"""DELETE FROM "schedules" WHERE id = $id AND day = $day AND key = $key""".update.run >>
          sql"""UPDATE "schedules" SET key = key - 1 WHERE id = $id AND day = $day AND key > $key""".update.run
      )
    case DatabaseAction.UpdateStartHourOnBlock(id, day, key, newStartHour) =>
      val localHour = LocalTime.of(newStartHour, 0)
      execute(
        sql"""UPDATE "schedules" SET start_time = $localHour + make_interval(mins => EXTRACT(MINUTE FROM start_time)::integer) WHERE id = $id AND day = $day AND key = $key"""
      )
    case DatabaseAction.UpdateStartMinuteOnBlock(
          id,
          day,
          key,
          newStartMinute
        ) =>
      val localMinute = LocalTime.of(0, newStartMinute)
      execute(
        sql"""UPDATE "schedules" SET start_time = $localMinute + make_interval(hours => EXTRACT(HOUR FROM start_time)::integer) WHERE id = $id AND day = $day AND key = $key"""
      )
    case DatabaseAction.UpdateDurationHoursOnBlock(
          id,
          day,
          key,
          durationHour
        ) =>
      val durationHourInSeconds = durationHour * 60 * 60
      execute(
        sql"""UPDATE "schedules" SET duration = $durationHourInSeconds + MOD(duration, 60) WHERE id = $id AND day = $day AND key = $key"""
      )
    case DatabaseAction.UpdateDurationMinutesOnBlock(
          id,
          day,
          key,
          durationMinute
        ) =>
      val durationMinuteInSeconds = durationMinute * 60
      execute(
        sql"""UPDATE "schedules" SET duration = $durationMinuteInSeconds + ((duration / 3600) * 3600) WHERE id = $id AND day = $day AND key = $key"""
      )
    case DatabaseAction.DeleteSchedule(id) =>
      (for
        result <- findSchedule(id).rethrow
        schedule <- result match
          case Some(schedule) =>
            execute(sql"""DELETE FROM "schedules" WHERE id = $id""").rethrow
              .as(schedule)
          case None =>
            Exception(s"No se encontro HORARIO con ID $id").raiseError
      yield schedule).attempt
  }
}

object SqlExecutor:
  given Meta[FiniteDuration] =
    Meta[Long].imap(FiniteDuration(_, "s"))(_.toSeconds)

  type ScheduleTuple = (UUID, Int, Long, LocalTime, FiniteDuration)

  private case class ScheduleModel(
      id: UUID,
      day: Int,
      key: Int,
      startingTime: LocalTime,
      duration: FiniteDuration
  ) {
    def toScheduleBlock = ScheduleBlock.unsafe(startingTime, duration)
    def tuple: ScheduleTuple = (id, day, key, startingTime, duration)
  }

  private case class HouseModel(
      id: String,
      img: String,
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
      scheduleId: String
  ) {
    def toHouse = House.unsafe(
      UUID.fromString(id),
      File(img),
      name,
      rif,
      phones,
      address,
      contactCI,
      UUID.fromString(scheduleId),
      maxShares,
      currentShares,
      minimumAge,
      maximumAge,
      currentGirlsHelped,
      currentBoysHelped
    )
  }

final case class Sql(username: String, password: String);

given [F[_]: Async]: Make[F, DatabaseAction, Sql] with
  def make(sql: Sql) = for
    tp <- Resource.eval {
      Async[F].delay(
        ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(32))
      )
    }
    transactor <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/charles-angels",
      sql.username,
      sql.password,
      tp
    )
  yield SqlExecutor(transactor)
