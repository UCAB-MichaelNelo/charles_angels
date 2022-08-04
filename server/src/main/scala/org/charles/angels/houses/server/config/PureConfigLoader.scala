package org.charles.angels.houses.server.config

import cats.implicits.*
import cats.effect.kernel.Sync
import pureconfig.ConfigSource
import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class PureAppConfig(app: AppConfig) derives ConfigReader

object PureConfigLoader {
  def load[F[_]: Sync] = Sync[F].fromEither {
    ConfigSource.default.load[PureAppConfig].leftMap(err => Exception(err.toString))
  }.map(_.app)
}
