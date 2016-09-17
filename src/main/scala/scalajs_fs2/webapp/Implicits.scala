package scalajs_fs2.webapp

import fs2._
import fs2.async.mutable.Queue
import org.scalajs.dom

import scala.scalajs.js

object Implicits {
  implicit class ElementSyntax(val element: dom.raw.Element) extends AnyVal {
    private def makeQueue(eventType: String)(implicit S: Strategy): Stream[Task, Queue[Task, Option[dom.Event]]] = {
      def clickEventListener(queue: Queue[Task, Option[dom.Event]]) = { evt: dom.Event =>
        queue.enqueue1(Some(evt)).unsafeRunAsync(_ => ())
      }

      def createAddAndRemove(queue: Queue[Task, Option[dom.Event]], evtType: String) = {
        val listenerFn = clickEventListener(queue)
        (Task.delay(element.addEventListener(evtType, listenerFn)), Task.delay(element.removeEventListener(evtType, listenerFn)))
      }

      def createQueue = {
        for {
          queue <- async.unboundedQueue[Task, Option[dom.Event]]
          listenerFn = clickEventListener(queue): js.Function1[dom.Event, Unit]
          _ <- Task.delay(element.addEventListener(eventType, listenerFn))
        } yield (queue, listenerFn)
      }

      def emitQueue(queue: Queue[Task, Option[dom.Event]], listenerFn: js.Function1[dom.Event, Unit]): Stream[Task, Queue[Task, Option[dom.Event]]] =
        Stream.emit(queue)

      def cleanupQueue(queue: Queue[Task, Option[dom.Event]], listenerFn: js.Function1[dom.Event, Unit]): Task[Unit] =
        queue.enqueue1(None).flatMap(_ => Task.delay(element.removeEventListener(eventType, listenerFn)))

      Stream.bracket(createQueue)((emitQueue _).tupled, (cleanupQueue _).tupled)
    }

    def stream(eventType: String)(implicit S: Strategy): Stream[Task, dom.Event] =
      makeQueue(eventType).flatMap(_.dequeueAvailable.unNoneTerminate)
  }
}
