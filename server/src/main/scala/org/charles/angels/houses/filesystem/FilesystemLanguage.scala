package org.charles.angels.houses.filesystem

import org.charles.angels.houses.compiler.CompilerLanguage
import java.io.File
import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum FilesystemAction[A]:
  case CreateFile(contents: Array[Byte], name: String)
      extends FilesystemAction[Either[Throwable, File]]
  case DeleteFile(file: File) extends FilesystemAction[Either[Throwable, Unit]]

trait FilesystemOperation[F[_]]:
  def createFile(contents: Array[Byte], name: String): CompilerLanguage[F, File]
  def deleteFile(file: File): CompilerLanguage[F, Unit]

class FilesystemLanguage[F[_]](using InjectK[FilesystemAction, F])
    extends FilesystemOperation[F]:
  def createFile(contents: Array[Byte], name: String) = EitherT(
    Free.liftInject(FilesystemAction.CreateFile(contents, name))
  )
  def deleteFile(file: File) = EitherT(
    Free.liftInject(FilesystemAction.DeleteFile(file))
  )
