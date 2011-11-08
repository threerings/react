//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react

import org.junit.Test
import org.junit.Assert._

/**
 * Tests our Scala implicits.
 */
class ImplicitsTest
{
  @Test def testSignal {
    var counter = 0
    val signal = new Signal[Int]

    val c = signal += { n => counter += n }
    signal.emit(3)
    assertEquals(3, counter)
    c.disconnect

    signal += { counter += 2 }
    signal.emit(9)
    assertEquals(5, counter)
  }

  @Test def testNamedUnitFunctionOnSignal {
    var counter = 0
    def incCounter () = counter += 5
    val signal = new Signal[Int]
    signal += incCounter
    signal.emit(9)
    assertEquals(5, counter)
  }

  @Test def testNamedFunctionOnSignal {
    val signal = new Signal[Int]
    var counter = 0
    def incCounter (n :Int) = counter += n
    signal += incCounter _
    signal.emit(5)
    assertEquals(5, counter)
  }

  @Test def testValue {
    var counter = 0
    val value = new Value(0)

    val c = value += { n => counter += n }
    value.update(3)
    assertEquals(3, counter)
    c.disconnect

    value += { counter += 2 }
    value.update(9)
    assertEquals(5, counter)
  }

  @Test def testNamedUnitFunctionOnValue {
    var counter = 0
    def incCounter () = counter += 5
    val value = new Value(0)
    value += incCounter
    value.update(9)
    assertEquals(5, counter)
  }

  @Test def testNamedFunctionOnValue {
    val value = new Value(0)
    var counter = 0
    def incCounter (n :Int) = counter += n
    value += incCounter _
    value.update(5)
    assertEquals(5, counter)
  }

  @Test def testConnectNotify {
    val value = new Value(5)
    var got = 0
    value +=! { n => got = n }
    assertEquals(5, got)
  }

  @Test def testMapFunc {
    val value = new Value(0)
    val asStr = value.map((_ :Int).toString)
    var counter = 0
    asStr += { a :String =>
      assertEquals("1", a)
      counter += 1
    }
    value.update(1)
    assertEquals(1, counter)
  }
}
