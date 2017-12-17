//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A view of a {@link Value}, to which listeners may be added, but which one cannot update. This
 * can be used in combination with {@link AbstractValue} to provide {@link Value} semantics to an
 * entity which dispatches value changes in a custom manner (like over the network). Value
 * consumers should require only a view on a value, rather than a concrete value.
 */
public interface ValueView<T>
{
  /** Used to observe changes to a value. */
  interface Listener<T> extends Reactor.RListener {
    /** Called when the value to which this listener is bound has changed. */
    void onChange (T value, T oldValue);
  }

  /**
   * Returns the current value.
   */
  T get ();

  /**
   * Creates a value that maps this value via a function. When this value changes, the mapped
   * listeners will be notified, regardless of whether the new and old mapped values differ. The
   * mapped value will retain a connection to this value for as long as it has connections of its
   * own.
   */
  <M> ValueView<M> map (Function<? super T, M> func);

  /**
   * Creates a value that flat maps (monadic binds) this value via a function. When this value
   * changes, the mapping function is called to obtain a new reactive value. All of the listeners
   * to the flat mapped value are "transferred" to the new reactive value. The mapped value will
   * retain a connection to the most recent reactive value for as long as it has connections of
   * its own.
   */
  <M> ValueView<M> flatMap (Function<? super T, ? extends ValueView<M>> func);

  /**
   * Returns a signal that is emitted whenever this value changes.
   */
  SignalView<T> changes ();

  /**
   * Returns a future which is completed with this value when the value meeds {@code cond}. If
   * the value meets {@code cond} now, the future will be completed immediately, otherwise the
   * future will be completed when the value changes to a value which meets {@code cond}.
   */
  RFuture<T> when (Function<? super T, Boolean> cond);

  /**
   * Connects the supplied listener to this value, such that it will be notified when this value
   * changes. The listener is held by a strong reference, so it's held in memory by virtue of
   * being connected.
   * @return a connection instance which can be used to cancel the connection.
   */
  Connection connect (Listener<? super T> listener);

  /**
   * Connects the supplied listener to this value, such that it will be notified when this value
   * changes. Also immediately notifies the listener of the current value. Note that the previous
   * value supplied with this notification will be null. If the notification triggers an
   * unchecked exception, the slot will automatically be disconnected and the caller need not
   * worry about cleaning up after itself.
   * @return a connection instance which can be used to cancel the connection.
   */
  Connection connectNotify (Listener<? super T> listener);

  /**
   * Disconnects the supplied listener from this value if it's connected. If the listener has been
   * connected multiple times, all connections are cancelled.
   */
  void disconnect (Listener<? super T> listener);

  // these methods exist to let javac know that it can synthesize a Slot when a single argument
  // lambda is passed to connect/connectNotify

  /**
   * Connects the supplied listener to this value, such that it will be notified when this value
   * changes. The listener is held by a strong reference, so it's held in memory by virtue of
   * being connected.
   * @return a connection instance which can be used to cancel the connection.
   */
  Connection connect (Slot<? super T> listener);

  /**
   * Connects the supplied listener to this value, such that it will be notified when this value
   * changes. Also immediately notifies the listener of the current value. If the notification
   * triggers an unchecked exception, the slot will automatically be disconnected and the caller
   * need not worry about cleaning up after itself.
   * @return a connection instance which can be used to cancel the connection.
   */
  Connection connectNotify (Slot<? super T> listener);
}
