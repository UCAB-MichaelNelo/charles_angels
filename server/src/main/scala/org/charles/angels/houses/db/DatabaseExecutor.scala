package org.charles.angels.houses.db

import cats.~>
import cats.implicits.*
import cats.effect.kernel.Resource
import cats.effect.kernel.Async
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import doobie.hikari.HikariTransactor
import org.charles.angels.houses.db.sql.{CommonSql, given}
import org.charles.angels.houses.db.houses.DatabaseAction as HousesDatabaseAction
import org.charles.angels.houses.db.people.DatabaseAction as PeopleDatabaseAction
import org.charles.angels.houses.db.relationships.DatabaseAction as RelationshipsDatabaseAction
import doobie.util.transactor.Transactor

trait Make[F[_], G[_], A]:
  def make(in: A): Resource[F, G ~> F]

object DatabaseExecutor {
  final case class DatabaseExecutorMakePartiallyApplied[F[_], G[_]]() {
    def apply[A](input: A)(using M: Make[F, G, A]) = M.make(input)
  }
  def apply[F[_], G[_]] = DatabaseExecutorMakePartiallyApplied[F, G]()

  def hikariSqlExecutor[F[_]: Async](
    databaseDriver: String,
    databaseUrl: String, 
    databaseUser: String, 
    databasePassword: String, 
    parallelismLevel: Int
  ): Resource[F, (DatabaseAction ~> F, Transactor[F])] = for
    tp <- Resource.eval {
      Async[F].delay(
        ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(8))
      )
    }
    xa <- 
      HikariTransactor.newHikariTransactor[F](
        databaseDriver, // "org.postgresql.Driver",
        databaseUrl, // "jdbc:postgresql://localhost:5432/charles-angels",
        databaseUser,
        databasePassword,
        tp   
      )
    commonSql = CommonSql(xa)
    finalExecutor <- (
      apply[F, HousesDatabaseAction](commonSql),
      apply[F, PeopleDatabaseAction](commonSql),
      apply[F, RelationshipsDatabaseAction](commonSql)
    ).parMapN((housesE, peopleE, relE) => relE or (housesE or peopleE))
  yield (finalExecutor, xa)
}
