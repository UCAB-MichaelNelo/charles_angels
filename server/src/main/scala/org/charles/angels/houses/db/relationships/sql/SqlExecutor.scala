package org.charles.angels.houses.db.relationships.sql

import cats.~>
import cats.syntax.all.*
import cats.effect.implicits.*
import doobie.implicits.*
import doobie.syntax.all.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import org.charles.angels.houses.db.relationships.DatabaseAction
import java.util.UUID
import org.charles.angels.houses.db.Make
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

class SqlExecutor[F[_]: Async](xa: Transactor[F])
    extends (DatabaseAction ~> F) {
  def apply[A](action: DatabaseAction[A]) = action match {
    case DatabaseAction.BindChildToHouse(hId, cId) =>
      sql"""INSERT INTO "children_houses" (house_id, child_id) VALUES ($hId, $cId)""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.BindPersonToHouse(hId, ci) =>
      sql"""INSERT INTO "people_houses" (house_id, person_ci) VALUES ($hId, $ci)""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.GetChildrenIdsOfHouse(hId) =>
      sql"""SELECT child_id FROM "children_houses" WHERE house_id = $hId"""
        .query[UUID]
        .to[List]
        .transact(xa)
        .attempt
    case DatabaseAction.GetHouseIdHousingChild(cId) =>
      sql"""SELECT house_id FROM "children_houses" WHERE child_id = $cId"""
        .query[UUID]
        .option
        .transact(xa)
        .attempt
    case DatabaseAction.GetHouseIdHousingPerson(ci) =>
      sql"""SELECT house_id FROM "people_houses" WHERE person_ci = $ci"""
        .query[UUID]
        .option
        .transact(xa)
        .attempt
    case DatabaseAction.UnbindChildFromHouse(cId) =>
      sql"""DELETE FROM "children_houses" WHERE child_id = $cId""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.UnbindPersonFromHouse(ci) =>
      sql"""DELETE FROM "people_houses" WHERE person_ci = $ci""".update.run.void
        .transact(xa)
        .attempt
  }
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
