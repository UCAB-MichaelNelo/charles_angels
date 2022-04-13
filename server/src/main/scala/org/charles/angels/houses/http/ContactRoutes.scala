package org.charles.angels.houses.http

import cats.syntax.all.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import cats.Parallel
import cats.effect.kernel.Concurrent
import org.charles.angels.houses.shared.Executor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.charles.angels.houses.application.ApplicationDSL
import org.charles.angels.houses.http.models.viewmodels.ContactViewModel
import org.charles.angels.houses.http.models.forms.UpdateNameOfContactForm
import org.charles.angels.houses.http.models.forms.UpdateLastnameOfContactForm
import org.charles.angels.houses.http.models.forms.UpdatePhoneOfContactForm
import org.charles.angels.houses.http.models.forms.UpdateCIOfContactForm
import cats.data.NonEmptyChain
import org.charles.angels.houses.errors.given
import org.charles.angels.houses.application.errors.ApplicationError as HouseApplicationError
import org.charles.angels.houses.application.ApplicationAction as HouseApplicationAction

class ContactRoutes[F[_]: Async: Concurrent: Parallel: Executor]
    extends ServerRoutes[F] {

  def routes = HttpRoutes.of[F] {
    case GET -> Root / IntVar(ci) =>
      for
        contact <- ApplicationDSL.findContact(ci).run
        response <- Ok(ContactViewModel(contact).asJson)
      yield response
    case req @ PATCH -> Root / IntVar(ci) / "ci" =>
      for
        form <- req.as[UpdateCIOfContactForm]
        _ <- ApplicationDSL.setCIOfContact(ci, form.ci).run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / IntVar(ci) / "name" =>
      for
        form <- req.as[UpdateNameOfContactForm]
        _ <- ApplicationDSL.setNameOfContact(ci, form.name).run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / IntVar(ci) / "lastname" =>
      for
        form <- req.as[UpdateLastnameOfContactForm]
        _ <- ApplicationDSL.setLastnameOfContact(ci, form.lastname).run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / IntVar(ci) / "phone" =>
      for
        form <- req.as[UpdatePhoneOfContactForm]
        _ <- ApplicationDSL.setPhoneOfContact(ci, form.phone).run
        response <- Ok("Update succesfull")
      yield response
    case DELETE -> Root / IntVar(ci) =>
      for
        contact <- ApplicationDSL.deleteContact(ci).run
        response <- Ok("Deletion succesfull")
      yield response
  }
}
