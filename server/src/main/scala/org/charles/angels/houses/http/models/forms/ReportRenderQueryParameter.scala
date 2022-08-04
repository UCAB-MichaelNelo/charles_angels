package org.charles.angels.houses.http.models.forms

import cats.implicits.*
import org.http4s.QueryParamDecoder
import org.charles.angels.houses.reports.data.ReportScope
import org.http4s.ParseFailure
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object ReportRenderQueryParameter extends OptionalQueryParamDecoderMatcher[Boolean]("render")