package au.com.cba.omnia.starthilfe

import org.scalacheck.Arbitrary

import org.specs2._
import org.specs2.Specification
import org.specs2.matcher.Matcher

import scalaz.Equal

import au.com.cba.omnia.omnitool.{Result, Ok, Error}
import au.com.cba.omnia.omnitool.test.OmnitoolProperties.resultantMonad
import au.com.cba.omnia.omnitool.test.Arbitraries._

object RSpec extends Specification with ScalaCheck {
  def is = sequential ^ s2"""
R Operations
===============

R operations should:
  obey resultant monad laws (monad and plus laws)                            ${resultantMonad.laws[R]}
R operations:
  R handles exceptions  $safeR

"""

  def safeR = prop { (t: Throwable) =>

    R.withClient(_ => throw t) must beResult {
      Result.exception(t)
    }
  }

  implicit def RengineArbirary[A: Arbitrary]: Arbitrary[R[A]] =
    Arbitrary(Arbitrary.arbitrary[Result[A]] map (R.result(_)))

  implicit def RengineEqual: Equal[R[Int]] =
    Equal.equal[R[Int]]((a, b) =>
      a.run must_== b.run)

  def beResult[A](expected: Result[A]): Matcher[R[A]] =
    (h: R[A]) => h.run must_== expected


}
