package org.charles.angels.houses.http.models.forms

import cats.implicits.*
import org.http4s.QueryParamDecoder
import org.charles.angels.houses.reports.data.ReportScope
import org.http4s.ParseFailure
import org.http4s.dsl.impl.QueryParamDecoderMatcher

private given QueryParamDecoder[ReportScope] = QueryParamDecoder[String].emap { st => st match {
    case "general" => ReportScope.General.asRight[ParseFailure]
    case "houses" => ReportScope.Houses.asRight[ParseFailure]
    case _ => ParseFailure("Failure when parsing report scope", "Parameter must be 'general' or 'houses'").asLeft[ReportScope]
} }

object ReportScopeQueryParameter extends QueryParamDecoderMatcher[ReportScope]("scope")