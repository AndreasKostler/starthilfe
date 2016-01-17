package au.com.cba.omnia.starthilfe

import org.scalacheck.Arbitrary

import org.specs2._
import org.specs2.Specification
import org.specs2.matcher.Matcher

import scalaz.Scalaz._
import scalaz.Equal

import au.com.cba.omnia.omnitool.Result
import au.com.cba.omnia.omnitool.test.OmnitoolProperties.resultantMonad
import au.com.cba.omnia.omnitool.test.Arbitraries._

object RSpec extends Specification with ScalaCheck {
  def is = sequential ^ s2"""
R Operations
===============

R operations should:
  obey resultant monad laws (monad and plus laws) ${resultantMonad.laws[R]}
R operations:
  R handles exceptions                            $safeR
  .GlobalEnv is environment                       $globalEnvR
  Arbitrary object is not environment             $arbEnvR
  Created environment must exist                  $existsEnvR
  R evaluates constant correctly                  $evalConstantR
  R evaluates constant in environment correctly   $evalConstantEnvR
"""

  def safeR = prop { (t: Throwable) =>

    R.withREngine(_ => throw t) must beResult {
      Result.exception(t)
    }

    R.withEnvironment(_ => throw t) must beResult {
      Result.exception(t)
    }

    R.value(3).map(_ => throw t) must beResult {
      Result.exception(t)
    }
  }

  def globalEnvR = {
    val res = for {
      isEnv <- R.isEnvironment(".GlobalEnv")
    } yield isEnv
    res must beValue(true)
  }

  def arbEnvR = {
    val res = for {
      _ <- R.withREngine(re => re.parseAndEval("x <- 42"))
      isEnv <- R.isEnvironment("x")
    } yield isEnv
    res must beValue(false)
  }

  def existsEnvR = {
    val res = for {
      env <- R.createEnvironment("foo")
      isEnv <- R.isEnvironment(env)
    } yield isEnv
    res must beValue(true)
  }

  def evalConstantR = {
    val res = for {
      r <- R.eval("as.integer(42)")
    } yield r.asInteger == 42
    res must beValue(true)
  }

  def evalConstantEnvR = {
    val res = for {
      env <- R.createEnvironment("foo")
      r <- R.withREngine( re => re.parseAndEval("as.integer(42)"))
    } yield r.asInteger == 42
    res must beValue(true)
  }

  implicit def RengineArbirary[A: Arbitrary]: Arbitrary[R[A]] =
    Arbitrary(Arbitrary.arbitrary[Result[A]] map (R.result(_)))

  implicit def RengineEqual: Equal[R[Int]] =
    Equal.equal[R[Int]]((a, b) =>
      a.run(None) must_== b.run(None))

  def beResult[A](expected: Result[A]): Matcher[R[A]] =
    (h: R[A]) => h.run(None) must_== expected

  def beValue[A](expected: A): Matcher[R[A]] =
    beResult(Result.ok(expected))

}
