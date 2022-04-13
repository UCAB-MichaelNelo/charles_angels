package org.charles.angels.houses.http.models.forms

import cats.effect.implicits.*
import cats.effect.kernel.Concurrent
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

final case class FullHouseForm[F[_]](
    house: HouseForm[F],
    contact: ContactForm,
    schedule: ScheduleForm
)

given [F[_]: Concurrent: Parallel]: EntityDecoder[F, FullHouseForm[F]] with
  private val houseDecoder: EntityDecoder[F, HouseForm[F]] = implicitly
  private val contactDecoder: EntityDecoder[F, ContactForm] = implicitly
  private val scheduleDecoder: EntityDecoder[F, ScheduleForm] = implicitly

  def consumes =
    houseDecoder.consumes ++ contactDecoder.consumes ++ scheduleDecoder.consumes
  def decode(media: Media[F], strict: Boolean) =
    (
      houseDecoder.decode(media, strict),
      contactDecoder.decode(media, strict),
      scheduleDecoder.decode(media, strict)
    ).parMapN(FullHouseForm.apply)

extension [F[_]: Functor](form: FullHouseForm[F])
  def toHouseModel(consume: fs2.Stream[F, Byte] => F[Array[Byte]]) =
    consume(form.house.fs).map { fileContents =>
      HouseModel(
        fileContents.toArray,
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
