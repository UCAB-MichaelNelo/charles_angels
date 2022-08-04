package org.charles.angels.houses.db.sql

import cats.implicits.*
import cats.effect.kernel.Async
import org.charles.angels.houses.db.Make
import org.charles.angels.houses.db.houses.DatabaseAction as HousesDatabaseAction
import org.charles.angels.houses.db.people.DatabaseAction as PeopleDatabaseAction
import org.charles.angels.houses.db.relationships.DatabaseAction as RelationshipsDatabaseAction
import cats.arrow.FunctionK
import cats.effect.kernel.Resource
import org.charles.angels.houses.db.houses.sql.SqlExecutor as HouseSqlExecutor
import org.charles.angels.houses.db.people.sql.SqlExecutor as PeopleSqlExecutor
import org.charles.angels.houses.db.relationships.sql.SqlExecutor as RelationshipsSqlExecutor
import cats.effect.kernel.Ref
import doobie.util.transactor.Transactor
import doobie.hikari.HikariTransactor.apply
import cats.effect.kernel.Sync
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContextExecutorService
import doobie.hikari.HikariTransactor

case class CommonSql[F[_]: Sync](transactor: Transactor[F])

given [F[_]: Async]: Make[F, HousesDatabaseAction, CommonSql[F]] with
    def make(in: CommonSql[F]): Resource[F, FunctionK[HousesDatabaseAction, F]] = 
        Resource.pure{ HouseSqlExecutor(in.transactor) }
    
given [F[_]: Async]: Make[F, PeopleDatabaseAction, CommonSql[F]] with
    def make(in: CommonSql[F]): Resource[F, FunctionK[PeopleDatabaseAction, F]] = 
        Resource.pure{ PeopleSqlExecutor(in.transactor) }

given [F[_]: Async]: Make[F, RelationshipsDatabaseAction, CommonSql[F]] with
    def make(in: CommonSql[F]): Resource[F, FunctionK[RelationshipsDatabaseAction, F]] = 
        Resource.pure{ RelationshipsSqlExecutor(in.transactor) }