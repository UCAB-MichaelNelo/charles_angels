package org.charles.angels.houses.filesystem.jvm

import cats.~>
import cats.syntax.all.*
import org.charles.angels.houses.filesystem.FilesystemAction
import org.charles.angels.houses.filesystem.GetStream
import org.charles.angels.houses.filesystem.WriteStream
import org.charles.angels.houses.compiler.ServerLanguage
import cats.effect.kernel.Async
import cats.effect.std.Console
import java.io.File
import java.io.PrintWriter
import java.nio.charset.Charset
import fs2.io.file.Files
import fs2.io.file.Flags
import fs2.Stream
import fs2.io.file.Path
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.filesystem.Make
import cats.effect.kernel.Resource
import cats.data.OptionT
import fs2.Pure

case object ImageNotFoundError extends Throwable("Image not found")

class JvmInterpreter[F[_]: Async](basePath: String)
    extends (FilesystemAction ~> F):
  private def interpret[A](fsaction: FilesystemAction[A]): F[A] = fsaction match
    case FilesystemAction.CreateFile(contents, name) =>
      val path = Path(f"$basePath/$name")
      val createDir = path.parent.toOptionT
        .semiflatMap(Files[F].createDirectory)
      val createFile =
        OptionT.liftF(Files[F].createFile(path))

      Files[F]
        .exists(path)
        .ifM(
          Files[F].delete(path) >> createDir.value.attempt >> createFile.value,
          createDir.value.attempt >> createFile.value
        )
      >>
      Stream
        .emits(contents)
        .covary[F]
        .through(Files[F].writeAll(path, Flags.Write))
        .compile
        .drain
        .as(File(path.toString))
        .attempt
    case FilesystemAction.DeleteFile(file) =>
      Async[F].interruptible { file.delete }.void.attempt
    case FilesystemAction.GetFileContents(location) =>
      val path = Path(location)
      Files[F]
        .exists(path)
        .ifM(
          new GetStream {
            def apply[G[_]: Async] = Files[G].readAll(path)
          }.pure[F],
          ImageNotFoundError.raiseError
        )
    case FilesystemAction.Resolve(name) => f"$basePath/$name".pure[F]
    case FilesystemAction.CreateFileWithStreamedContents(name) =>
      val path = f"$basePath/$name"
      new WriteStream {
        def apply[G[_]: Async](contents: Stream[G, Byte]) =
          contents
            .through(Files[G].writeAll(Path(path)))
            .compile
            .drain
            .as(File(path))
      }.pure[F]

  def apply[A](fsaction: FilesystemAction[A]): F[A] =
    interpret(fsaction).adaptErr(ServerError.FilesystemError(_))

class JVM(val basePath: String)

given [F[_]: Async]: Make[F, JVM] with
  def make(jvm: JVM) = JvmInterpreter[F](jvm.basePath).pure[Resource[F, _]]
