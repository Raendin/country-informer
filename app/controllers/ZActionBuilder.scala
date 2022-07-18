package controllers

import zio._
import play.api.mvc._

object ZActionBuilder {

  val runtime: Runtime[ZEnv] = Runtime.default

  implicit class ActionBuilderOps[+R[_], B](actionBuilder: ActionBuilder[R, B]) {

    def zio[E](zioActionBody: R[B] => IO[E, Result]): Action[B] = actionBuilder.async { request =>
      runtime.unsafeRunToFuture(
        ioToTask(zioActionBody(request))
      )
    }

    def zio[E, A](bp: BodyParser[A])(zioActionBody: R[A] => IO[E, Result]): Action[A] =
      actionBuilder(bp).async { request =>
        runtime.unsafeRunToFuture(
          ioToTask(zioActionBody(request))
        )
      }

    private def ioToTask[E, A](io: IO[E, A]) =
      io.mapError {
        case t: Throwable => t
        case s: String    => new Throwable(s)
        case e            => new Throwable("Error: " + e.toString)
      }
  }

  implicit class RecoverIO[E, A](io: IO[E, A]) {
    def recover(f: E => A): UIO[A] = io.fold(f, identity)
  }
}
