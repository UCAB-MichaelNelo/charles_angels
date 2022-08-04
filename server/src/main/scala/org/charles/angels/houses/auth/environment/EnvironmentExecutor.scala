package org.charles.angels.houses.auth.environment

import cats.~>
import cats.implicits.*
import org.charles.angels.houses.auth.AuthAction
import org.charles.angels.houses.auth.Make
import cats.arrow.FunctionK
import cats.effect.kernel.Resource
import scala.util.Properties
import cats.effect.kernel.Sync
import tsec.cipher.symmetric.*
import tsec.cipher.symmetric.jca.*
import tsec.common.*
import org.charles.angels.houses.errors.ServerError

class EnvironmentExecutor[F[_]: Sync: [F[_]] =>> IvGen[F, AES128CTR]](username: String, password: String, key: SecretKey[AES128CTR])
    extends (AuthAction ~> F) {

  override def apply[A](fa: AuthAction[A]): F[A] = fa match {
    case AuthAction.Login(username, password) => 
      if (username == this.username && password == this.password)
        AES128CTR.encrypt[F](PlainText(s"${username}:${password}".utf8Bytes), key).map(_.toConcatenated.toHexString).attempt
      else
        Sync[F].raiseError(ServerError.InvalidUserOrPassword)
    case AuthAction.ValidateToken(token) => (for
      encryptedText <- Sync[F].fromEither(AES128CTR.ciphertextFromConcat(token.utf8Bytes))
      _ <- AES128CTR.decrypt[F](encryptedText, key)
    yield ()).attempt
  }
}

case class Environment(username: String, password: String, rawKey: String)

given [F[_]: Sync]: Make[F, Environment] with
    given IvGen[F, AES128CTR] = AES128CTR.defaultIvStrategy[F]
    def make(input: Environment): Resource[F, FunctionK[org.charles.angels.houses.auth.AuthAction, F]] = for
      key <- Resource.eval { AES128CTR.buildKey[F](input.rawKey.utf8Bytes) }
    yield EnvironmentExecutor(input.username, input.password, key)