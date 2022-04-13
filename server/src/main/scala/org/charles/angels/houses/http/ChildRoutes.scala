package org.charles.angels.houses.http

import cats.implicits.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Concurrent
import org.http4s.dsl.Http4sDsl
import org.charles.angels.houses.shared.Executor
import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import org.charles.angels.houses.http.models.forms.ChildForm
import org.charles.angels.houses.http.models.forms.given
import org.charles.angels.houses.http.models.forms.*
import org.charles.angels.people.application.errors.given
import cats.Parallel
import fs2.io.file.Path as FilePath
import fs2.io.file.Files
import java.io.File
import fs2.Pure
import org.charles.angels.people.application.ApplicationDSL
import org.charles.angels.houses.errors.given
import org.charles.angels.houses.http.models.viewmodels.ChildViewModel
import org.http4s.StaticFile
import org.charles.angels.people.application.models.PersonalInformationModel
import org.charles.angels.houses.compiler.CompilerDSL
import io.circe.JsonObject

class ChildRoutes[F[_]: Async: Parallel: Concurrent: Executor]
    extends ServerRoutes[F] {
  def routes = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for
        form <- req.as[ChildForm[F]]
        _ <- CompilerDSL.info(f"Received child form $form").run
        writer <- CompilerDSL
          .createFile(
            form.pInfo.name + "-" + form.filename
          )
          .run
        file <- writer(form.photoContents)
        wear = form.attire.toWear
        response <- (wear match {
          case None => BadRequest("Attire provided is invalid")
          case Some(vwear) =>
            for
              child <- (for
                wear <- ApplicationDSL.of(vwear)
                child <- ApplicationDSL.create(
                  form.houseId,
                  form.pInfo,
                  form.mInfo,
                  form.fInfo,
                  form.npInfo,
                  form.rBen,
                  wear,
                  file
                )
              yield child).run
              response <- Ok.apply(
                JsonObject("id" -> child.getID.asJson).asJson
              )
            yield response
        }).onError { case _ => CompilerDSL.deleteFile(file).run }
      yield response
    case GET -> Root / UUIDVar(id) =>
      for
        child <- ApplicationDSL.getChild(id).run
        response <- Ok(ChildViewModel(child).asJson)
      yield response
    case req @ GET -> Root / UUIDVar(id) / "img" =>
      for
        child <- ApplicationDSL.getChild(id).run
        read <- CompilerDSL.getFileContents(child.getInformation.photo).run
        response <- Ok(read[F])
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "information" =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- ApplicationDSL.updatePersonalInformationOfChild(id, model).run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "mother" =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsMother(id, model)
          .run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "father" =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsFather(id, model)
          .run
        response <- Ok("Update succesfull")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "representative" =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsNonParentRepresentative(
            id,
            model
          )
          .run
        response <- Ok("Update succesfull")
      yield response
    case req @ POST -> Root / UUIDVar(id) / "related" =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- Executor[F].execute(
          ApplicationDSL
            .addRelatedBeneficiary(
              id,
              model
            )
        )
        response <- Ok("Added related beneficiary succesfull")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) / "related" / IntVar(ci) =>
      for
        _ <- ApplicationDSL
          .removedRelatedBeneficiary(
            id,
            ci
          )
          .run
        response <- Ok("Removed related beneficiary succesfull")
      yield response
    case req @ PUT -> Root / UUIDVar(id) / "related" / IntVar(ci) =>
      for
        model <- req.as[PersonalInformationModel]
        _ <- ApplicationDSL
          .updateRelatedBeneficiary(
            id,
            ci,
            model
          )
          .run
        response <- Ok("Updated related beneficiary succesfull")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) =>
      for
        _ <- ApplicationDSL.deleteChild(id).run
        response <- Ok("Child deleted")
      yield response
  }
}
