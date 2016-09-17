package scalajs_fs2.webapp

import Implicits._

import fs2._
import org.scalajs.dom

import scala.scalajs.js.JSApp

object MainApp extends JSApp {
  implicit val strategy = Strategy.default

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
      _ <- clickDiv.stream("click").take(5).to(printLocation).run
    } yield ()).unsafeRunAsync(_ => ())
  }
}
