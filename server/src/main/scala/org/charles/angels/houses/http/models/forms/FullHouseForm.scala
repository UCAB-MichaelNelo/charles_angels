package org.charles.angels.houses.http.models.forms

import cats.effect.implicits.*
import cats.effect.kernel.Concurrent
import cats.effect.std.Console
import cats.syntax.all.*
import cats.Parallel
import org.http4s.implicits.*
import org.http4s.*
import org.http4s.EntityDecoder
import cats.data.Chain
import org.charles.angels.houses.domain.RawScheduleBlock
import org.charles.angels.houses.application.models.ScheduleModel
import org.charles.angels.houses.application.models.ContactModel
import org.charles.angels.houses.application.models.HouseModel
import cats.Functor
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object ImageExtensionQueryParamMatcher
    extends QueryParamDecoderMatcher[String]("ext")

object RifQueryParamMatcher
    extends QueryParamDecoderMatcher[Int]("rif")

final case class FullHouseForm(
    house: HouseForm,
    contact: ContactForm,
    schedule: ScheduleForm
) { form =>
  def toHouseModel[F[_]: Functor](resolve: String => F[String]) =
    resolve(s"${form.house.rif}.${form.house.fileExtension}").map { filename =>
      HouseModel(
        filename,
        form.house.name,
        form.house.rif,
        form.house.phones,
        form.house.address,
        form.house.maxShares,
        form.house.currentShares,
        form.house.minimumAge,
        form.house.maximumAge,
        form.house.currentGirlsHelped,
        form.house.currentBoysHelped,
        ContactModel(
          form.contact.ci,
          form.contact.name,
          form.contact.lastname,
          form.contact.phone
        ),
        ScheduleModel(
          Chain.fromSeq(
            form.schedule.monday.map(r =>
              RawScheduleBlock(
                r.startHour,
                r.startMinute,
                r.durationHours,
                r.durationMinutes
              )
            )
          ),
          Chain.fromSeq(
            form.schedule.tuesday.map(r =>
              RawScheduleBlock(
                r.startHour,
                r.startMinute,
                r.durationHours,
                r.durationMinutes
              )
            )
          ),
          Chain.fromSeq(
            form.schedule.wednesday.map(r =>
              RawScheduleBlock(
                r.startHour,
                r.startMinute,
                r.durationHours,
                r.durationMinutes
              )
            )
          ),
          Chain.fromSeq(
            form.schedule.thursday.map(r =>
              RawScheduleBlock(
                r.startHour,
                r.startMinute,
                r.durationHours,
                r.durationMinutes
              )
            )
          ),
          Chain.fromSeq(
            form.schedule.friday.map(r =>
              RawScheduleBlock(
                r.startHour,
                r.startMinute,
                r.durationHours,
                r.durationMinutes
              )
            )
          )
        )
      )
    }
}
