package org.charles.angels.people.application.queries

import cats.syntax.all.*
import org.charles.angels.people.domain.Child
import org.charles.angels.people.application.Language
import org.charles.angels.people.application.errors.ApplicationError
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import java.util.UUID

enum QueryAction[A] {
  case GetChild(id: UUID) extends QueryAction[Option[Child]]
}

trait QueryLanguage[F[_]](using InjectK[QueryAction, F]) {
  def getChild(id: UUID): Language[F, Child] = EitherT(
    Free
      .liftInject(QueryAction.GetChild(id))
      .map(_.toRight(ApplicationError.ChildNotFound(id).pure))
  )
}
