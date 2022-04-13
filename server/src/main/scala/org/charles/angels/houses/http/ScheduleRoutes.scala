package org.charles.angels.houses.http

import cats.syntax.all.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import cats.Parallel
import cats.data.NonEmptyChain
import cats.effect.kernel.Concurrent
import org.charles.angels.houses.shared.Executor
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.charles.angels.houses.application.ApplicationDSL
import org.charles.angels.houses.errors.given
import org.charles.angels.houses.http.models.viewmodels.ScheduleViewModel
import org.charles.angels.houses.http.models.viewmodels.given
import org.charles.angels.houses.http.models.forms.given
import org.charles.angels.houses.http.models.forms.Day
import org.charles.angels.houses.http.models.forms.AddBlockToScheduleForm
import org.charles.angels.houses.domain.RawScheduleBlock
import org.charles.angels.houses.http.models.forms.RemoveBlockFromScheduleForm
import org.charles.angels.houses.http.models.forms.UpdateStartHourOnScheduleBlockForm
import org.charles.angels.houses.http.models.forms.UpdateStartMinuteOnScheduleBlockForm
import org.charles.angels.houses.http.models.forms.UpdateDurationHourOnScheduleBlockForm
import org.charles.angels.houses.http.models.forms.UpdateDurationMinuteOnScheduleBlockForm
import org.charles.angels.houses.application.errors.ApplicationError as HouseApplicationError
import org.charles.angels.houses.application.ApplicationAction as HouseApplicationAction
import org.charles.angels.houses.domain.errors.HouseError

class ScheduleRoutes[F[_]: Async: Concurrent: Parallel: Executor]
    extends ServerRoutes[F] {

  def routes = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      for
        schedule <- ApplicationDSL.findSchedule(id).run
        response <- Ok(ScheduleViewModel(schedule).asJson)
      yield response
    case req @ POST -> Root / UUIDVar(id) =>
      for
        form <- req.as[AddBlockToScheduleForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.addBlockOnMonday(
              id,
              RawScheduleBlock(
                form.startHour,
                form.startMinute,
                form.durationHours,
                form.durationMinutes
              )
            )
          case Day.Tuesday =>
            ApplicationDSL.addBlockOnTuesday(
              id,
              RawScheduleBlock(
                form.startHour,
                form.startMinute,
                form.durationHours,
                form.durationMinutes
              )
            )
          case Day.Wednesday =>
            ApplicationDSL.addBlockOnWednesday(
              id,
              RawScheduleBlock(
                form.startHour,
                form.startMinute,
                form.durationHours,
                form.durationMinutes
              )
            )
          case Day.Thursday =>
            ApplicationDSL.addBlockOnThursday(
              id,
              RawScheduleBlock(
                form.startHour,
                form.startMinute,
                form.durationHours,
                form.durationMinutes
              )
            )
          case Day.Friday =>
            ApplicationDSL.addBlockOnFriday(
              id,
              RawScheduleBlock(
                form.startHour,
                form.startMinute,
                form.durationHours,
                form.durationMinutes
              )
            )
        }).run
        response <- Ok("Block added succesfully")
      yield response
    case req @ DELETE -> Root / UUIDVar(id) =>
      for
        form <- req.as[RemoveBlockFromScheduleForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.removeBlockOnMonday(
              id,
              form.block
            )
          case Day.Tuesday =>
            ApplicationDSL.removeBlockOnTuesday(
              id,
              form.block
            )
          case Day.Wednesday =>
            ApplicationDSL.removeBlockOnWednesday(
              id,
              form.block
            )
          case Day.Thursday =>
            ApplicationDSL.removeBlockOnThursday(
              id,
              form.block
            )
          case Day.Friday =>
            ApplicationDSL.removeBlockOnFriday(
              id,
              form.block
            )
        }).run
        response <- Ok("Block removed succesfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "startHour" =>
      for
        form <- req.as[UpdateStartHourOnScheduleBlockForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.setStartingHourOnMonday(
              id,
              form.key,
              form.startHour
            )
          case Day.Tuesday =>
            ApplicationDSL.setStartingHourOnTuesday(
              id,
              form.key,
              form.startHour
            )
          case Day.Wednesday =>
            ApplicationDSL.setStartingHourOnWednesday(
              id,
              form.key,
              form.startHour
            )
          case Day.Thursday =>
            ApplicationDSL.setStartingHourOnThursday(
              id,
              form.key,
              form.startHour
            )
          case Day.Friday =>
            ApplicationDSL.setStartingHourOnFriday(
              id,
              form.key,
              form.startHour
            )
        }).run
        response <- Ok("Block updated succesfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "startMinute" =>
      for
        form <- req.as[UpdateStartMinuteOnScheduleBlockForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.setStartingMinuteOnMonday(
              id,
              form.key,
              form.startMinute
            )
          case Day.Tuesday =>
            ApplicationDSL.setStartingMinuteOnTuesday(
              id,
              form.key,
              form.startMinute
            )
          case Day.Wednesday =>
            ApplicationDSL.setStartingMinuteOnWednesday(
              id,
              form.key,
              form.startMinute
            )
          case Day.Thursday =>
            ApplicationDSL.setStartingMinuteOnThursday(
              id,
              form.key,
              form.startMinute
            )
          case Day.Friday =>
            ApplicationDSL.setStartingMinuteOnFriday(
              id,
              form.key,
              form.startMinute
            )
        }).run
        response <- Ok("Block updated succesfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "durationHours" =>
      for
        form <- req.as[UpdateDurationHourOnScheduleBlockForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.setDurationHoursOnMonday(
              id,
              form.key,
              form.durationHour
            )
          case Day.Tuesday =>
            ApplicationDSL.setDurationHoursOnTuesday(
              id,
              form.key,
              form.durationHour
            )
          case Day.Wednesday =>
            ApplicationDSL.setDurationHoursOnWednesday(
              id,
              form.key,
              form.durationHour
            )
          case Day.Thursday =>
            ApplicationDSL.setDurationHoursOnThursday(
              id,
              form.key,
              form.durationHour
            )
          case Day.Friday =>
            ApplicationDSL.setDurationHoursOnFriday(
              id,
              form.key,
              form.durationHour
            )
        }).run
        response <- Ok("Block updated succesfully")
      yield response
    case req @ PATCH -> Root / UUIDVar(id) / "durationMinutes" =>
      for
        form <- req.as[UpdateDurationMinuteOnScheduleBlockForm]
        _ <- (form.day match {
          case Day.Monday =>
            ApplicationDSL.setDurationMinuteOnMonday(
              id,
              form.key,
              form.durationMinute
            )
          case Day.Tuesday =>
            ApplicationDSL.setDurationMinuteOnTuesday(
              id,
              form.key,
              form.durationMinute
            )
          case Day.Wednesday =>
            ApplicationDSL.setDurationMinuteOnWednesday(
              id,
              form.key,
              form.durationMinute
            )
          case Day.Thursday =>
            ApplicationDSL.setDurationMinuteOnThursday(
              id,
              form.key,
              form.durationMinute
            )
          case Day.Friday =>
            ApplicationDSL.setDurationMinuteOnFriday(
              id,
              form.key,
              form.durationMinute
            )
        }).run
        response <- Ok("Block updated succesfully")
      yield response
    case DELETE -> Root / UUIDVar(id) =>
      for
        _ <- ApplicationDSL.deleteSchedule(id).run
        response <- Ok("Schedule deleted succesfully")
      yield response
  }
}
