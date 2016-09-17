package scalajs_fs2.webapp

import fs2._

import org.scalajs.dom
import dom.html.Element

import scala.scalajs.js.JSApp

object MainApp extends JSApp {
  implicit val strategy = Strategy.default

  implicit class ElementSyntax(val element: dom.raw.Element) extends AnyVal {
    private def mkQueue =
      Stream.bracket(async.unboundedQueue[Task, Option[dom.Event]])(Stream.emit, _.enqueue1(None))

    private def addEventListener(queue: async.mutable.Queue[Task, Option[dom.Event]], evtType: String) =
      Task.delay {
        element.addEventListener(evtType, { evt: dom.Event =>
          queue.enqueue1(Some(evt)).unsafeRunAsync(_ => ())
        })
      }

    def stream(evtType: String): Stream[Task, dom.Event] = {
      for {
        queue <- mkQueue
        _ <- Stream.eval(addEventListener(queue, evtType))
        event <- queue.dequeueAvailable.unNoneTerminate
      } yield event
    }
  }

  def printLocation: Sink[Task, dom.Event] =
    _.evalMap {
      case me: dom.MouseEvent =>
        Task.delay(println(s"[${me.clientX}, ${me.clientY}]"))
      case _ =>
        Task.now(())
    }

  def createDiv: Task[dom.raw.Element] = Task.delay {
    val child = dom.document.createElement("div")
    child.textContent = "Click here!"
    dom.document.body.appendChild(child)
    child
  }

  def main(): Unit = {
    (for {
      clickDiv <- createDiv
      stream <- clickDiv.stream("click").to(printLocation).run
    } yield clickDiv).unsafeRunAsync(_ => ())
  }
}
