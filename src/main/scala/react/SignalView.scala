//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react

/**
 * A view of a `Signal`, on which slots may listen, but to which one cannot emit events. This is
 * generally used to provide signal-like views of changing entities. See `AbstractValue` for an
 * example.
 */
trait SignalView[T]
{
  /** Creates a signal that maps this signal via a function. When this signal emits a value, the
   * mapped signal will emit that value as transformed by the supplied function. */
  def map[M] (func :Function[_ >: T,M]) :MappedSignalView[M]

  /** Connects this signal to the supplied slot, such that when an event is emitted from this
   * signal, the slot will be notified.
   * @return a connection instance which can be used to cancel the connection. */
  def connect (slot :Slot[_ >: T]) :Connection

  /** Disconnects the supplied slot from this signal if connect was called with it. If the slot has
   * been connected multiple times, all connections are cancelled. */
  def disconnect (slot :Slot[_ >: T]) :Unit
}

/**
 * Some implicits that make using `SignalView` and friends more pleasant in Scala.
 */
object SignalView
{
  /** Enrichments to the `SignalView` type. */
  class RichSignal[T] (signal :SignalView[T]) {
    /** Enables the use of a block as a UnitSlot. For example:
     * `foo.signal += { println("Yay!") }`
     */
    def +=[A>:T] (f : => Unit) =
      signal.connect(new Slot[A] { def onEmit (t :A) = f })

    /** Connects the supplied function to the signal as a slot. For example:
     * `foo.signal += { a => println(a) }`
     */
    def +=[A>:T] (f :A => Unit) =
      signal.connect(new Slot[A] { def onEmit (t :A) = f(t) })
  }
  implicit def enrichSignal[T] (signal :SignalView[T]) = new RichSignal[T](signal)
}
