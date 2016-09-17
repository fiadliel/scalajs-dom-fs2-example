package scalajs_fs2.webapp

import fs2._
import fs2.async.mutable.Queue
import org.scalajs.dom

import scala.scalajs.js

object Implicits {
  implicit class ElementSyntax(val element: dom.raw.Element) extends AnyVal {
    private def makeQueue(eventType: String)(implicit S: Strategy) = {
      def clickEventListener(queue: Queue[Task, dom.Event]): js.Function1[dom.Event, Unit] = { evt: dom.Event =>
        queue.enqueue1(evt).unsafeRunAsync(_ => ())
      }

      def createQueue = {
        for {
          queue <- async.unboundedQueue[Task, dom.Event]
          listenerFn = clickEventListener(queue)
          _ <- Task.delay(element.addEventListener(eventType, listenerFn))
        } yield (queue, listenerFn)
      }

      def emitQueue(queue: Queue[Task, dom.Event], listenerFn: js.Function1[dom.Event, Unit]) =
        Stream.emit(queue)

      def cleanupQueue(queue: Queue[Task, dom.Event], listenerFn: js.Function1[dom.Event, Unit]) =
        Task.delay(element.removeEventListener(eventType, listenerFn))

      Stream.bracket(createQueue)((emitQueue _).tupled, (cleanupQueue _).tupled)
    }

    def stream(eventType: String)(implicit S: Strategy): Stream[Task, dom.Event] =
      makeQueue(eventType).flatMap(_.dequeueAvailable)
  }
}
