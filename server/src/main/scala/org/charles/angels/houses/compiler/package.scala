package org.charles.angels.houses

import cats.data.EitherT
import cats.free.Free
import cats.data.EitherK
import cats.data.NonEmptyChain
import cats.arrow.FunctionK
import cats.InjectK
import org.charles.angels.houses.application.ApplicationAction as HousesApplicationAction
import org.charles.angels.people.application.ApplicationAction as PeopleApplicationAction
import org.charles.angels.houses.application.errors.ApplicationError as HousesApplicationError
import org.charles.angels.people.application.errors.ApplicationError as PeopleApplicationError
import org.charles.angels.houses.logging.LoggingAction
import org.charles.angels.houses.db.DatabaseAction
import org.charles.angels.houses.filesystem.FilesystemAction
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.cron.CronAction
import org.charles.angels.houses.notifications.NotificationAction
import org.charles.angels.houses.reports.ReportAction
import org.charles.angels.houses.reports.data.DataReportAction
import org.charles.angels.houses.reports.template.TemplateReportAction
import org.charles.angels.houses.auth.AuthAction

package object compiler:
  type ApplicationAction[A] =
    EitherK[HousesApplicationAction, PeopleApplicationAction, A]
  type ApplicationLanguage[A] =
    EitherT[Free[ApplicationAction, _], ServerError, A]

  private type ServerAction0[A] = EitherK[LoggingAction, DatabaseAction, A]
  private type ServerAction1[A] = EitherK[FilesystemAction, ServerAction0, A]
  private type ServerAction2[A] = EitherK[AuthAction, ServerAction1, A]
  private type ServerAction3[A] = EitherK[NotificationAction, ServerAction2, A]
  private type ServerAction4[A] = EitherK[ReportAction, ServerAction3, A]
  type ServerAction[A] = EitherK[CronAction, ServerAction4, A]
  type CompilerLanguage[F[_], A] = EitherT[Free[F, _], Throwable, A]
  type ServerLanguage[A] = CompilerLanguage[ServerAction, A]

  given InjectK[DataReportAction, ServerAction] with
    def inj: FunctionK[DataReportAction, ServerAction] = new FunctionK {
      def apply[A](fa: DataReportAction[A]): ServerAction[A] = EitherK.right[CronAction](
        EitherK.left[ServerAction3](
          EitherK.left[TemplateReportAction](fa)
        )
      )
    }
    def prj: FunctionK[ServerAction, [A] =>> scala.Option[DataReportAction[A]]] = new FunctionK {
      def apply[A](fa: ServerAction[A]): Option[DataReportAction[A]] = fa.run match {
        case Right(lang) => lang.run match {
          case Left(lang) => lang.run match {
            case Left(ra) => Some(ra)
            case _ => None
          }
          case _ => None
        }
        case _ => None
      }
    }
  
  given InjectK[TemplateReportAction, ServerAction] with
    def inj: FunctionK[TemplateReportAction, ServerAction] = new FunctionK {
      def apply[A](fa: TemplateReportAction[A]): ServerAction[A] = EitherK.right[CronAction](
        EitherK.left[ServerAction3](
          EitherK.right[DataReportAction](fa)
        )
      )
    }
    def prj: FunctionK[ServerAction, [A] =>> scala.Option[TemplateReportAction[A]]] = new FunctionK {
      def apply[A](fa: ServerAction[A]): Option[TemplateReportAction[A]] = fa.run match {
        case Right(lang) => lang.run match {
          case Left(lang) => lang.run match {
            case Right(ra) => Some(ra)
            case _ => None
          }
          case _ => None
        }
        case _ => None
      }
    }