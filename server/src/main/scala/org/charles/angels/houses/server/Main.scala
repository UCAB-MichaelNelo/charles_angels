package org.charles.angels.houses.server

import cats.~>
import fs2.Stream
import cats.implicits.*
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.std.Console
import org.charles.angels.houses.db.DatabaseExecutor
import org.charles.angels.houses.logging.LoggingExecutor
import org.charles.angels.houses.logging.`scala-logging`.ScalaLogging
import org.charles.angels.houses.logging.`scala-logging`.given
import org.charles.angels.houses.filesystem.FilesystemExecutor
import org.charles.angels.houses.filesystem.jvm.JVM
import org.charles.angels.houses.filesystem.jvm.given
import org.charles.angels.houses.cron.CronExecutor
import org.charles.angels.houses.cron.ce.CatsEffectScheduler
import org.charles.angels.houses.cron.ce.given
import org.charles.angels.houses.notifications.NotificationExecutor
import org.charles.angels.houses.notifications.fs2.Fs2
import org.charles.angels.houses.notifications.fs2.given
import org.charles.angels.houses.shared.Executor
import org.charles.angels.houses.compiler.ApplicationLanguage
import org.charles.angels.houses.compiler.Compiler
import org.charles.angels.houses.compiler.ServerLanguage
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.http.HttpServer
import org.charles.angels.houses.reports.ReportExecutor
import org.charles.angels.houses.reports.ReportAction
import org.charles.angels.houses.reports.data.sql.ExistingSqlTransactor
import org.charles.angels.houses.reports.data.sql.given
import org.charles.angels.houses.reports.template.scalatags.Scalatags
import org.charles.angels.houses.reports.template.scalatags.given
import cats.effect.kernel.Resource
import org.charles.angels.houses.auth.AuthExecutor
import org.charles.angels.houses.auth.environment.Environment
import org.charles.angels.houses.auth.environment.given
import org.charles.angels.houses.server.config.PureConfigLoader

object Main extends IOApp {
  private def executor: Resource[IO, (Executor[IO], Int)] = for
    config <- Resource.eval { PureConfigLoader.load[IO] }
    (dbInterpreter, xa) <-
      DatabaseExecutor.hikariSqlExecutor[IO](
        config.database.driver,
        config.database.url,
        config.database.user,
        config.database.password,
        config.database.parallelismLevel
      )
    logInterpreter <- LoggingExecutor[IO](
      ScalaLogging(getClass().getPackage().toString)
    )
    fsInterpreter <- FilesystemExecutor[IO](JVM(config.fs.baseDir))
    (notificationStream, notificationInterpreter) <- NotificationExecutor[IO](Fs2())
    reportInterpreter <- ReportExecutor[IO](ExistingSqlTransactor(xa), Scalatags(config.report.wkhtmltopdfPath, config.report.resourcesBasePath))
    authIntepreter <- AuthExecutor[IO](Environment(config.authentication.user, config.authentication.password, config.authentication.rawKey))
    unscheduledInterpreter =
      reportInterpreter or (notificationInterpreter or (authIntepreter or (fsInterpreter or (logInterpreter or (dbInterpreter)))))
    cronMaker = CatsEffectScheduler[IO](unscheduledInterpreter)
    cronInterpreter <- CronExecutor(cronMaker)
    finalInterpreter = cronInterpreter or unscheduledInterpreter
  yield new Executor[IO] {
    def compiler = Compiler
    def interpreter = finalInterpreter
    def stream = notificationStream
  } -> config.http.port

  def run(args: List[String]): IO[ExitCode] = (for
    (executor, port) <- Stream.resource(executor)
    given Executor[IO] = executor
    server <- Stream.resource(HttpServer[IO].server(port))
  yield server).compile.drain.as(ExitCode.Success)
}
