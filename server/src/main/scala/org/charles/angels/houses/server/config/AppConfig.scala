package org.charles.angels.houses.server.config

final case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    parallelismLevel: Int, 
)

final case class FsConfig(
    baseDir: String
)

final case class HttpConfig(
    port: Int
)

final case class AuthenticationConfig(
    user: String,
    password: String,
    rawKey: String
)

final case class AppConfig(
    database: DatabaseConfig,
    fs: FsConfig,
    http: HttpConfig,
    authentication: AuthenticationConfig
)