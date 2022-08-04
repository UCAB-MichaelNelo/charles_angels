package org.charles.angels.houses.auth

import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum AuthAction[A] {
    case Login(username: String, password: String) extends AuthAction[Either[Throwable, String]]
    case ValidateToken(token: String) extends AuthAction[Either[Throwable, Unit]]
}

trait AuthLanguage[F[_]](using InjectK[AuthAction, F]) {
    def login(username: String, password: String) = EitherT(
        Free.liftInject(AuthAction.Login(username, password))
    )
    def validateToken(token: String) = EitherT(
        Free.liftInject(AuthAction.ValidateToken(token))
    )
}