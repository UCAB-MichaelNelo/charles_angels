package org.charles.angels.houses.filesystem

import org.charles.angels.houses.compiler.CompilerLanguage
import java.io.File
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import fs2.Stream
import cats.effect.kernel.Async

enum FilesystemAction[A]:
  case CreateFile(contents: Array[Byte], name: String)
      extends FilesystemAction[Either[Throwable, File]]
  case Resolve(name: String) extends FilesystemAction[String]
  case DeleteFile(file: File) extends FilesystemAction[Either[Throwable, Unit]]
  case GetFileContents(path: String) extends FilesystemAction[GetStream]
  case CreateFileWithStreamedContents(name: String)
      extends FilesystemAction[WriteStream]

trait FilesystemLanguage[F[_]](using InjectK[FilesystemAction[_], F]) {
  def resolve(name: String): CompilerLanguage[F, String] = EitherT.liftF(Free.liftInject(FilesystemAction.Resolve(name)))
  def createFile(
      contents: Array[Byte],
      name: String
  ): CompilerLanguage[F, File] = EitherT(
    Free
      .liftInject[F]
      .apply[FilesystemAction[_], Either[Throwable, File]](
        FilesystemAction.CreateFile(contents, name)
      )
  )
  def deleteFile(file: File): CompilerLanguage[F, Unit] = EitherT(
    Free
      .liftInject[F]
      .apply[FilesystemAction[_], Either[Throwable, Unit]](
        FilesystemAction.DeleteFile(file)
      )
  )
  def getFileContents(
      file: File
  ): CompilerLanguage[F, GetStream] = EitherT.right(
    Free
      .liftInject(
        FilesystemAction.GetFileContents(file.getAbsolutePath)
      )
  )
  def createFile(name: String): CompilerLanguage[F, WriteStream] =
    EitherT.right(
      Free.liftInject(
        FilesystemAction.CreateFileWithStreamedContents(name)
      )
    )
}

trait GetStream {
  def apply[F[_]: Async]: Stream[F, Byte]
}

trait WriteStream {
  def apply[F[_]: Async](stream: Stream[F, Byte]): F[File]
}
