package org.charles.angels.houses.application.services

import java.io.File
import org.charles.angels.houses.application.Language
import cats.InjectK
import cats.data.EitherT
import cats.free.Free

enum ServiceAction[A]:
  case AllocFile(contents: Array[Byte], name: String)
      extends ServiceAction[File]
  case DeallocFile(file: File) extends ServiceAction[Unit]

trait ServiceAlgebra[F[_]]:
  def alloc(contents: Array[Byte], name: String): Language[F, File]
  def dealloc(file: File): Language[F, Unit]

class ServiceLanguage[F[_]](using InjectK[ServiceAction, F])
    extends ServiceAlgebra[F]:
  def alloc(contents: Array[Byte], name: String) = EitherT.right(
    Free.liftInject(ServiceAction.AllocFile(contents, name))
  )
  def dealloc(file: File) = EitherT.right(
    Free.liftInject(ServiceAction.DeallocFile(file))
  )
