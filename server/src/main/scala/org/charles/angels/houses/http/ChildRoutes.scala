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
import org.charles.angels.houses.application.ApplicationDSL as HouseApplicationDSL
import org.charles.angels.houses.errors.given
import org.charles.angels.houses.http.models.viewmodels.ChildViewModel
import org.charles.angels.houses.compiler.*
import org.http4s.StaticFile
import org.charles.angels.people.application.models.PersonalInformationModel
import org.charles.angels.houses.compiler.CompilerDSL
import io.circe.JsonObject
import org.http4s.multipart.Multipart
import cats.data.OptionT
import org.http4s.multipart
import cats.Monad
import org.http4s.EntityDecoder
import fs2.text.utf8
import io.circe.parser.decode
import org.http4s.QueryParamDecoder
import java.util.UUID
import org.http4s.ParseFailure
import org.charles.angels.people.domain.Child
import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.people.application.models.PersonalInformationOfChild

class ChildRoutes[F[_]: Async: Parallel: Concurrent: Executor]
    extends ServerRoutes[F] {

  def routes = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for
        response <- req.decodeWith(EntityDecoder.multipart[F], strict = true) { m => 
          (for
            imagePart <- OptionT.fromOption[F](m.parts.find(_.name == "image".some))
            formPart <- OptionT.fromOption[F](m.parts.find(_.name == "childForm".some))
            formString <- OptionT.liftF(formPart.body.through(utf8.decode).compile.string)
            _ <- OptionT.liftF(CompilerDSL.info(f"Obtenido formulario JSON $formString").run)
            decodeResult = decode[ChildForm](formString)
            childForm <- OptionT { 
              decodeResult match {
                case Right(result) => CompilerDSL.info(f"Obtenido el formulario: $result").run.as(result.some)
                case Left(e) => CompilerDSL.warn(f"Obtenido un error al procesar el formulario $e").run.as(None)
              }
            }
            extension = imagePart.filename.map(_.split("\\.").last).getOrElse("")
            fileWriter <- OptionT.liftF(CompilerDSL.createFile(childForm.pInfo.ci + "." + extension).run)
            imageFile <- OptionT.liftF(fileWriter(imagePart.body))
            wear = childForm.attire.toWear
            response <- OptionT.liftF {
              (wear match {
                case None => BadRequest("Provided attire is invalid")
                case Some(vwear) => 
                  for
                    child <- (for
                      wear <- ApplicationDSL.of(vwear).lift
                      child <- ApplicationDSL.create(
                        childForm.houseId,
                        childForm.pInfo,
                        childForm.mInfo,
                        childForm.fInfo,
                        childForm.npInfo,
                        childForm.rBen,
                        wear,
                        imageFile
                      ).lift
                      _ <- HouseApplicationDSL.incrementCurrentSharesOfHouse(childForm.houseId).lift >> (child match {
                        case Child.Boy(_, _, _) => HouseApplicationDSL.incrementCurrentBoysHelpedOfHouse(childForm.houseId).lift
                        case Child.Girl(_, _, _) => HouseApplicationDSL.incrementCurrentGirlsHelpedOfHouse(childForm.houseId).lift
                      })
                    yield child).run
                    finalResponse <- Ok.apply(
                      JsonObject("id" -> child.getID.asJson).asJson
                    )
                  yield finalResponse
              }).onError { case _ => CompilerDSL.deleteFile(imageFile).run }
            }
          yield response).getOrElseF(BadRequest("Unable to parse form"))
        }
      yield response
    case GET -> Root / "unique" :? CIQueryParamMatcher(ci) =>
      for
        children <- ApplicationDSL.doesChildCiExist(ci).run
        response <- children.as(Conflict()).getOrElse(Ok())
      yield response
    case GET -> Root / "personalInformation" => 
      for
        personalInformation <- ApplicationDSL.getAllExistantPersonalInformation.run
        response <- Ok(personalInformation.asJson)
      yield response
    case GET -> Root :? HouseUUIDQueryParamMatcher(houseId) =>
      for 
        children <- houseId
          .map(belonging => (belonging match {
            case HouseBelonging.BelongsToHouse(id) => ApplicationDSL.getChildrenOfHouse(id).nested.map(ChildViewModel(_, id.some)).value
            case HouseBelonging.DoesNotBelongToAnyHouse => ApplicationDSL.getChildrenWithoutHousing.nested.map(ChildViewModel(_)).value
          }).map(_.asRight[Vector[PersonalInformationOfChild]]))
          .getOrElse(ApplicationDSL.getChildrenPersonalInformation.map(_.asLeft[Vector[ChildViewModel]]))
          .run
        response <- Ok(children match {
          case Right(children) => children.asJson
          case Left(children) => children.asJson
        })
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
    case req @ PATCH -> Root / UUIDVar(id) / "house"  =>
      for
        form <- req.as[UpdateHouseOfChildForm]
        _ <- (for 
          child <- ApplicationDSL.getChild(id).lift
          houseId <- CompilerDSL.getHouseHousingChild(id)
          _ <- CompilerDSL.unbindChildFromHouse(id)
          _ <- CompilerDSL.bindChildToHouse(form.id, id)
          _ <- HouseApplicationDSL.incrementCurrentSharesOfHouse(form.id).lift >> (child match {
            case Child.Boy(_, _, _) => HouseApplicationDSL.incrementCurrentBoysHelpedOfHouse(form.id).lift
            case Child.Girl(_, _, _) => HouseApplicationDSL.incrementCurrentGirlsHelpedOfHouse(form.id).lift
          })
          _ <- houseId match {
            case Some(id) => HouseApplicationDSL.decrementCurrentSharesOfHouse(id).lift >> (child match {
                case Child.Boy(_, _, _) => HouseApplicationDSL.decrementCurrentBoysHelpedOfHouse(id).void.lift
                case Child.Girl(_, _, _) => HouseApplicationDSL.decrementCurrentGirlsHelpedOfHouse(id).void.lift
              })
            case None => ().pure[ServerLanguage]
          }
        yield ()).run
        response <- Ok("Update successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "information" =>
      for
        model <- req.as[PersonalInformationModel]
        child <- ApplicationDSL.updatePersonalInformationOfChild(id, model).run
        suffix = child.getInformation.photo.getName.split("\\.").last
        file <- CompilerDSL.moveFile(child.getInformation.photo, child.getInformation.information.ci + "." + suffix).run
        _ <- ApplicationDSL.updateChildPhoto(id, file).run
        response <- Ok("Update successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "attire" =>
      for
        model <- req.as[AttireForm]
        wearParse = model.toWear
        response <- wearParse
          .map { model => for 
              _ <- (for
                _ <- CompilerDSL.info(f"Got attire: $model")
                house <- CompilerDSL.getHouseHousingChild(id)
                childBefore <- ApplicationDSL.getChild(id).lift
                child <- ApplicationDSL.updateChildAttire(id, model).lift
                _ <- (house match {
                  case Some(houseId) => (childBefore -> child match {
                      case (Child.Boy(_, _, _), Child.Girl(_, _, _)) => 
                        HouseApplicationDSL.decrementCurrentBoysHelpedOfHouse(houseId).lift >> 
                        HouseApplicationDSL.incrementCurrentGirlsHelpedOfHouse(houseId).lift
                      case (Child.Girl(_, _, _), Child.Boy(_, _, _)) => 
                        HouseApplicationDSL.decrementCurrentGirlsHelpedOfHouse(houseId).lift >> 
                        HouseApplicationDSL.incrementCurrentBoysHelpedOfHouse(houseId).lift
                      case _ => ().pure[ServerLanguage]
                      })
                  case _ => ().pure[ServerLanguage]
                })
              yield ()).run
              response <- Ok("Update successful")
            yield response }
          .getOrElse { BadRequest() }
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "img" =>
      for
        child <- ApplicationDSL.getChild(id).run
        _ <- CompilerDSL.deleteFile(child.getInformation.photo).run
        extension = child.getInformation.photo.getName.split("\\.").last
        writer <- CompilerDSL.createFile(child.getInformation.information.ci + "." + extension).run
        file <- writer(req.body)
        _ <- ApplicationDSL.updateChildPhoto(id, file).run
        response <- Ok("Update successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "mother" :? OptionalCIQueryParamMatcher(ci) =>
      for
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsMother(id, ci)
          .run
        response <- Ok("Update successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "father" :? OptionalCIQueryParamMatcher(ci) =>
      for
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsFather(id, ci)
          .run
        response <- Ok("Update successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "representative" :? OptionalCIQueryParamMatcher(ci) =>
      for
        _ <- ApplicationDSL
          .updatePersonalInformationOfChildsNonParentRepresentative(
            id,
            ci
          )
          .run
        response <- Ok("Update successful")
      yield response
    case req @ POST -> Root / UUIDVar(id) / "related" =>
      for
        model <- req.as[RelatedBeneficiaryForm]
        _ <- ApplicationDSL.addRelatedBeneficiary(id, model.id).run
        response <- Ok("Added related beneficiary successful")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) / "related" =>
      for
        model <- req.as[RelatedBeneficiaryForm]
        _ <- ApplicationDSL.removedRelatedBeneficiary(id, model.id).run
        response <- Ok("Removed related beneficiary successful")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) =>
      for
        result <- (for 
          houseId <- OptionT { CompilerDSL.getHouseHousingChild(id) }
          child <- OptionT.liftF { ApplicationDSL.deleteChild(id).lift }
          _ <- OptionT.liftF { HouseApplicationDSL.decrementCurrentSharesOfHouse(houseId).lift }
          _ <- OptionT.liftF { child match {
            case Child.Boy(_, _, _) => HouseApplicationDSL.decrementCurrentBoysHelpedOfHouse(houseId).lift
            case Child.Girl(_, _, _) => HouseApplicationDSL.decrementCurrentGirlsHelpedOfHouse(houseId).lift
          } }
        yield ()).value.run
        _ <- result match {
          case Some(_) => CompilerDSL.info(f"Actualizadas las métricas de la CASA que albergaba al BENEFICIARIO con ID $id").run
          case None => CompilerDSL.warn(f"No se pudo actualizar las métricas de la CASA que albergaba al BENEFICIARIO con ID $id").run
        }
        response <- Ok("Child deleted")
      yield response
  }
}
