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

final case class ChildForm[F[_]](
    photoContents: fs2.Stream[F, Byte],
    filename: String,
    sex: String,
    houseId: UUID,
    pInfo: PersonalInformationModel,
    fInfo: Option[PersonalInformationModel],
    mInfo: Option[PersonalInformationModel],
    npInfo: Option[PersonalInformationModel],
    rBen: List[PersonalInformationModel],
    attire: AttireForm
)

given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, AttireForm] = (
  PartField.field[F, Int]("shortsOrTrousersSize"),
  PartField.field[F, Int]("tShirtOrShirtSize"),
  PartField.field[F, Option[Int]]("sweaterSize"),
  PartField.field[F, Option[Int]]("dressSize"),
  PartField.field[F, Int]("footwearSize")
).mapN(AttireForm.apply)

given [F[_]: Monad: RaiseThrowable: Concurrent]
    : PartField[F, PersonalInformationModel] = (
  PartField.field[F, Int]("ci"),
  PartField.field[F, String]("name"),
  PartField.field[F, String]("lastname"),
  PartField.field[F, LocalDate]("birthdate")
).mapN(PersonalInformationModel.apply)

given [F[_]: Concurrent: Parallel]: EntityDecoder[F, ChildForm[F]] =
  EntityDecoder.multipart
    .map { m =>
      def rbV = (
        m.parts.field[Vector[Int]](f"relatedBeneficiaries[][ci]"),
        m.parts.field[Vector[String]](f"relatedBeneficiaries[][name]"),
        m.parts
          .field[Vector[String]](f"relatedBeneficiaries[][lastname]"),
        m.parts
          .field[Vector[LocalDate]](f"relatedBeneficiaries[][birthdate]")
      ).mapN((ci, name, lastname, birthdate) =>
        (ci, name, lastname, birthdate).parMapN(PersonalInformationModel.apply)
      )

      (
        m.parts.field[FilePart[F]]("image"),
        m.parts.field[String]("sex"),
        m.parts.field[UUID]("houseId"),
        m.parts.field[PersonalInformationModel]("personalInformation"),
        m.parts.field[Option[PersonalInformationModel]]("fatherInformation"),
        m.parts.field[Option[PersonalInformationModel]]("motherInformation"),
        m.parts.field[Option[PersonalInformationModel]]("nonParentInformation"),
        rbV,
        m.parts.field[AttireForm]("attire")
      ).parMapN { (file, sex, hId, pInf, fInf, mInf, npInf, relBen, attire) =>
        ChildForm(
          file.stream,
          file.name,
          sex,
          hId,
          pInf,
          fInf,
          mInf,
          npInf,
          relBen.toList,
          attire
        )
      }
    }
    .flatMapR(result => DecodeResult(result.value))
