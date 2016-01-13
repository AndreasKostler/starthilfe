package au.com.cba.omnia
package starthilfe

import scala.util.control.NonFatal

import scalaz._, Scalaz._

import org.rosuda.REngine.{ REngine, REXP, REXPLogical, REXPString }


import au.com.cba.omnia.omnitool.{Result, ResultantMonad, ResultantOps, ToResultantMonadOps}

/**
 * A data-type that represents a R operation.
 *
 * R operations produce a (potentially failing) result. For
 * convenience R operations receive a handle to a REngine object. The REngine
 * is created when the run method is called.
 *
 */

case class R[A](action: REngine => Result[A]) {
  /** Runs the R action with a REngine created */
  def run(): Result[A] = {
    try {

      val re = Option(REngine.getLastEngine)
        .getOrElse(REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine") )

      try {
        action(re)
      } catch {
        case NonFatal(t) => Result.error("Failed to run R operation", t)
      } finally {
        val _ = re.close
      }
    } catch {
      case NonFatal(t) => Result.error("Failed to create REngine", t)
    }
  }
}

/** Hive operations */
// NB that this is the Hive equivalent of the HDFS monad in permafrost.
object R extends ResultantOps[R] with ToResultantMonadOps {

  /** Gets the R engine */
  def getREngine: R[REngine] =
    R(client => Result.ok(client))

  /** Builds a R operation from a function. The resultant R operation will not throw an exception. */
  def withREngine[A](f: REngine => A): R[A] =
    R(client => Result.safe(f(client)))

  /**
   * Sets the R working directory
   *
   * @param dir The working directory
   */
  def setWorkingDir(dir: String): R[REXP] = R.getREngine >>= { re =>
    val res = re.parseAndEval(s"setwd('${dir}')")
    R.value(res)
  }

  def isEnvironment(env: String): R[Boolean] = R.getREngine >>= { re =>
    val res = re.parseAndEval(s"is.environment($env)") match {
      case log: REXPLogical => log
      case _ => throw new ClassCastException
    }
    R.value(res.isTRUE.head)
  }
  /**
   * Creates a R environment and assigns it to env
   * @param env The environment
   */
  def createEnvironment(env: String): R[String] = R.getREngine >>= { re =>
    val _ = re.parseAndEval(s"$env <- new.env()")
    R.value(env)
  }

  /**
   * Creates a R environment and assigns it to env
   * if it doesn't already exists. Return Error if it
   * does already exist
   * @param env The environment
   */
  def createEnvironmentStrict(env: String): R[REXP] = ???


  /**
   *
   *
   */
  def runScript = ???

  implicit val monad: ResultantMonad[R] = new ResultantMonad[R] {
    def rPoint[A](v: => Result[A]): R[A] = R[A](_ => v)

    def rBind[A, B](ma: R[A])(f: Result[A] => R[B]): R[B] =
      R(client => f(ma.action(client)).action(client))
  }
}

