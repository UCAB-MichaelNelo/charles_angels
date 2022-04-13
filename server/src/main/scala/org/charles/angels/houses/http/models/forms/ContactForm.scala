package org.charles.angels.houses.http.models.forms

import org.http4s.EntityDecoder
import cats.syntax.all.*
import cats.data.OptionT
import cats.effect.kernel.Concurrent
import cats.Parallel
import org.http4s.DecodeResult

final case class ContactForm(
    ci: Int,
    name: String,
    lastname: String,
    phone: Option[String]
)

given [F[_]: Concurrent: Parallel]: EntityDecoder[F, ContactForm] =
  EntityDecoder.multipart
    .map { m =>
      (
        m.parts.field[Int]("ci"),
        m.parts.field[String]("contact_name"),
        m.parts.field[String]("contact_lastname"),
        m.parts.field[Option[String]]("contact_phone")
      ).parMapN(ContactForm.apply)
    }
    .flatMapR(result => DecodeResult(result.value))
