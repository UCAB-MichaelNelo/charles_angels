package org.charles.angels.houses.http.models.forms

import cats.data.ValidatedNec
import org.http4s.EntityDecoder
import cats.syntax.all.*
import cats.effect.kernel.Concurrent
import cats.Parallel
import cats.data.EitherT
import org.http4s.DecodeResult
import fs2.Stream
import cats.Monad
import fs2.RaiseThrowable
import java.util.UUID
import org.charles.angels.people.domain.Child
import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.people.application.models.PersonalInformationModel
import org.charles.angels.people.domain.ChildInformation
import java.io.File
import org.charles.angels.people.domain.Wear
import org.charles.angels.people.domain.BoyAttire
import org.charles.angels.people.domain.GirlAttire
import java.time.LocalDate
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.QueryParamDecoder
import org.http4s.ParseFailure
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

enum HouseBelonging {
  case BelongsToHouse(id: UUID)
  case DoesNotBelongToAnyHouse
}

given QueryParamDecoder[UUID] = QueryParamDecoder[String].emap { s => Either.catchNonFatal(UUID.fromString(s)).leftMap(e => ParseFailure("Failed decoding uuid in query parameter", e.getMessage)) }

given QueryParamDecoder[HouseBelonging] = 
  QueryParamDecoder[UUID]
    .map(HouseBelonging.BelongsToHouse.apply) <+>
  QueryParamDecoder[String]
    .emap(f => if(f == "none") f.asRight else ParseFailure("Invalid belonging", "To find childs with no house belonging use 'none'").asLeft)
    .as(HouseBelonging.DoesNotBelongToAnyHouse)

object HouseUUIDQueryParamMatcher extends OptionalQueryParamDecoderMatcher[HouseBelonging]("house")

object CIQueryParamMatcher extends QueryParamDecoderMatcher[Int]("ci")

object OptionalCIQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("ci")

final case class AttireForm(
    shortOrTrousersSize: Int,
    tshirtOrshirtSize: Int,
    sweaterSize: Option[Int],
    dressSize: Option[Int],
    footwearSize: Int
) {
  def toWear = (sweaterSize.map(ss =>
    BoyAttire(
      shortOrTrousersSize,
      tshirtOrshirtSize,
      ss,
      footwearSize
    ).map(Wear.BoyWear(_))
  ) <+> dressSize.map(ds =>
    GirlAttire(
      shortOrTrousersSize,
      tshirtOrshirtSize,
      ds,
      footwearSize
    ).map(Wear.GirlWear(_))
  ))
}

final case class ChildForm(
    houseId: UUID,
    pInfo: PersonalInformationModel,
    fInfo: Option[PersonalInformationModel],
    mInfo: Option[PersonalInformationModel],
    npInfo: Option[PersonalInformationModel],
    rBen: Vector[UUID],
    attire: AttireForm
)