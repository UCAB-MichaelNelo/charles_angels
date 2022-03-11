package org.charles.angels.houses

import cats.data.EitherT
import cats.free.Free
import cats.data.EitherK
import org.charles.angels.houses.logging.LoggingAction
import org.charles.angels.houses.db.DatabaseAction
import org.charles.angels.houses.filesystem.FilesystemAction

package object compiler:
  private type ServerAction0[A] = EitherK[LoggingAction, DatabaseAction, A]
  type ServerAction[A] = EitherK[FilesystemAction, ServerAction0, A]
  type CompilerLanguage[F[_], A] = EitherT[Free[F, _], Throwable, A]
  type ServerLanguage[A] = CompilerLanguage[ServerAction, A]
