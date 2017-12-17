//
// React - a library for functional-reactive-like programming
// Copyright (c) 2011-present, React Authors
// http://github.com/threerings/react/blob/master/LICENSE

package react;

/**
 * A view of a {@link Signal}, on which slots may listen, but to which one cannot emit events. This
 * is generally used to provide signal-like views of changing entities. See {@link AbstractValue}
 * for an example.
 */
public interface SignalView<T> {

  /**
   * Used to observe events from a signal. Normally one uses {@link Slot} rather than this
   * listener, but this interface exists to allow Java 8 lambdas to be used as well.
   */
  interface Listener<T> extends Reactor.RListener {
    /**
     * Called when a signal to which this slot is connected has emitted an event.
     * @param event the event emitted by the signal.
     */
    void onEmit (T event);
  }

  /**
   * Creates a signal that maps this signal via a function. When this signal emits a value, the
   * mapped signal will emit that value as transformed by the supplied function. The mapped
   * signal will retain a connection to this signal for as long as it has connections of its own.
   */
  <M> SignalView<M> map (Function<? super T, M> func);

  /**
   * Creates a signal that emits a value only when the supplied filter function returns true. The
   * filtered signal will retain a connection to this signal for as long as it has connections of
   * its own.
   */
  SignalView<T> filter (Function<? super T, Boolean> pred);

  /**
   * Creates a signal that maps the values emitted by this signal through {@code collector} and
   * emits only the non-null values that are returned. This allows you to perform a type-test on
   * the values emitted by a signal and only emit values of the appropriate subtype.
   */
  <M> SignalView<M> collect (Function<? super T, M> collector);

  /**
   * Returns a future that is completed with the next value from this signal.
   */
  RFuture<T> next ();

  /**
   * Connects this signal to the supplied slot, such that when an event is emitted from this
   * signal, the slot will be notified.
   *
   * @return a connection instance which can be used to cancel the connection.
   */
  Connection connect (Listener<? super T> slot);

  /**
   * Disconnects the supplied slot from this signal if connect was called with it. If the slot has
   * been connected multiple times, all connections are cancelled.
   */
  void disconnect (Listener<? super T> slot);
}
