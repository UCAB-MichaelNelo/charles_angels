package org.charles.angels.houses.server.config

import ciris.ConfigValue
import cats.implicits.*
import ciris.*
import cats.effect.kernel.Async
import java.nio.file.Path

object CirisLoader {
    def load[F[_]: Async]: F[AppConfig] = (
        env("DATABASE_DRIVER").or(prop("database.driver")).as[String],
        env("DATABASE_URL").or(prop("database.url")).as[String],
        env("DATABASE_USER").or(prop("database.user")).as[String],
        env("DATABASE_PASSWORD").or(prop("database.password")).as[String],
        env("DATABASE_PARALLELISM_LEVEL").or(prop("database.parallelism-level")).as[Int],

        prop("fs.base-dir").as[String],
        prop("report.wkhtmltopdf-path").as[String],

        env("PORT").or(prop("http.port")).as[Int],

        env("AUTH_USERNAME").or(prop("auth.username")).as[String],
        env("AUTH_PASSWORD").or(prop("auth.password")).as[String],
        env("AUTH_SECRET_KEY").or(prop("auth.secret-key")).as[String]
    ).parMapN((dbDriver, dbUrl, dbUser, dbPassword, dbPl, storageBaseDir, wkhtmltopdfPath, port, authUser, authPassword, authSecret) => 
        AppConfig(
            DatabaseConfig(
                dbDriver,
                dbUrl,
                dbUser,
                dbPassword,
                dbPl
            ),
            FsConfig(storageBaseDir),
            ReportConfig(wkhtmltopdfPath),
            HttpConfig(port),
            AuthenticationConfig(authUser, authPassword, authSecret)
        )
    ).load[F]
}