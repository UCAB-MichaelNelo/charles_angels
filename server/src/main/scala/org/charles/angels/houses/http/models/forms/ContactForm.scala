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
