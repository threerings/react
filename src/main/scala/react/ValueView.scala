//
// React - a library for functional-reactive-like programming in Java
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/react/blob/master/LICENSE

package react

/**
 * A view of a `Value`, to which listeners may be added, but which one cannot update. This can be
 * used in combination with `AbstractValue` to provide `Value` semantics to an entity which
 * dispatches value changes in a custom manner (like over the network). Value consumers should
 * require only a view on a value, rather than a concrete value.
 */
trait ValueView[T]
{
  /** Returns the current value. */
  def get :T

  /** Creates a value that maps this value via a function. When this value changes, the mapped
   * listeners will be notified, regardless of whether the new and old mapped values differ. */
  def map[M] (func :Function[_ >: T,M]) :MappedValueView[M]

  /** Connects the supplied listener to this value, such that it will be notified when this value
   * changes.
   * @return a connection instance which can be used to cancel the connection. */
  def connect (listener :ValueView.Listener[_ >: T]) :Connection

  /** Connects the supplied listener to this value, such that it will be notified when this value
   * changes. Also immediately notifies the listener of the current value. Note that the previous
   * value supplied with this notification will be null. If the notification triggers an unchecked
   * exception, the slot will automatically be disconnected and the caller need not worry about
   * cleaning up after itself.
   * @return a connection instance which can be used to cancel the connection. */
  def connectNotify (listener :ValueView.Listener[_ >: T]) :Connection

  /** Disconnects the supplied listener from this value if it's connected. If the listener has been
   * connected multiple times, all connections are cancelled. */
  def disconnect (listener :ValueView.Listener[_ >: T]) :Unit
}

/**
 * Some implicits that make using `ValueView` and friends more pleasant in Scala.
 */
object ValueView
{
  /** Used to observe changes to a value. One must override only one of the {@link #onChange}
   * methods, depending on how much information is desired. */
  abstract class Listener[T] extends Reactor.RListener {
    /** Called when the value to which this listener is bound has changed. */
    def onChange (value :T, oldValue :T)
  }

  /** Enrichments to the `ValueView` type. */
  class RichValue[T] (value :ValueView[T]) {
    /** Enables the use of a block as a UnitSlot. For example:
     * `value += { println("Yay!") }` */
    def +=[A>:T] (f : => Unit) = value.connect(new Slot[A] { def onEmit (t :A) = f })

    /** Connects the supplied function to the value as a slot. For example:
     * `value += { a => println(a) }` */
    def +=[A>:T] (f :A => Unit) = value.connect(new Slot[A] { def onEmit (t :A) = f(t) })

    /** Connects the supplied function to the value as a slot and notifies. For example:
     * `value +=! { a => println(a) }` */
    def +=![A>:T] (f :A => Unit) = value.connectNotify(new Slot[A] { def onEmit (t :A) = f(t) })
  }
  implicit def enrichValue[T] (value :ValueView[T]) = new RichValue[T](value)
}
