package au.com.cba.omnia
package starthilfe

import scala.util.control.NonFatal

import scalaz._, Scalaz._

import org.rosuda.REngine.{ REngine, REXP, REXPLogical, REXPString }


import au.com.cba.omnia.omnitool.{Result, ResultantMonad, ResultantOps, ToResultantMonadOps}

/**
 * A data-type that represents a R operation.
 *
 * R operations execute in a R environment and produce a (potentially failing) result. For
 * convenience R operations receive a handle to a REngine and Environment object. The REngine
 * and Environment are created when the run method is called. If no environment name is given,
 * the R operation will be executed in the default environment returned by the 'globalenv()'
 * function in R.
 *
 */
case class R[A](action: (REXP, REngine) => Result[A]) {
  /** Runs the R action with a REngine created */

  def run(env: Option[String]): Result[A] = {
    try {

      val re = Option(REngine.getLastEngine)
        .getOrElse(REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine"))

      try {

        //create R environment
        val e = env
          .map(e => re.parseAndEval(s"$e <- new.env()"))
          .getOrElse(re.parseAndEval(s"globalenv()"))

        action(e, re)
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
    R((_, re) => Result.ok(re))

  /** Gets the R environment */
  def getEnvironment: R[REXP] =
    R((env, _) => Result.ok(env))

  /** Gets the R engine and environment */
  def getEnvREngine: R[(REXP, REngine)] =
    R((env, re) => Result.ok((env, re)))

  /** Builds a R operation from a function. The resultant R operation will not throw an exception. */
  def withREngine[A](f: REngine => A): R[A] =
    R((_, re) => Result.safe(f(re)))

  /** Builds a R operation from a function. The resultant R operation will not throw an exception. */
  def withEnvironment[A](f: REXP => A): R[A] =
    R((env, _) => Result.safe(f(env)))

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
    val e: REXP = re.parseAndEval(s"$env <- new.env()")
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
    def rPoint[A](v: => Result[A]): R[A] = R[A]( (_, _) => v)

    def rBind[A, B](ma: R[A])(f: Result[A] => R[B]): R[B] =
      R((env, re) => f(ma.action(env, re)).action(env, re))
  }
}

