package org.charles.angels.houses.server

import cats.~>
import fs2.Stream
import cats.implicits.*
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.std.Console
import org.charles.angels.houses.db.DatabaseExecutor
import org.charles.angels.houses.db.houses.DatabaseAction as HousesDatabaseAction
import org.charles.angels.houses.db.houses.sql.Sql as HousesSql
import org.charles.angels.houses.db.houses.sql.given
import org.charles.angels.houses.db.people.DatabaseAction as PeopleDatabaseAction
import org.charles.angels.houses.db.people.sql.Sql as PeopleSql
import org.charles.angels.houses.db.people.sql.given
import org.charles.angels.houses.db.relationships.DatabaseAction as RelationshipsDatabaseAction
import org.charles.angels.houses.db.relationships.sql.Sql as RelationshipsSql
import org.charles.angels.houses.db.relationships.sql.given
import org.charles.angels.houses.logging.LoggingExecutor
import org.charles.angels.houses.logging.`scala-logging`.ScalaLogging
import org.charles.angels.houses.logging.`scala-logging`.given
import org.charles.angels.houses.filesystem.FilesystemExecutor
import org.charles.angels.houses.filesystem.jvm.JVM
import org.charles.angels.houses.filesystem.jvm.given
import org.charles.angels.houses.cron.CronExecutor
import org.charles.angels.houses.cron.`cats-effect`.CatsEffectScheduler
import org.charles.angels.houses.cron.`cats-effect`.given
import org.charles.angels.houses.notifications.NotificationExecutor
import org.charles.angels.houses.notifications.fs2.Fs2
import org.charles.angels.houses.notifications.fs2.given
import org.charles.angels.houses.shared.Executor
import org.charles.angels.houses.compiler.ApplicationLanguage
import org.charles.angels.houses.compiler.Compiler
import org.charles.angels.houses.compiler.ServerLanguage
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.http.HttpServer
import cats.effect.kernel.Resource

object Main extends IOApp {
  private def executor = for
    dbInterpreter <-
      (
        DatabaseExecutor[IO, HousesDatabaseAction](
          HousesSql("charles-angels-admin", "charles-angels-admin-pw")
        ),
        DatabaseExecutor[IO, PeopleDatabaseAction](
          PeopleSql("charles-angels-admin", "charles-angels-admin-pw")
        ),
        DatabaseExecutor[IO, RelationshipsDatabaseAction](
          RelationshipsSql("charles-angels-admin", "charles-angels-admin-pw")
        )
      ).parMapN((humanExecutor, peopleExecutor, relationshipsExecutor) =>
        relationshipsExecutor or (humanExecutor or peopleExecutor)
      )
    logInterpreter <- LoggingExecutor[IO](
      ScalaLogging(getClass().getPackage().toString)
    )
    fsInterpreter <- FilesystemExecutor[IO](JVM("storage"))
    (notificationStream, notificationInterpreter) <- NotificationExecutor[IO](
      Fs2()
    )
    unscheduledInterpreter =
      notificationInterpreter or (fsInterpreter or (logInterpreter or (dbInterpreter)))
    cronInterpreter <- CronExecutor(
      CatsEffectScheduler[IO](unscheduledInterpreter)
    )
    finalInterpreter = cronInterpreter or unscheduledInterpreter
  yield new Executor[IO] {
    def compiler = Compiler
    def interpreter = finalInterpreter
    def stream = notificationStream
  }

  def run(args: List[String]): IO[ExitCode] = (for
    given Executor[IO] <- Stream.resource(executor)
    server <- Stream.resource(HttpServer[IO].server)
  yield server).compile.drain.as(ExitCode.Success)
}
