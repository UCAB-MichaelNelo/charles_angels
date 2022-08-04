package org.charles.angels.houses.http

import cats.implicits.*
import org.charles.angels.houses.shared.Executor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.charles.angels.houses.application.ApplicationDSL
import cats.Monad
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.charles.angels.houses.domain.House
import java.util.UUID
import fs2.io.file.Files
import fs2.io.file.Path
import cats.effect.kernel.Concurrent
import org.http4s.EntityDecoder
import org.http4s.multipart.Multipart
import org.http4s.DecodeResult
import cats.data.OptionT
import org.http4s.DecodeFailure
import org.http4s.MalformedMessageBodyFailure
import cats.Parallel
import cats.effect.kernel.Async
import org.charles.angels.houses.http.models.viewmodels.HouseViewModel
import org.charles.angels.houses.http.models.forms.FullHouseForm
import org.charles.angels.houses.http.models.forms.given
import org.charles.angels.houses.http.models.forms.*
import org.charles.angels.houses.application.models.HouseModel
import org.charles.angels.houses.application.models.ContactModel
import cats.data.Chain
import cats.data.NonEmptyChain
import org.charles.angels.houses.errors.given
import org.charles.angels.houses.application.errors.ApplicationError as HouseApplicationError
import org.charles.angels.houses.application.ApplicationAction as HouseApplicationAction
import fs2.io.file.Path as FPath
import org.charles.angels.houses.compiler.CompilerDSL
import cats.effect.std.Console
import scala.concurrent.duration.FiniteDuration

class HouseRoutes[F[_]: Async: Concurrent: Parallel: Executor]
    extends ServerRoutes[F] {

  def routes = HttpRoutes.of[F] {
    case GET -> Root / IntVar(rif) => for
      () <- ApplicationDSL.assertRifDoesNotExist(rif).run
      response <- Ok("Rif does not exist")
    yield response
    case GET -> Root :? BirthdateQueryParamMatcher(birthdateParam) => for
      houses <- birthdateParam
      .map(ApplicationDSL.getHousesThatCanAddWithBirthdate)
      .getOrElse(ApplicationDSL.getAllHouses)
      .run
      response <- Ok(houses.map(HouseViewModel(_)).asJson)
    yield response
    case GET -> Root / UUIDVar(id) =>
      for
        house <- ApplicationDSL.findHouse(id).run
        response <- Ok(HouseViewModel(house).asJson)
      yield response
    case GET -> Root / UUIDVar(id) / "image" =>
      for
        house <- ApplicationDSL.findHouse(id).run
        stream <- CompilerDSL.getFileContents(house.img).run
        response <- Ok(stream[F])
      yield response
    case req @ PUT -> Root / "image" :? ImageExtensionQueryParamMatcher(ext) +& RifQueryParamMatcher(rif) =>
      for
        write <- CompilerDSL.createFile(s"$rif.$ext").run
        _ <- write(req.body)
        response <- Ok("Image allocated successfully")
      yield response
    case req @ POST -> Root =>
      for
        form <- req.as[FullHouseForm]
        model <- form.toHouseModel(CompilerDSL.resolve).run
        _ <- ApplicationDSL.createHouse(model).run
        response <- Ok("Creation Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "image" :? ImageExtensionQueryParamMatcher(ext) =>
      for
        image <- req.body.compile.toVector
        _ <- ApplicationDSL.setImageToHouse(id, ext, image.toArray).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "name" =>
      for
        form <- req.as[UpdateHouseNameForm]
        _ <- ApplicationDSL.setNameToHouse(id, form.name).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "rif" =>
      for
        house <- ApplicationDSL.findHouse(id).run
        form <- req.as[UpdateHouseRIFForm]
        _ <- ApplicationDSL.setRIFToHouse(id, form.rif).run
        file <- CompilerDSL.moveFile(house.img, f"${form.rif}.${house.fileExtension}").run
        _ <- ApplicationDSL.setImageToHouse(id, file).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "address" =>
      for
        form <- req.as[UpdateHouseAddressForm]
        _ <- ApplicationDSL.setAddressToHouse(id, form.address).run
        response <- Ok("Update Successful")
      yield response
    case req @ POST -> Root / UUIDVar(id) / "phone" =>
      for
        form <- req.as[AddPhoneToHouseForm]
        _ <- ApplicationDSL.addPhoneToHouse(id, form.phone).run
        response <- Ok("Phone Added Successfully")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) / "phone" =>
      for
        form <- req.as[RemovePhoneFromHouseForm]
        _ <- ApplicationDSL.removePhoneOfHouse(id, form.index).run
        response <- Ok("Phone Removed Successfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "phone" =>
      for
        form <- req.as[UpdatePhoneOfHouseForm]
        _ <- ApplicationDSL.updatePhoneOfHouse(id, form.index, form.phone).run
        response <- Ok("Phone Updated Successfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "maxShares" =>
      for
        form <- req.as[UpdateMaxSharesOfHouseForm]
        _ <- ApplicationDSL.setMaxSharesOfHouse(id, form.maxShares).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "minimumAge" =>
      for
        form <- req.as[UpdateMinimumAgeOfHouseForm]
        _ <- ApplicationDSL.setMinimumAgeOfHouse(id, form.minimumAge).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "maximumAge" =>
      for
        form <- req.as[UpdateMaximumAgeOfHouseForm]
        _ <- ApplicationDSL.setMaximumAgeOfHouse(id, form.maximumAge).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "scheduleStartTime" =>
      for
        form <- req.as[UpdateScheduleStartTime]
        _ <- ApplicationDSL.setScheduleStartTimeOfHouse(id, form.scheduleStartTime).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "scheduleEndTime" =>
      for
        form <- req.as[UpdateScheduleEndTime]
        _ <- ApplicationDSL.setScheduleEndTimeOfHouse(id, form.scheduleEndTime).run
        response <- Ok("Update Successful")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "contactCI" =>
      for 
        form <- req.as[UpdateContactCIOfHouse]
        _ <- ApplicationDSL.setContactCIOfHouse(id, form.contactCI).run
        response <- Ok("Update Successful")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) =>
      for
        _ <- ApplicationDSL.deleteHouse(id).run
        response <- Ok("Deletion Successful")
      yield response
  }
}
