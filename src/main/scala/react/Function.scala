//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * Models a single argument function.
 */
trait Function[F,T]
{
  /** Applies this function to the supplied input value. A function is generally expected to have
   * no side effects; violate that assumption at your peril. */
  def apply (value :F) :T
}

object Function
{
  /** Allows the use of Scala functions for React functions. */
  implicit def scalaFuncToReactFunc[A,B] (f :A => B) = new Function[A,B] { def apply (v :A) = f(v) }
}
